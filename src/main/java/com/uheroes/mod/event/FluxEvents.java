package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.core.flux.FluxProvider;
import com.uheroes.mod.core.flux.NeuralFlux;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handlers for Neural Flux capability system.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class FluxEvents {
    
    /**
     * Registers the Neural Flux capability.
     */
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(NeuralFlux.class);
    }
    
    /**
     * Attaches Neural Flux capability to all players.
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            FluxProvider provider = new FluxProvider();
            event.addCapability(FluxCapability.ID, provider);
            event.addListener(provider::invalidateCaps);
        }
    }
    
    /**
     * Copies Neural Flux data on player death/respawn and dimension change.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        
        oldPlayer.reviveCaps();
        
        oldPlayer.getCapability(FluxCapability.INSTANCE).ifPresent(oldFlux -> {
            newPlayer.getCapability(FluxCapability.INSTANCE).ifPresent(newFlux -> {
                newFlux.copyFrom(oldFlux);
            });
        });
        
        oldPlayer.invalidateCaps();
    }
    
    /**
     * Ticks Neural Flux for all players (server-side only).
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            event.player.getCapability(FluxCapability.INSTANCE).ifPresent(flux -> {
                flux.tick();
                
                // Sync to client every 5 ticks
                if (event.player.tickCount % 5 == 0 && event.player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    com.uheroes.mod.core.network.ModNetwork.sendToPlayer(
                        new com.uheroes.mod.core.network.FluxSyncPacket(
                            flux.getCurrentFlux(),
                            flux.getMaxFlux(),
                            flux.isOvercharged()
                        ),
                        serverPlayer
                    );
                }
            });
        }
    }
}
