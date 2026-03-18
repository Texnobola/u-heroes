package com.uheroes.mod.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.ClientRegistry;

public class ModKeybinds {

    private static final String CATEGORY = "key.categories.u_heroes";

    // ─── AVA ─────────────────────────────────────────────────────────────────
    /** Hold to expand AVA's shield sphere. */
    public static final KeyMapping AVA_SHIELD = new KeyMapping(
        "key.u_heroes.ava_shield",
        KeyConflictContext.IN_GAME,
        InputConstants.getKey("key.keyboard.r", -1),
        CATEGORY);

    // ─── Boosters ─────────────────────────────────────────────────────────────
    /** Tap for horizontal dash burst. */
    public static final KeyMapping BOOSTER_DASH = new KeyMapping(
        "key.u_heroes.booster_dash",
        KeyConflictContext.IN_GAME,
        InputConstants.getKey("key.keyboard.v", -1),
        CATEGORY);

    /** Hold while airborne for jetpack flight. */
    public static final KeyMapping JETPACK = new KeyMapping(
        "key.u_heroes.jetpack",
        KeyConflictContext.IN_GAME,
        InputConstants.getKey("key.keyboard.space", -1),
        CATEGORY);

    /** Flux-charged power punch / gauntlet strike. */
    public static final KeyMapping POWER_PUNCH = new KeyMapping(
        "key.u_heroes.power_punch",
        KeyConflictContext.IN_GAME,
        InputConstants.getKey("key.keyboard.g", -1),
        CATEGORY);

    // ─── Analysis ─────────────────────────────────────────────────────────────
    /** Scan the targeted entity. */
    public static final KeyMapping SCANNER = new KeyMapping(
        "key.u_heroes.scanner",
        KeyConflictContext.IN_GAME,
        InputConstants.getKey("key.keyboard.z", -1),
        CATEGORY);

    public static void registerKeybinds() {
        ClientRegistry.registerKeyBinding(AVA_SHIELD);
        ClientRegistry.registerKeyBinding(BOOSTER_DASH);
        ClientRegistry.registerKeyBinding(JETPACK);
        ClientRegistry.registerKeyBinding(POWER_PUNCH);
        ClientRegistry.registerKeyBinding(SCANNER);
    }
}