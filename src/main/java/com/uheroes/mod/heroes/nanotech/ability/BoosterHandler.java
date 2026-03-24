package com.uheroes.mod.heroes.nanotech.ability;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.core.network.JetpackVfxPacket;
import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class BoosterHandler {

    private static final int FLUX_DASH         = 10;
    private static final int FLUX_PUNCH        = 12;
    private static final int FLUX_JETPACK_TICK = 1;

    private static final int CD_DASH  = 12;
    private static final int CD_PUNCH = 20;

    private static final Map<UUID, Integer> dashCooldowns  = new HashMap<>();
    private static final Map<UUID, Integer> punchCooldowns = new HashMap<>();

    // ─── Flight phase ─────────────────────────────────────────────────────────

    public enum FlightPhase { IDLE, CHARGING, THRUSTING, CRUISING, GLIDING }

    private static final Map<UUID, FlightPhase> flightPhase   = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer>     flightTicks   = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean>     jetpackActive = new ConcurrentHashMap<>();

    /** C2S: Space pressed or released. */
    public static void setJetpackActive(UUID id, boolean active) {
        if (active) jetpackActive.put(id, true);
        else        jetpackActive.remove(id);
    }

    public static boolean isJetpackActive(Player player) {
        return jetpackActive.getOrDefault(player.getUUID(), false);
    }

    public static FlightPhase getFlightPhase(UUID id) {
        return flightPhase.getOrDefault(id, FlightPhase.IDLE);
    }

    // ─── Dash ─────────────────────────────────────────────────────────────────

    public static void triggerDash(Player player) {
        if (!NanoSuitHandler.isWearingNanoLeggings(player)) return;
        if (onCooldown(dashCooldowns, player)) return;
        if (!FluxCapability.consume(player, FLUX_DASH)) return;

        Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 cur  = player.getDeltaMovement();
        player.setDeltaMovement(cur.x + look.x * 1.6, cur.y + 0.1, cur.z + look.z * 1.6);
        player.hurtMarked = true;
        setCooldown(dashCooldowns, player, CD_DASH);

        if (player.level() instanceof ServerLevel sl)
            sl.playSound(null, player.blockPosition(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8f, 1.5f);
    }

    // ─── Power Punch ──────────────────────────────────────────────────────────

    public static void triggerPowerPunch(Player player) {
        if (!NanoSuitHandler.isWearingNanoLeggings(player)) return;
        if (onCooldown(punchCooldowns, player)) return;
        if (!FluxCapability.consume(player, FLUX_PUNCH)) return;

        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(player.getDeltaMovement().add(look.scale(0.8)));
        player.hurtMarked = true;

        AABB hitBox = player.getBoundingBox().expandTowards(look.scale(4.0)).inflate(0.8);
        player.level().getEntitiesOfClass(LivingEntity.class, hitBox,
            e -> e != player && e.isAlive()
        ).forEach(e -> {
            e.hurt(player.damageSources().playerAttack(player), 14.0f);
            e.setDeltaMovement(look.multiply(4.5, 0, 4.5).add(0, 1.2, 0));
            e.hurtMarked = true;
        });
        setCooldown(punchCooldowns, player, CD_PUNCH);

        if (player.level() instanceof ServerLevel sl)
            sl.playSound(null, player.blockPosition(),
                SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    // ─── Jetpack (epic Falcon-style) ──────────────────────────────────────────
    //
    //  CHARGING  (ground, Space held, 0-15 ticks) — thrusters warm up, player crouches
    //  THRUSTING (ticks 16-25) — burst launch, player rockets upward
    //  CRUISING  (air, Space held) — look-steered horizontal flight, Space = altitude hold
    //  GLIDING   (Space released in air) — smooth deceleration, gentle descent
    //  IDLE      — on ground, no thrust

    private static final int CHARGE_TICKS  = 15;
    private static final int THRUST_TICKS  = 10;

    public static void tickJetpack(Player player) {
        if (!NanoSuitHandler.isWearingNanoChestplate(player)) {
            stopJetpack(player);
            return;
        }

        UUID id = player.getUUID();
        boolean spaceHeld = jetpackActive.getOrDefault(id, false);
        boolean onGround  = player.onGround();
        FlightPhase phase = flightPhase.getOrDefault(id, FlightPhase.IDLE);
        int ticks = flightTicks.merge(id, 1, Integer::sum);

        // Always zero fall distance while system is active
        player.fallDistance = 0;
        player.resetFallDistance();

        // ── Phase transitions ──────────────────────────────────────────────
        if (!spaceHeld && phase != FlightPhase.IDLE) {
            if (onGround) {
                endFlight(player);
                return;
            } else {
                // Released in air → glide
                if (phase != FlightPhase.GLIDING) {
                    flightPhase.put(id, FlightPhase.GLIDING);
                    flightTicks.put(id, 0);
                }
            }
        }

        if (spaceHeld && onGround && phase == FlightPhase.IDLE) {
            flightPhase.put(id, FlightPhase.CHARGING);
            flightTicks.put(id, 0);
            ticks = 0;
        }

        if (phase == FlightPhase.CHARGING && ticks >= CHARGE_TICKS) {
            flightPhase.put(id, FlightPhase.THRUSTING);
            flightTicks.put(id, 0);
            ticks = 0;
            // Blast off sound
            if (player.level() instanceof ServerLevel sl)
                sl.playSound(null, player.blockPosition(),
                    SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.2f, 0.7f);
        }

        if (phase == FlightPhase.THRUSTING && ticks >= THRUST_TICKS) {
            flightPhase.put(id, FlightPhase.CRUISING);
            flightTicks.put(id, 0);
        }

        if (phase == FlightPhase.CRUISING && onGround) {
            endFlight(player);
            return;
        }

        // Re-read phase after transitions
        phase = flightPhase.getOrDefault(id, FlightPhase.IDLE);
        if (phase == FlightPhase.IDLE) return;

        // ── Flux cost ──────────────────────────────────────────────────────
        boolean hasFlux = FluxCapability.consume(player, FLUX_JETPACK_TICK);
        if (!hasFlux && phase != FlightPhase.GLIDING) {
            if (!onGround) {
                flightPhase.put(id, FlightPhase.GLIDING);
                flightTicks.put(id, 0);
            } else {
                endFlight(player);
                return;
            }
        }

        Vec3 m    = player.getDeltaMovement();
        Vec3 look = player.getLookAngle();

        switch (phase) {
            case CHARGING -> {
                // Vibrate slightly, crouch pose — no movement
                float prog = ticks / (float) CHARGE_TICKS;
                player.setDeltaMovement(0, 0, 0);
                player.setNoGravity(true);
                // Send charge VFX every 2 ticks
                if (ticks % 2 == 0 && player instanceof ServerPlayer sp)
                    ModNetwork.sendToAllTracking(new JetpackVfxPacket(
                        JetpackVfxPacket.Type.CHARGE, (float)player.getX(),
                        (float)player.getY(), (float)player.getZ(), prog), sp);
            }
            case THRUSTING -> {
                // Explosive upward launch
                float prog = ticks / (float) THRUST_TICKS;
                double vy = 1.8 - prog * 0.6; // 1.8 → 1.2 over thrust phase
                player.setDeltaMovement(m.x * 0.6, vy, m.z * 0.6);
                player.setNoGravity(true);
                if (player instanceof ServerPlayer sp)
                    ModNetwork.sendToAllTracking(new JetpackVfxPacket(
                        JetpackVfxPacket.Type.THRUST, (float)player.getX(),
                        (float)player.getY(), (float)player.getZ(), 1.0f), sp);
            }
            case CRUISING -> {
                // Full Falcon flight: look-steered, WASD horizontal, Space = hold altitude
                double hSpeed = look.horizontalDistance();
                double targetVy;

                if (player.isCrouching()) {
                    // Sneak = fast descent
                    targetVy = -0.55;
                } else if (spaceHeld) {
                    // Space held = pitch-steered: look up → gain altitude, look down → lose
                    // Pitch range: -90 (up) to +90 (down)
                    float pitch = player.getXRot(); // -90=up, +90=down in MC
                    // Map pitch: up = positive vy, down = negative
                    double pitchFactor = -pitch / 90.0; // -1 to 1
                    targetVy = pitchFactor * 0.8; // max 0.8 up / 0.8 down
                } else {
                    targetVy = -0.08; // gentle drift down
                }

                // Horizontal: follow look direction smoothly
                double hBoost = 0.22;
                double nx = m.x * 0.78 + look.x * hBoost;
                double nz = m.z * 0.78 + look.z * hBoost;
                // Cap horizontal speed
                double hMag = Math.sqrt(nx*nx + nz*nz);
                if (hMag > 1.4) { nx = nx/hMag*1.4; nz = nz/hMag*1.4; }

                player.setDeltaMovement(nx, targetVy, nz);
                player.setNoGravity(true);

                // Thruster trail VFX every 3 ticks
                if (ticks % 3 == 0 && player instanceof ServerPlayer sp)
                    ModNetwork.sendToAllTracking(new JetpackVfxPacket(
                        JetpackVfxPacket.Type.CRUISE, (float)player.getX(),
                        (float)player.getY(), (float)player.getZ(), 1.0f), sp);

                // Thruster sound every 12 ticks
                if (ticks % 12 == 1 && player.level() instanceof ServerLevel sl)
                    sl.playSound(null, player.blockPosition(),
                        SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.25f, 2.0f);
            }
            case GLIDING -> {
                // Smooth deceleration, gentle descent
                double nx = m.x * 0.94;
                double ny = Math.max(m.y - 0.04, -0.35);
                double nz = m.z * 0.94;
                player.setDeltaMovement(nx, ny, nz);
                player.setNoGravity(false);
                if (onGround) { endFlight(player); }
            }
        }
    }

    public static void stopJetpack(Player player) {
        UUID id = player.getUUID();
        FlightPhase phase = flightPhase.getOrDefault(id, FlightPhase.IDLE);
        if (phase != FlightPhase.IDLE) {
            // If in air, transition to glide instead of hard stop
            if (!player.onGround() && phase != FlightPhase.CHARGING) {
                flightPhase.put(id, FlightPhase.GLIDING);
                flightTicks.put(id, 0);
            } else {
                endFlight(player);
            }
        }
    }

    private static void endFlight(Player player) {
        UUID id = player.getUUID();
        flightPhase.remove(id);
        flightTicks.remove(id);
        player.setNoGravity(false);
    }

    // ─── Cooldown tick ────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        UUID id = event.player.getUUID();
        dashCooldowns.computeIfPresent(id,  (k, v) -> v > 0 ? v - 1 : null);
        punchCooldowns.computeIfPresent(id, (k, v) -> v > 0 ? v - 1 : null);
    }

    private static boolean onCooldown(Map<UUID, Integer> map, Player p) {
        return map.getOrDefault(p.getUUID(), 0) > 0;
    }
    private static void setCooldown(Map<UUID, Integer> map, Player p, int t) {
        map.put(p.getUUID(), t);
    }
}