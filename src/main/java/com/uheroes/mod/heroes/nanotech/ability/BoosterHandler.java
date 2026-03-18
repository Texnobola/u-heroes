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

/**
 * Server-side booster ability logic.
 * Jetpack state is tracked here and set via BoosterPacket (JETPACK_ON/OFF)
 * because player.jumping is protected in LivingEntity and unreachable from here.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class BoosterHandler {

    private static final int FLUX_DASH         = 10;
    private static final int FLUX_PUNCH        = 12;
    private static final int FLUX_JETPACK_TICK = 1;

    private static final int CD_DASH  = 12;
    private static final int CD_PUNCH = 20;

    private static final Map<UUID, Integer> dashCooldowns   = new HashMap<>();
    private static final Map<UUID, Integer> punchCooldowns  = new HashMap<>();
    /** Server-side jetpack active flags — set by client packets */
    private static final Map<UUID, Boolean> jetpackActive   = new HashMap<>();

    // ─── Jetpack state (set from BoosterPacket) ───────────────────────────────

    public static void setJetpackActive(UUID playerId, boolean active) {
        if (active) jetpackActive.put(playerId, true);
        else        jetpackActive.remove(playerId);
    }

    public static boolean isJetpackActive(Player player) {
        return jetpackActive.getOrDefault(player.getUUID(), false);
    }

    // ─── Ability triggers ─────────────────────────────────────────────────────

    public static void triggerDash(Player player) {
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;
        if (onCooldown(dashCooldowns, player)) return;
        if (!FluxCapability.consume(player, FLUX_DASH)) return;

        Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 cur  = player.getDeltaMovement();
        player.setDeltaMovement(cur.x + look.x * 1.4, cur.y, cur.z + look.z * 1.4);
        player.hurtMarked = true;

        setCooldown(dashCooldowns, player, CD_DASH);

        if (player.level() instanceof ServerLevel sl) {
            // SoundEvents.WIND_CHARGE_BURST added in 1.21 — use TRIDENT_THROW for 1.20.1
            sl.playSound(null, player.blockPosition(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8f, 1.5f);
        }
    }

    public static void triggerPowerPunch(Player player) {
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;
        if (onCooldown(punchCooldowns, player)) return;
        if (!FluxCapability.consume(player, FLUX_PUNCH)) return;

        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(player.getDeltaMovement().add(look.scale(0.7)));
        player.hurtMarked = true;

        AABB hitBox = player.getBoundingBox().expandTowards(look.scale(3.0)).inflate(0.6);
        player.level().getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())
            .forEach(e -> {
                e.hurt(player.damageSources().playerAttack(player), 10.0f);
                e.setDeltaMovement(e.getDeltaMovement().add(look.scale(1.4)));
                e.hurtMarked = true;
            });

        setCooldown(punchCooldowns, player, CD_PUNCH);

        if (player.level() instanceof ServerLevel sl) {
            sl.playSound(null, player.blockPosition(),
                SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 0.9f, 1.6f);
        }
    }

    /** Called every tick from NanoSuitHandler when jetpackActive[player] == true. */
    public static void tickJetpack(Player player) {
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;
        if (player.onGround()) return;   // onGround() — correct 1.20.1 spelling

        if (!FluxCapability.consume(player, FLUX_JETPACK_TICK)) {
            Vec3 m = player.getDeltaMovement();
            player.setDeltaMovement(m.x * 0.88, m.y * 0.78, m.z * 0.88);
            return;
        }

        Vec3 m = player.getDeltaMovement();
        double targetY = player.isCrouching() ? -0.08 : 0.22;
        player.setDeltaMovement(m.x, m.y + (targetY - m.y) * 0.28, m.z);
        player.fallDistance = 0;
        player.resetFallDistance();
    }

    // ─── Per-tick cooldown decay ──────────────────────────────────────────────

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
    private static void setCooldown(Map<UUID, Integer> map, Player p, int ticks) {
        map.put(p.getUUID(), ticks);
    }
}