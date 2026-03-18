package com.uheroes.mod.heroes.nanotech.ability;

import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import com.uheroes.mod.init.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.uheroes.mod.UHeroesMod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side booster ability logic.
 *
 * <h2>Abilities</h2>
 * <ul>
 *   <li><b>Dash</b>     — horizontal burst in look direction. 10 Flux, 0.6s cooldown.</li>
 *   <li><b>Power Punch</b> — forward lunge + melee AOE. 12 Flux, 1s cooldown.</li>
 *   <li><b>Jetpack</b>  — vertical thrust while space is held. 1 Flux/tick (15/sec),
 *                          server-controlled via {@link #tickJetpack}.</li>
 * </ul>
 *
 * <h2>Requirements</h2>
 * All abilities require the full Nano Suit (4 pieces). Jetpack additionally
 * requires at least the chestplate equipped (but full suit gives flight).
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class BoosterHandler {

    // Flux costs
    private static final int FLUX_DASH        = 10;
    private static final int FLUX_PUNCH       = 12;
    private static final int FLUX_JETPACK_TICK = 1;   // per tick

    // Cooldowns in ticks
    private static final int CD_DASH  = 12; // 0.6s
    private static final int CD_PUNCH = 20; // 1.0s

    // Per-player cooldown maps (server-side)
    private static final Map<UUID, Integer> dashCooldowns  = new HashMap<>();
    private static final Map<UUID, Integer> punchCooldowns = new HashMap<>();

    // ─── Public triggers (called from BoosterPacket) ──────────────────────────

    /**
     * Applies a horizontal dash in the player's look direction.
     * Validates full suit + Flux + cooldown server-side.
     */
    public static void triggerDash(Player player) {
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;
        if (onCooldown(dashCooldowns, player)) return;
        if (!FluxCapability.consume(player, FLUX_DASH)) return;

        Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 current = player.getDeltaMovement();
        player.setDeltaMovement(
            current.x + look.x * 1.4,
            current.y,
            current.z + look.z * 1.4
        );
        player.hurtMarked = true; // forces client pos sync

        setCooldown(dashCooldowns, player, CD_DASH);

        if (player.level() instanceof ServerLevel sl) {
            sl.playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.WIND_CHARGE_BURST,
                SoundSource.PLAYERS, 0.7f, 1.4f);
        }
    }

    /**
     * Applies a forward lunge and damages entities in front of the player.
     */
    public static void triggerPowerPunch(Player player) {
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;
        if (onCooldown(punchCooldowns, player)) return;
        if (!FluxCapability.consume(player, FLUX_PUNCH)) return;

        Vec3 look = player.getLookAngle();

        // Lunge the player forward
        player.setDeltaMovement(
            player.getDeltaMovement().add(look.scale(0.7))
        );
        player.hurtMarked = true;

        // Hit entities in a 3-block cone ahead
        AABB hitBox = player.getBoundingBox()
            .expandTowards(look.scale(3.0))
            .inflate(0.6);

        player.level().getEntitiesOfClass(LivingEntity.class, hitBox,
            e -> e != player && e.isAlive()
        ).forEach(e -> {
            e.hurt(player.damageSources().playerAttack(player), 10.0f);
            e.setDeltaMovement(e.getDeltaMovement().add(look.scale(1.4)));
            e.hurtMarked = true;
        });

        setCooldown(punchCooldowns, player, CD_PUNCH);

        if (player.level() instanceof ServerLevel sl) {
            sl.playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.IRON_GOLEM_ATTACK,
                SoundSource.PLAYERS, 0.9f, 1.6f);
        }
    }

    // ─── Jetpack tick (server, called from NanoSuitHandler) ──────────────────

    /**
     * Called every tick from {@code NanoSuitHandler.onPlayerTick} when the
     * player has the full suit and the jetpack key is held.
     *
     * <p>The client sends its jump-key state via the vanilla jump input; the
     * server checks whether the player is in the air to gate activation.
     */
    public static void tickJetpack(Player player) {
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;
        if (player.isOnGround()) return;

        if (!FluxCapability.consume(player, FLUX_JETPACK_TICK)) {
            // Out of Flux — graceful deceleration, no instant drop
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * 0.88, motion.y * 0.78, motion.z * 0.88);
            return;
        }

        // Vertical thrust — target +0.22 m/t upward, eased
        Vec3 motion = player.getDeltaMovement();
        double targetY = player.isCrouching() ? -0.08 : 0.22;
        player.setDeltaMovement(
            motion.x,
            motion.y + (targetY - motion.y) * 0.28,
            motion.z
        );
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

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static boolean onCooldown(Map<UUID, Integer> map, Player p) {
        return map.getOrDefault(p.getUUID(), 0) > 0;
    }

    private static void setCooldown(Map<UUID, Integer> map, Player p, int ticks) {
        map.put(p.getUUID(), ticks);
    }
}