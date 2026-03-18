package com.uheroes.mod.heroes.nanotech.ava;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * AVA VFX — three Effekseer effects for the AI companion system.
 *
 * <h2>Effect Sources (from official Effekseer sample pack)</h2>
 * <pre>
 * ava_orbit_trail.efkefc   = Aura01.efkefc   (4-layer glowing aura rings)
 *   └─ Textures/Aura02_T.png, Aura04_T.png, Normal01.png, Particle01.png
 *
 * ava_shield_pulse.efkefc  = Barrior02.efkefc (multi-layer animated barrier)
 *   └─ Textures/Aura01_T, Aura02_T, Aura04_T, Aura06_T, Normal01.png
 *
 * ava_block_deflect.efkefc = Simple_Ring_Shape1.efkefc (no-texture ring burst)
 *   └─ no textures needed
 * </pre>
 *
 * <h2>All textures</h2>
 * Copied to {@code assets/u_heroes/effeks/Textures/} — same relative folder
 * the .efkefc files expect. Effekseer resolves paths relative to the effect file.
 *
 * <h2>Usage</h2>
 * All methods are client-side only. Call inside {@code level.isClientSide()} guards
 * or from a {@code @Mod.EventBusSubscriber(value = Dist.CLIENT)} handler.
 */
public class AVAEffects {

    // ─── Emitter descriptors ─────────────────────────────────────────────────

    /** Aura01 — 4-layer glowing ring aura. Soft, persistent glow trail. */
    private static final ParticleEmitterInfo ORBIT_TRAIL = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_orbit_trail")
    );

    /** Barrior02 — multi-layer animated force barrier. Instantaneous ring burst. */
    private static final ParticleEmitterInfo SHIELD_PULSE = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_shield_pulse")
    );

    /** Simple_Ring_Shape1 — clean geometric ring flash. Sharp deflect spark. */
    private static final ParticleEmitterInfo BLOCK_DEFLECT = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "ava_block_deflect")
    );

    // ─── Public spawn methods ─────────────────────────────────────────────────

    /**
     * Spawns AVA's orbital glow trail at her current position.
     *
     * <p>Based on Aura01 — a four-layer animated aura effect. Use scale 0.3–0.6
     * so it fits around a single entity rather than filling a room.
     *
     * <p>Call every 4–6 ticks while AVA is moving (client-side).
     *
     * @param ava   the AVA entity (position source)
     * @param yaw   AVA's current yaw in radians
     * @param pitch AVA's current pitch in radians
     */
    public static void spawnOrbitTrail(LivingEntity ava, float yaw, float pitch) {
        Vec3 pos = ava.position().add(0, ava.getBbHeight() * 0.5, 0);
        AAALevel.addParticle(
            ava.level(), false,
            ORBIT_TRAIL.clone()
                .position(pos.x, pos.y, pos.z)
                .rotation(pitch, yaw, 0f)
                .scale(0.35f)   // Aura01 is large by default — scale down for entity-size
        );
    }

    /**
     * Spawns the shield activation burst at AVA's centre.
     *
     * <p>Based on Barrior02 — a multi-layered barrier ring with inner/outer
     * glow. Fires four rings at 90° intervals to fake a sphere.
     *
     * <p>Call once when the shield key is pressed or the barrier expands.
     *
     * @param ava   the AVA entity
     * @param scale 1.0 = default ring, 2.5 = full expanded barrier sphere
     */
    public static void spawnShieldPulse(LivingEntity ava, float scale) {
        Vec3 pos = ava.position().add(0, ava.getBbHeight() * 0.5, 0);
        // Four rings at 90° rotations → fakes a complete sphere
        for (int i = 0; i < 4; i++) {
            float roll = (float) Math.toRadians(i * 90.0);
            AAALevel.addParticle(
                ava.level(), false,
                SHIELD_PULSE.clone()
                    .position(pos.x, pos.y, pos.z)
                    .rotation(0f, roll, roll)
                    .scale(scale * 0.4f)  // Barrior02 is designed for large scenes
            );
        }
    }

    /**
     * Spawns a sharp deflect ring at AVA's position when she intercepts
     * a projectile or blocks an attack.
     *
     * <p>Based on Simple_Ring_Shape1 — a clean geometric ring flash with
     * no texture dependency. The ring expands and fades instantly.
     *
     * <p>Primary ring fires in the direction of the blocked hit, plus three
     * 45°-offset scatter rings for a burst feel.
     *
     * @param ava    the AVA entity
     * @param hitDir normalised direction FROM AVA TOWARD the blocked threat
     */
    public static void spawnBlockDeflect(LivingEntity ava, Vec3 hitDir) {
        Vec3 pos = ava.getEyePosition();

        float yaw   = (float) Math.atan2(hitDir.x, hitDir.z);
        float pitch = (float) -Math.asin(Math.max(-1, Math.min(1, hitDir.y)));

        // Primary large ring facing the impact direction
        AAALevel.addParticle(
            ava.level(), false,
            BLOCK_DEFLECT.clone()
                .position(pos.x, pos.y, pos.z)
                .rotation(pitch, yaw, 0f)
                .scale(1.5f)
        );

        // Three smaller scatter rings at 45° / 90° / 135° roll offsets
        for (int i = 1; i <= 3; i++) {
            float roll = (float) Math.toRadians(i * 45.0);
            AAALevel.addParticle(
                ava.level(), false,
                BLOCK_DEFLECT.clone()
                    .position(pos.x, pos.y, pos.z)
                    .rotation(pitch, yaw, roll)
                    .scale(0.8f)
            );
        }
    }

    // ─── Prevent instantiation ────────────────────────────────────────────────
    private AVAEffects() {}
}