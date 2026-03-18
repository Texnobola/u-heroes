package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.ability.BoosterHandler;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import com.uheroes.mod.heroes.nanotech.ava.AVACapability;
import com.uheroes.mod.heroes.nanotech.ava.AVAData;
import com.uheroes.mod.heroes.nanotech.ava.AVAEntity;
import com.uheroes.mod.init.ModEntities;
import com.uheroes.mod.init.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;
import java.util.UUID;

/**
 * Manages the full lifecycle of the AVA entity for each player.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Spawn AVA when the player first equips the full Nano Suit.</li>
 *   <li>Respawn AVA after player death (with cooldown).</li>
 *   <li>Verify AVA is still alive each tick; re-spawn if missing.</li>
 *   <li>Gate jetpack continuous thrust (no keybind packet needed).</li>
 *   <li>Clean up AVA on player log-out.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class AVAEvents {

    // ─── Player tick: verify AVA alive, gate jetpack ──────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player.level() instanceof ServerLevel level)) return;

        Player player = event.player;

        // Jetpack: server-authoritative continuous thrust
        // We use the player's jump key state which IS synced to the server
        // via vanilla movement packets (player.jumping).
        if (NanoSuitHandler.isWearingFullNanoSuit(player) && player.jumping && !player.isOnGround()) {
            BoosterHandler.tickJetpack(player);
        }

        // AVA lifecycle — only check every 20 ticks (1 second) to save perf
        if (player.tickCount % 20 != 0) return;
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;

        player.getCapability(AVACapability.INSTANCE).ifPresent(ava -> {
            if (ava.isOnCooldown()) return;

            boolean needsSpawn = false;

            if (!ava.hasAva()) {
                needsSpawn = true;
            } else {
                // Check if the AVA entity still exists in the level
                UUID id = ava.getAvaUUID().orElseThrow();
                boolean alive = level.getEntities()
                    .getAll()
                    .anyMatch(e -> e instanceof AVAEntity && e.getUUID().equals(id) && e.isAlive());
                if (!alive) {
                    ava.clearAvaUUID();
                    needsSpawn = true;
                }
            }

            if (needsSpawn) {
                spawnAVA(player, level, ava);
            }
        });
    }

    // ─── Respawn after player death ───────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        player.getCapability(AVACapability.INSTANCE).ifPresent(ava -> {
            // Clear the old UUID — the entity is gone since the level may differ
            ava.clearAvaUUID();
            // Start cooldown so AVA respawns after a delay (not instantly on death)
            ava.startRespawnCooldown();
        });
    }

    // ─── Clean up AVA on log-out ──────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        player.getCapability(AVACapability.INSTANCE).ifPresent(ava ->
            ava.getAvaUUID().ifPresent(id ->
                level.getEntities().getAll()
                    .filter(e -> e instanceof AVAEntity && e.getUUID().equals(id))
                    .findFirst()
                    .ifPresent(e -> e.discard())
            )
        );
    }

    // ─── Public spawn helper (also called from NanoCreatureHandler) ───────────

    /**
     * Spawns AVA next to the player, links her UUID into AVAData, and plays
     * the activation sound + chat message.
     *
     * <p>Safe to call multiple times — always clears the old link first.
     */
    public static void spawnAVA(Player player, ServerLevel level, AVAData ava) {
        AVAEntity entity = ModEntities.AVA.get().create(level);
        if (entity == null) return;

        entity.setOwnerUUID(player.getUUID());
        entity.moveTo(
            player.getX() + 1.5,
            player.getY() + 1.4,
            player.getZ(),
            player.getYRot(), 0
        );
        level.addFreshEntity(entity);

        ava.setAvaUUID(entity.getUUID());

        // Feedback
        level.playSound(null, player.blockPosition(),
            ModSounds.AVA_ACTIVATE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        player.displayClientMessage(
            Component.translatable("status.u_heroes.ava_online"), true);

        UHeroesMod.LOGGER.debug("[AVA] Spawned for player {} UUID={}",
            player.getName().getString(), entity.getUUID());
    }
}