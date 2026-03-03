package com.uheroes.mod.client.hud;

/**
 * Client-side cache for Neural Flux data.
 */
public class ClientFluxData {
    private static int currentFlux = 0;
    private static int maxFlux = 100;
    private static boolean overcharged = false;
    
    /**
     * Gets the current flux amount.
     */
    public static int getCurrentFlux() {
        return currentFlux;
    }
    
    /**
     * Gets the maximum flux amount.
     */
    public static int getMaxFlux() {
        return maxFlux;
    }
    
    /**
     * Gets the overcharged state.
     */
    public static boolean isOvercharged() {
        return overcharged;
    }
    
    /**
     * Gets the flux as a percentage (0.0 to 1.0).
     */
    public static float getFluxPercent() {
        return maxFlux > 0 ? (float) currentFlux / maxFlux : 0.0f;
    }
    
    /**
     * Updates the client-side flux data.
     */
    public static void update(int current, int max, boolean overcharge) {
        currentFlux = current;
        maxFlux = max;
        overcharged = overcharge;
    }
}
