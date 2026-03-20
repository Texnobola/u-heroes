package com.uheroes.mod.heroes.nanotech.ability;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import net.minecraft.server.level.ServerLevel;
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

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class BoosterHandler {

    private static final int FLUX_DASH         = 10;
    private static final int FLUX_PUNCH        = 12;
    private static final int FLUX_JETPACK_TICK = 1;

    private static final int CD_DASH  = 12;
    private static final int CD_PUNCH = 20;

    private static final Map<UUID, Integer> dashCooldowns  = new HashMap<>();
    private static final Map<UUID, Integer> punchCooldowns = new HashMap<>();
    private static final Map<UUID, Boolean> jetpackActive  = new HashMap<>();

    // ─── Jetpack state ────────────────────────────────────────────────────────

    public static void setJetpackActive(UUID id, boolean active) {
        if (active) jetpackActive.put(id, true);
        else        jetpackActive.remove(id);
    }

    public static boolean isJetpackActive(Player player) {
        return jetpackActive.getOrDefault(player.getUUID(), false);
    }

    // ─── Dash ─────────────────────────────────────────────────────────────────

    public static void triggerDash(Player player) {
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;
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

    // ─── Power Punch ─────────────────────────────────────────────────────────

    /**
     * Brutally launches enemies in the look direction.
     * Horizontal scale 4.5, upward component 1.2 — throws them across the arena.
     */
    public static void triggerPowerPunch(Player player) {
        // Play punch animation on the local client
        if (player.level().isClientSide() && player instanceof net.minecraft.client.player.LocalPlayer lp) {
            com.uheroes.mod.client.animation.NanoSuitWalkAnimHandler.triggerPunchAnim(lp);
        }
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;
        if (onCooldown(punchCooldowns, player)) return;
        if (!FluxCapability.consume(player, FLUX_PUNCH)) return;

        Vec3 look = player.getLookAngle();
        // Player lunge
        player.setDeltaMovement(player.getDeltaMovement().add(look.scale(0.8)));
        player.hurtMarked = true;

        // Hit entities in a 4-block cone
        AABB hitBox = player.getBoundingBox()
            .expandTowards(look.scale(4.0)).inflate(0.8);

        player.level().getEntitiesOfClass(LivingEntity.class, hitBox,
            e -> e != player && e.isAlive()
        ).forEach(e -> {
            e.hurt(player.damageSources().playerAttack(player), 14.0f);
            // Massive launch: horizontal 4.5 + upward 1.2
            Vec3 launch = look.multiply(4.5, 0, 4.5).add(0, 1.2, 0);
            e.setDeltaMovement(launch);
            e.hurtMarked = true;
        });

        setCooldown(punchCooldowns, player, CD_PUNCH);

        if (player.level() instanceof ServerLevel sl)
            sl.playSound(null, player.blockPosition(),
                SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    // ─── Jetpack ──────────────────────────────────────────────────────────────

    /**
     * Direct Y-velocity override every tick — no lerp, no gravity accumulation.
     * Player holds Jetpack key (Space) → stays at fixed upward velocity.
     * Sneak → descend. Gravity is defeated by overriding Y each tick directly.
     */
    public static void tickJetpack(Player player) {
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;
        if (player.onGround()) return;

        if (!FluxCapability.consume(player, FLUX_JETPACK_TICK)) {
            // Out of flux — graceful deceleration
            Vec3 m = player.getDeltaMovement();
            player.setDeltaMovement(m.x * 0.90, m.y * 0.80, m.z * 0.90);
            return;
        }

        Vec3 m = player.getDeltaMovement();
        // Direct set — overrides gravity completely each tick
        double vy = player.isCrouching() ? -0.20 : 0.28;
        player.setDeltaMovement(m.x, vy, m.z);
        player.fallDistance = 0;
        player.resetFallDistance();
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