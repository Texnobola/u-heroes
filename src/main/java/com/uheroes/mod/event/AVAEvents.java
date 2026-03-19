package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.ability.BoosterHandler;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import com.uheroes.mod.heroes.nanotech.ava.AVACapability;
import com.uheroes.mod.heroes.nanotech.ava.AVAData;
import com.uheroes.mod.heroes.nanotech.ava.AVAEntity;
import com.uheroes.mod.heroes.nanotech.ava.AVAShieldItem;
import com.uheroes.mod.init.ModEntities;
import com.uheroes.mod.init.ModItems;
import com.uheroes.mod.init.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class AVAEvents {

    // ─── Entity interaction (right-click AVA) ─────────────────────────────────

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        // Must check target type first
        if (!(event.getTarget() instanceof AVAEntity ava)) return;
        // Only handle once — main hand
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        // Cancel vanilla interaction immediately so vanilla doesn't also process it
        event.setCanceled(true);
        event.setResult(Event.Result.DENY);

        // Server-side logic only
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player.isShiftKeyDown()) {
            // Shift+RightClick → recall AVA into shield item
            player.getCapability(AVACapability.INSTANCE).ifPresent(data -> {
                data.clearAvaUUID();
                ava.discard();
                ItemStack avaItem = new ItemStack(ModItems.AVA_SHIELD.get());
                if (!player.getInventory().add(avaItem)) {
                    player.drop(avaItem, false);
                }
                event.getLevel().playSound(null, player.blockPosition(),
                    ModSounds.AVA_BLOCK.get(), SoundSource.PLAYERS, 0.9f, 0.8f);
                player.displayClientMessage(
                    Component.literal("§b[AVA] §7Recalled to item."), true);
            });
        } else {
            // RightClick → mount AVA as hoverboard
            if (ava.getPassengers().isEmpty()) {
                player.startRiding(ava, true);
                player.displayClientMessage(
                    Component.literal("§b[AVA] §7Hoverboard active. Sneak to dismount."), true);
            }
        }
    }

    // ─── Player tick ──────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        if (!(event.player.level() instanceof ServerLevel level)) return;

        Player player = event.player;

        // Jetpack
        if (NanoSuitHandler.isWearingFullNanoSuit(player)
                && BoosterHandler.isJetpackActive(player)
                && !player.onGround()) {
            BoosterHandler.tickJetpack(player);
        }

        // AVA lifecycle — check every 20 ticks
        if (player.tickCount % 20 != 0) return;
        if (!NanoSuitHandler.isWearingFullNanoSuit(player)) return;

        player.getCapability(AVACapability.INSTANCE).ifPresent(ava -> {
            if (ava.isOnCooldown()) return;

            if (!ava.hasAva()) {
                spawnAVA(player, level, ava);
                return;
            }

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

    // ─── Respawn / logout ─────────────────────────────────────────────────────

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
                for (Entity e : level.getEntities().getAll()) {
                    if (e instanceof AVAEntity && e.getUUID().equals(id)) {
                        e.discard();
                        break;
                    }
                }
            })
        );
    }

    // ─── Spawn helper ─────────────────────────────────────────────────────────

    public static void spawnAVA(Player player, ServerLevel level, AVAData ava) {
        // Don't spawn if player has AVA in item form already
        for (ItemStack s : player.getInventory().items) {
            if (s.getItem() instanceof AVAShieldItem) return;
        }

        AVAEntity entity = ModEntities.AVA.get().create(level);
        if (entity == null) return;

        entity.setOwnerUUID(player.getUUID());
        entity.moveTo(player.getX() + 1.5, player.getY() + 1.4,
            player.getZ(), player.getYRot(), 0);
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