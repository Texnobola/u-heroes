package com.uheroes.mod.heroes.nanotech.ability;

import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.core.network.SeismicSlamVfxPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Seismic Slam — boots ability.
 *
 * IN AIR:  Player dives straight down at high speed.
 *          On landing, triggers full impact.
 * ON GROUND: Instant stomp — triggers impact immediately.
 *
 * Impact:
 *  - Damages + knocks back all enemies in radius
 *  - Destroys weak blocks in radius (crater)
 *  - Sends shockwave VFX packet to all clients
 *  - Sends screen shake packet to all clients
 *
 * Cost: 20 Flux. Cooldown: 2 seconds (40 ticks).
 */
public class SeismicSlamHandler {

    public static final int   FLUX_COST    = 20;
    public static final int   COOLDOWN     = 40;
    public static final float DAMAGE       = 12f;
    public static final float KNOCKBACK    = 2.0f;
    public static final int   RADIUS       = 5;   // blocks
    public static final int   CRATER_RADIUS = 3;  // smaller for crater

    // Per-player dive state — stored in a simple map
    private static final java.util.Map<java.util.UUID, Integer> diveCooldowns =
        new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Set<java.util.UUID> divingPlayers =
        java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    /** Called by BoosterPacket on the server. */
    public static void triggerSlam(ServerPlayer player) {
        // Cooldown check
        int cd = diveCooldowns.getOrDefault(player.getUUID(), 0);
        if (cd > 0) return;

        if (!FluxCapability.consume(player, FLUX_COST)) return;

        if (!player.onGround()) {
            // IN AIR — start dive: shoot player downward fast
            Vec3 vel = player.getDeltaMovement();
            player.setDeltaMovement(vel.x * 0.3, -3.5, vel.z * 0.3);
            player.hurtMarked = true;
            divingPlayers.add(player.getUUID());
            player.level().playSound(null, player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 1.8f);
        } else {
            // ON GROUND — instant stomp
            doImpact(player, player.position());
        }
        diveCooldowns.put(player.getUUID(), COOLDOWN);
    }

    /**
     * Called every server tick for players that are mid-dive.
     * Triggers impact when they hit the ground.
     * Hook into FluxEvents.onPlayerTick or a Forge tick event.
     */
    public static void tickDiving(ServerPlayer player) {
        // Decrement cooldown
        diveCooldowns.computeIfPresent(player.getUUID(), (k, v) -> v > 0 ? v - 1 : null);

        if (!divingPlayers.contains(player.getUUID())) return;

        if (player.onGround()) {
            divingPlayers.remove(player.getUUID());
            doImpact(player, player.position());
        }
    }

    // ── Impact ────────────────────────────────────────────────────────────────

    private static void doImpact(ServerPlayer player, Vec3 pos) {
        if (!(player.level() instanceof ServerLevel sl)) return;

        // 1. Damage + knockback nearby entities
        List<LivingEntity> nearby = sl.getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(RADIUS),
            e -> e != player && e.isAlive()
        );
        for (LivingEntity e : nearby) {
            Vec3 dir = e.position().subtract(pos).normalize();
            double dist = e.distanceTo(player);
            float dmg = DAMAGE * (float)(1.0 - dist / (RADIUS + 1));
            e.hurt(player.damageSources().playerAttack(player), dmg);
            double kb = KNOCKBACK * (1.0 - dist / (RADIUS + 1));
            e.setDeltaMovement(dir.x * kb, 0.8 + kb * 0.3, dir.z * kb);
            e.hurtMarked = true;
        }

        // 2. Crater — destroy weak blocks in sphere
        BlockPos center = BlockPos.containing(pos);
        for (int dx = -CRATER_RADIUS; dx <= CRATER_RADIUS; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -CRATER_RADIUS; dz <= CRATER_RADIUS; dz++) {
                    if (dx*dx + dz*dz > CRATER_RADIUS*CRATER_RADIUS) continue;
                    BlockPos bp = center.offset(dx, dy, dz);
                    BlockState state = sl.getBlockState(bp);
                    float hardness = state.getDestroySpeed(sl, bp);
                    // Only destroy weak blocks: dirt, sand, gravel, grass, snow, leaves etc.
                    if (hardness >= 0 && hardness <= 1.5f
                            && !state.isAir()
                            && state.getBlock() != Blocks.BEDROCK) {
                        sl.destroyBlock(bp, false);
                    }
                }
            }
        }

        // 3. Impact sound
        sl.playSound(null, center,
            SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.6f);

        // 4. Send VFX + screen shake to all nearby clients
        SeismicSlamVfxPacket pkt = new SeismicSlamVfxPacket(
            (float)pos.x, (float)pos.y, (float)pos.z);
        ModNetwork.sendToAllTracking(pkt, player);
        ModNetwork.sendToPlayer(pkt, player); // also to self
    }
}