package com.ton.veinminer;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Result;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(VeinMiner.MOD_ID)
public final class VeinMiner {
    public static final String MOD_ID = "veinminer";

    private static final int MAX_BLOCKS = 64;
    private static final ThreadLocal<Boolean> VEIN_BREAK = ThreadLocal.withInitial(() -> false);

    public VeinMiner() {
        BlockEvent.BreakEvent.BUS.addListener(VeinMiner::onBreak);
    }

    private static void onBreak(BlockEvent.BreakEvent event) {
        if (Boolean.TRUE.equals(VEIN_BREAK.get())) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        var player = event.getPlayer();
        if (player == null || !player.isShiftKeyDown() || player.isCreative()) {
            return;
        }

        var level = event.getLevel();
        var origin = event.getPos();
        var originState = event.getState();
        var tool = player.getMainHandItem();

        if (originState.isAir() || !ForgeHooks.isCorrectToolForDrops(originState, player)) {
            return;
        }

        var positions = VeinSearch.collect(level, origin, originState.getBlock(), MAX_BLOCKS);
        if (positions.size() <= 1) {
            return;
        }

        VEIN_BREAK.set(true);
        try {
            int broken = 0;
            for (var pos : positions) {
                var state = level.getBlockState(pos);
                if (state.isAir() || state.getBlock() != originState.getBlock()) {
                    continue;
                }
                if (!ForgeHooks.isCorrectToolForDrops(state, player)) {
                    continue;
                }

                if (level.destroyBlock(pos, true, player)) {
                    broken++;
                    if (!tool.isEmpty() && tool.isDamageableItem()) {
                        tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        if (tool.isEmpty()) {
                            break;
                        }
                    }
                }
            }

            if (broken > 0) {
                event.setResult(Result.DENY);
            }
        } finally {
            VEIN_BREAK.set(false);
        }
    }
}