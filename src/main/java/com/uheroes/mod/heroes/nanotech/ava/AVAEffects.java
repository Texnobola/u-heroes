package com.uheroes.mod.heroes.nanotech.ava;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * AVA client-side VFX helpers.
 *
 * Note: blaster muzzle and hit VFX are sent via AVAVfxPacket (server→all clients).
 * Methods here are for effects triggered directly on the entity (orbit trail, shield).
 */
public class AVAEffects {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("U-Heroes/AVAEffects");



    private static final ParticleEmitterInfo ORBIT_TRAIL = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_orbit_trail")
    );
    private static final ParticleEmitterInfo SHIELD_PULSE = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_shield_pulse")
    );
    private static final ParticleEmitterInfo BLOCK_DEFLECT = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_block_deflect")
    );
    private static final ParticleEmitterInfo BLASTER = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_blaster")
    );

    /** Tiny comet tail — every 3 ticks while orbiting. */
    public static void spawnOrbitTrail(LivingEntity ava, float yaw, float pitch) {
        LOGGER.debug("[AVA-VFX] Playing: ava_orbit_trail @ {}", ava.position());
        Vec3 pos = ava.position().add(0, ava.getBbHeight() * 0.5, 0);
        logFire("ava_orbit_trail", 0.04f);
        AAALevel.addParticle(ava.level(), false,
            ORBIT_TRAIL.clone()
                .position(pos.x, pos.y, pos.z)
                .rotation(pitch, yaw, 0f)
                .scale(0.04f));
    }

    /** Shield barrier rings — 2 perpendicular rings. */
    public static void spawnShieldPulse(LivingEntity ava, float scale) {
        LOGGER.debug("[AVA-VFX] Playing: ava_shield_pulse scale={}", scale * 0.15f);
        Vec3 pos = ava.position().add(0, ava.getBbHeight() * 0.5, 0);
        for (int i = 0; i < 2; i++) {
            float roll = (float) Math.toRadians(i * 90.0);
            logFire("ava_shield_pulse", scale * 0.15f);
            AAALevel.addParticle(ava.level(), false,
                SHIELD_PULSE.clone()
                    .position(pos.x, pos.y, pos.z)
                    .rotation(0f, roll, roll)
                    .scale(scale * 0.15f));
        }
    }

    /**
     * Deflect ring at a world position — called from AVAVfxPacket on the client.
     * @param level  client level
     * @param pos    world position
     * @param dir    normalised hit direction
     * @param scale  ring size
     */
    public static void spawnDeflect(Level level, Vec3 pos, Vec3 dir, float scale) {
        LOGGER.debug("[AVA-VFX] Playing: ava_block_deflect scale={} @ {}", scale, pos);
        float yaw   = (float) Math.atan2(dir.x, dir.z);
        float pitch = (float) -Math.asin(Math.max(-1, Math.min(1, dir.y)));
        logFire("ava_block_deflect", scale);
        AAALevel.addParticle(level, false,
            BLOCK_DEFLECT.clone()
                .position(pos.x, pos.y, pos.z)
                .rotation(pitch, yaw, 0f)
                .scale(scale));
    }

    /**
     * Laser beam — called from AVAVfxPacket on the client.
     * @param level  client level
     * @param from   muzzle world position
     * @param dir    normalised direction toward target
     */
    public static void spawnBlaster(Level level, Vec3 from, Vec3 dir) {
        LOGGER.debug("[AVA-VFX] Playing: ava_blaster @ {}", from);
        float yaw   = (float) Math.atan2(dir.x, dir.z);
        float pitch = (float) -Math.asin(Math.max(-1, Math.min(1, dir.y)));
        logFire("ava_blaster", 0.06f);
        AAALevel.addParticle(level, false,
            BLASTER.clone()
                .position(from.x, from.y, from.z)
                .rotation(pitch, yaw, 0f)
                .scale(0.06f));
    }

    private static void logFire(String effect, float scale) {
        LOGGER.debug("[AVA VFX] Playing: {}.efkefc  scale={}", effect, scale);
    }

    private AVAEffects() {}
}