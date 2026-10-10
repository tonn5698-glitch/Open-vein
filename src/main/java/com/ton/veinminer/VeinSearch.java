package com.ton.veinminer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

final class VeinSearch {
    private VeinSearch() {}

    static ArrayList<BlockPos> collect(LevelAccessor level, BlockPos start, Block target, int limit) {
        var result = new ArrayList<BlockPos>();
        var queue = new ArrayDeque<BlockPos>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(start.immutable());
        visited.add(start.immutable());

        while (!queue.isEmpty() && result.size() < limit) {
            var pos = queue.removeFirst();
            if (level.getBlockState(pos).getBlock() != target) {
                continue;
            }

            result.add(pos);

            for (Direction direction : Direction.values()) {
                if (direction.getAxis().isVertical() || direction.getAxis().isHorizontal()) {
                    var next = pos.relative(direction);
                    if (visited.add(next.immutable()) && level.getBlockState(next).getBlock() == target) {
                        queue.addLast(next.immutable());
                    }
                }
            }
        }

        return result;
    }
}
