package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.core.flux.FluxProvider;
import com.uheroes.mod.core.flux.NeuralFlux;
import com.uheroes.mod.heroes.nanotech.ava.AVACapability;
import com.uheroes.mod.heroes.nanotech.ava.AVAData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handlers for Neural Flux and AVA capability systems.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class FluxEvents {

    // ─── Capability registration ──────────────────────────────────────────────

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(NeuralFlux.class);
        event.register(AVAData.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) return;

        FluxProvider fluxProvider = new FluxProvider();
        event.addCapability(FluxCapability.ID, fluxProvider);
        event.addListener(fluxProvider::invalidateCaps);

        AVACapability.Provider avaProvider = new AVACapability.Provider();
        event.addCapability(AVACapability.ID, avaProvider);
        event.addListener(avaProvider::invalidateCaps);
    }

    // ─── Death / respawn copy ─────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player old = event.getOriginal();
        Player neo = event.getEntity();

        old.reviveCaps();

        old.getCapability(FluxCapability.INSTANCE).ifPresent(oldFlux ->
            neo.getCapability(FluxCapability.INSTANCE).ifPresent(newFlux ->
                newFlux.copyFrom(oldFlux)));

        old.getCapability(AVACapability.INSTANCE).ifPresent(oldAva ->
            neo.getCapability(AVACapability.INSTANCE).ifPresent(newAva ->
                newAva.copyFrom(oldAva)));

        old.invalidateCaps();
    }

    // ─── Per-tick logic ───────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;

        // Seismic Slam dive tick
        if (player instanceof net.minecraft.server.level.ServerPlayer sp)
            com.uheroes.mod.heroes.nanotech.ability.SeismicSlamHandler.tickDiving(sp);

        // Flux tick + sync
        player.getCapability(FluxCapability.INSTANCE).ifPresent(flux -> {
            flux.tick();
            if (player.tickCount % 5 == 0 && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                com.uheroes.mod.core.network.ModNetwork.sendToPlayer(
                    new com.uheroes.mod.core.network.FluxSyncPacket(
                        flux.getCurrentFlux(), flux.getMaxFlux(), flux.isOvercharged()),
                    sp);
            }
        });

        // AVA cooldown tick
        player.getCapability(AVACapability.INSTANCE).ifPresent(AVAData::tickCooldown);
    }

    /** Prevent fall damage for seismic slam divers and jetpack users. */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;
        // No fall damage during seismic slam
        if (com.uheroes.mod.heroes.nanotech.ability.SeismicSlamHandler.isDiving(sp)) {
            event.setCanceled(true);
            return;
        }
        // No fall damage while jetpack chestplate worn
        if (com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler.isWearingNanoChestplate(sp)) {
            event.setCanceled(true);
        }
    }
}