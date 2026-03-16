package com.uheroes.mod.origin;

import com.uheroes.mod.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class AsteroidCraterGenerator {

    private static final int CRATER_RADIUS  = 6;
    private static final int SCATTER_RADIUS = 50;
    private static final int SCATTER_COUNT  = 50;

    /**
     * Picks a random crater position near spawnPos without generating anything.
     * Called at first login to determine where the asteroid will land.
     */
    public static BlockPos pickCraterPos(ServerLevel level, BlockPos spawnPos) {
        Random rand = new Random();
        double angle = rand.nextDouble() * Math.PI * 2;
        int dist     = 40 + rand.nextInt(21);
        int cx       = spawnPos.getX() + (int)(Math.cos(angle) * dist);
        int cz       = spawnPos.getZ() + (int)(Math.sin(angle) * dist);
        int cy       = level.getHeight(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        return new BlockPos(cx, cy, cz);
    }

    /**
     * Called by AsteroidEntity on impact.
     * Carves the crater sphere AND scatters Nano Alloy blocks around it.
     */
    public static void impactAt(ServerLevel level, BlockPos craterCenter) {
        carveCrater(level, craterCenter);
        scatterNanoAlloy(level, craterCenter);
    }

    private static void carveCrater(ServerLevel level, BlockPos center) {
        for (int dx = -CRATER_RADIUS; dx <= CRATER_RADIUS; dx++) {
            for (int dy = -CRATER_RADIUS; dy <= CRATER_RADIUS; dy++) {
                for (int dz = -CRATER_RADIUS; dz <= CRATER_RADIUS; dz++) {
                    if (dx*dx + dy*dy + dz*dz <= CRATER_RADIUS * CRATER_RADIUS) {
                        BlockPos p = center.offset(dx, dy, dz);
                        if (!level.getBlockState(p).isAir()) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    private static void scatterNanoAlloy(ServerLevel level, BlockPos center) {
        Random rand = new Random();
        BlockState nanoBlock = ModBlocks.NANO_ALLOY_BLOCK.get().defaultBlockState();
        int placed   = 0;
        int attempts = 0;

        while (placed < SCATTER_COUNT && attempts < 500) {
            attempts++;
            double a = rand.nextDouble() * Math.PI * 2;
            int r    = 5 + rand.nextInt(SCATTER_RADIUS - 4);
            int bx   = center.getX() + (int)(Math.cos(a) * r);
            int bz   = center.getZ() + (int)(Math.sin(a) * r);
            int by   = level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);

            BlockPos pos = new BlockPos(bx, by, bz);
            if (!level.getBlockState(pos.below()).isAir() && level.getBlockState(pos).isAir()) {
                level.setBlock(pos, nanoBlock, 3);
                placed++;
            }
        }
    }
}