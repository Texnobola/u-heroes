package com.uheroes.mod.heroes.nanotech.ava;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * AVA VFX — Effekseer effects.
 *
 * Scale guide (empirical):
 *  - Simple_Track1 (orbit trail):   0.04  → tiny sparkling comet tail
 *  - Simple_Ring_Shape1 (deflect):  0.08  → compact ring flash
 *  - Laser01 (blaster):             0.06  → tight beam shot
 *  - Simple_Ring_Shape1 (shield):   0.15  → moderate barrier ring
 */
public class AVAEffects {

    private static final ParticleEmitterInfo ORBIT_TRAIL = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_orbit_trail")   // Simple_Track1
    );
    private static final ParticleEmitterInfo SHIELD_PULSE = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_shield_pulse")  // Simple_Ring_Shape1
    );
    private static final ParticleEmitterInfo BLOCK_DEFLECT = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_block_deflect") // Simple_Ring_Shape1
    );
    private static final ParticleEmitterInfo BLASTER = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_blaster")       // Laser01
    );

    // ─── Orbit trail ──────────────────────────────────────────────────────────

    /**
     * Small sparkling comet tail — spawned every 3 ticks while orbiting.
     * Scale 0.04 keeps it tiny and tight around AVA's body.
     */
    public static void spawnOrbitTrail(LivingEntity ava, float yaw, float pitch) {
        Vec3 pos = ava.position().add(0, ava.getBbHeight() * 0.5, 0);
        AAALevel.addParticle(
            ava.level(), false,
            ORBIT_TRAIL.clone()
                .position(pos.x, pos.y, pos.z)
                .rotation(pitch, yaw, 0f)
                .scale(0.04f)
        );
    }

    // ─── Shield pulse ─────────────────────────────────────────────────────────

    /**
     * Two perpendicular rings for a shield barrier feel.
     * scale=0.15 on activation, 0.10 for sustained pulses.
     */
    public static void spawnShieldPulse(LivingEntity ava, float scale) {
        Vec3 pos = ava.position().add(0, ava.getBbHeight() * 0.5, 0);
        // Two rings at 0° and 90° — gives a cross/sphere hint without overdoing it
        for (int i = 0; i < 2; i++) {
            float roll = (float) Math.toRadians(i * 90.0);
            AAALevel.addParticle(
                ava.level(), false,
                SHIELD_PULSE.clone()
                    .position(pos.x, pos.y, pos.z)
                    .rotation(0f, roll, roll)
                    .scale(scale * 0.15f)
            );
        }
    }

    // ─── Block deflect ────────────────────────────────────────────────────────

    /**
     * Single compact ring facing the impact direction.
     * Scale 0.08 — tight, sharp, doesn't fill the screen.
     */
    public static void spawnBlockDeflect(LivingEntity ava, Vec3 hitDir) {
        Vec3 pos  = ava.getEyePosition();
        float yaw   = (float) Math.atan2(hitDir.x, hitDir.z);
        float pitch = (float) -Math.asin(Math.max(-1, Math.min(1, hitDir.y)));

        AAALevel.addParticle(
            ava.level(), false,
            BLOCK_DEFLECT.clone()
                .position(pos.x, pos.y, pos.z)
                .rotation(pitch, yaw, 0f)
                .scale(0.08f)
        );
    }

    // ─── Blaster shot ─────────────────────────────────────────────────────────

    /**
     * Laser beam shot from AVA toward her target.
     * Laser01 is a straight beam with muzzle flash — fires in the look direction.
     * Scale 0.06 = tight beam that looks like a blaster bolt.
     *
     * @param from  muzzle position (AVA eye)
     * @param dir   normalised direction toward target
     */
    public static void spawnBlasterShot(LivingEntity ava, Vec3 from, Vec3 dir) {
        float yaw   = (float) Math.atan2(dir.x, dir.z);
        float pitch = (float) -Math.asin(Math.max(-1, Math.min(1, dir.y)));

        AAALevel.addParticle(
            ava.level(), false,
            BLASTER.clone()
                .position(from.x, from.y, from.z)
                .rotation(pitch, yaw, 0f)
                .scale(0.06f)
        );
    }

    /**
     * Impact ring at an explicit world position — used by the blaster bolt on hit.
     */
    public static void spawnBlockDeflect(Object unused, Vec3 hitDir, Vec3 pos, net.minecraft.world.level.Level level) {
        float yaw   = (float) Math.atan2(hitDir.x, hitDir.z);
        float pitch = (float) -Math.asin(Math.max(-1, Math.min(1, hitDir.y)));
        AAALevel.addParticle(
            level, false,
            BLOCK_DEFLECT.clone()
                .position(pos.x, pos.y, pos.z)
                .rotation(pitch, yaw, 0f)
                .scale(0.06f)
        );
    }

    private AVAEffects() {}
}