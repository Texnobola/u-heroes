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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class AVAEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player.level() instanceof ServerLevel level)) return;

        Player player = event.player;

        // Jetpack: driven by client packet (JETPACK_ON/OFF) stored in BoosterHandler.
        // player.jumping is protected in LivingEntity — cannot be read from here.
        if (NanoSuitHandler.isWearingFullNanoSuit(player)
                && BoosterHandler.isJetpackActive(player)
                && !player.onGround()) {       // onGround() — correct 1.20.1 method name
            BoosterHandler.tickJetpack(player);
        }

        if (player.tickCount % 20 != 0) return;
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;

        player.getCapability(AVACapability.INSTANCE).ifPresent(ava -> {
            if (ava.isOnCooldown()) return;

            if (!ava.hasAva()) {
                spawnAVA(player, level, ava);
                return;
            }

            // Check if linked AVA entity is still alive.
            // level.getEntities().getAll() returns Iterable<Entity> — not a Stream.
            // Use a plain for-loop instead of .anyMatch().
            UUID id = ava.getAvaUUID().orElseThrow();
            boolean alive = false;
            for (Entity e : level.getEntities().getAll()) {
                if (e instanceof AVAEntity && e.getUUID().equals(id) && e.isAlive()) {
                    alive = true;
                    break;
                }
            }
            if (!alive) {
                ava.clearAvaUUID();
                spawnAVA(player, level, ava);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getCapability(AVACapability.INSTANCE).ifPresent(ava -> {
            ava.clearAvaUUID();
            ava.startRespawnCooldown();
        });
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        player.getCapability(AVACapability.INSTANCE).ifPresent(ava ->
            ava.getAvaUUID().ifPresent(id -> {
                // Iterable — use for-loop, not .filter().findFirst()
                for (Entity e : level.getEntities().getAll()) {
                    if (e instanceof AVAEntity && e.getUUID().equals(id)) {
                        e.discard();
                        break;
                    }
                }
            })
        );
    }

    public static void spawnAVA(Player player, ServerLevel level, AVAData ava) {
        AVAEntity entity = ModEntities.AVA.get().create(level);
        if (entity == null) return;

        entity.setOwnerUUID(player.getUUID());
        entity.moveTo(player.getX() + 1.5, player.getY() + 1.4, player.getZ(),
            player.getYRot(), 0);
        level.addFreshEntity(entity);
        ava.setAvaUUID(entity.getUUID());

        level.playSound(null, player.blockPosition(),
            ModSounds.AVA_ACTIVATE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        player.displayClientMessage(
            Component.translatable("status.u_heroes.ava_online"), true);

        UHeroesMod.LOGGER.debug("[AVA] Spawned for {} uuid={}",
            player.getName().getString(), entity.getUUID());
    }
}