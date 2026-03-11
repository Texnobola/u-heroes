package com.uheroes.mod.origin;

import com.uheroes.mod.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class AsteroidCraterGenerator {

    private static final int CRATER_RADIUS   = 6;
    private static final int SCATTER_RADIUS  = 50;
    private static final int SCATTER_COUNT   = 50;

    public static BlockPos generate(ServerLevel level, BlockPos spawnPos) {
        Random rand = new Random();

        // Pick random direction 40-60 blocks from spawn
        double angle  = rand.nextDouble() * Math.PI * 2;
        int dist      = 40 + rand.nextInt(21);
        int cx        = spawnPos.getX() + (int)(Math.cos(angle) * dist);
        int cz        = spawnPos.getZ() + (int)(Math.sin(angle) * dist);

        // Find ground level at crater center
        int cy = level.getHeight(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);

        BlockPos craterCenter = new BlockPos(cx, cy, cz);

        // Carve sphere of radius 6
        for (int dx = -CRATER_RADIUS; dx <= CRATER_RADIUS; dx++) {
            for (int dy = -CRATER_RADIUS; dy <= CRATER_RADIUS; dy++) {
                for (int dz = -CRATER_RADIUS; dz <= CRATER_RADIUS; dz++) {
                    if (dx*dx + dy*dy + dz*dz <= CRATER_RADIUS * CRATER_RADIUS) {
                        BlockPos p = craterCenter.offset(dx, dy, dz);
                        if (!level.getBlockState(p).isAir()) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Scatter 50 Nano Alloy Blocks within 50 block radius
        BlockState nanoBlock = ModBlocks.NANO_ALLOY_BLOCK.get().defaultBlockState();
        int placed = 0;
        int attempts = 0;
        while (placed < SCATTER_COUNT && attempts < 500) {
            attempts++;
            double a   = rand.nextDouble() * Math.PI * 2;
            int r      = 5 + rand.nextInt(SCATTER_RADIUS - 4);
            int bx     = cx + (int)(Math.cos(a) * r);
            int bz     = cz + (int)(Math.sin(a) * r);
            int by     = level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);

            BlockPos blockPos = new BlockPos(bx, by, bz);
            // Only place on solid ground, not in air or water
            if (!level.getBlockState(blockPos.below()).isAir()
                    && level.getBlockState(blockPos).isAir()) {
                level.setBlock(blockPos, nanoBlock, 3);
                placed++;
            }
        }

        return craterCenter;
    }
}
