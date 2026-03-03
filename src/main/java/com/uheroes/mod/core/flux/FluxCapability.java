package com.uheroes.mod.core.flux;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Capability for Neural Flux system.
 */
public class FluxCapability {
    public static final Capability<NeuralFlux> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation ID = new ResourceLocation("u_heroes", "neural_flux");
    
    /**
     * Gets the Neural Flux capability from a player.
     * @param player The player
     * @return LazyOptional containing the NeuralFlux instance
     */
    public static LazyOptional<NeuralFlux> getFlux(Player player) {
        return player.getCapability(INSTANCE);
    }
    
    /**
     * Attempts to consume flux from a player.
     * @param player The player
     * @param amount The amount to consume
     * @return true if consumption was successful
     */
    public static boolean consume(Player player, int amount) {
        return getFlux(player).map(flux -> flux.consumeFlux(amount)).orElse(false);
    }
    
    /**
     * Adds flux to a player.
     * @param player The player
     * @param amount The amount to add
     */
    public static void add(Player player, int amount) {
        getFlux(player).ifPresent(flux -> flux.addFlux(amount));
    }
    
    /**
     * Gets the current flux amount for a player.
     * @param player The player
     * @return The current flux amount, or 0 if capability not present
     */
    public static int getCurrent(Player player) {
        return getFlux(player).map(NeuralFlux::getCurrentFlux).orElse(0);
    }
    
    /**
     * Gets the maximum flux amount for a player.
     * @param player The player
     * @return The maximum flux amount, or 0 if capability not present
     */
    public static int getMax(Player player) {
        return getFlux(player).map(NeuralFlux::getMaxFlux).orElse(0);
    }
}
