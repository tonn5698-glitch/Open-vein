package com.ton.veinminer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(VeinMiner.MOD_ID)
public final class VeinMiner {
    public static final String MOD_ID = "veinminer";

    public VeinMiner() {
        MinecraftForge.EVENT_BUS.register(BlockBreakHandler.class);
    }

    public static final class BlockBreakHandler {
        private static final int MAX_BLOCKS = 64;
        private static final ThreadLocal<Boolean> VEIN_BREAK = ThreadLocal.withInitial(() -> false);

        @SubscribeEvent
        public static void onBreak(BlockEvent.BreakEvent event) {
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

            if (originState.isAir() || !net.minecraftforge.common.ForgeHooks.isCorrectToolForDrops(originState, player)) {
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
                    if (!net.minecraftforge.common.ForgeHooks.isCorrectToolForDrops(state, player)) {
                        continue;
                    }

                    if (level.destroyBlock(pos, true, player)) {
                        broken++;
                        if (!tool.isEmpty() && tool.isDamageableItem()) {
                            tool.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
                            if (tool.isEmpty()) {
                                break;
                            }
                        }
                    }
                }

                if (broken > 1) {
                    event.setCanceled(true);
                }
            } finally {
                VEIN_BREAK.set(false);
            }
        }
    }
}
