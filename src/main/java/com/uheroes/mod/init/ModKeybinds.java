package com.uheroes.mod.init;

import com.uheroes.mod.UHeroesMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeybinds {

    private static final String CATEGORY = "key.categories.u_heroes";

    /** Hold to expand AVA's shield sphere. */
    public static final KeyMapping AVA_SHIELD = new KeyMapping(
        "key.u_heroes.ava_shield",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R, CATEGORY);

    /** Horizontal burst dash in look direction. */
    public static final KeyMapping BOOSTER_DASH = new KeyMapping(
        "key.u_heroes.booster_dash",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V, CATEGORY);

    /** Hold while airborne for jetpack thrust. */
    public static final KeyMapping JETPACK = new KeyMapping(
        "key.u_heroes.jetpack",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);

    /** Flux-charged gauntlet power punch. */
    public static final KeyMapping POWER_PUNCH = new KeyMapping(
        "key.u_heroes.power_punch",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G, CATEGORY);

    /** Scan target entity for combat analysis. */
    public static final KeyMapping SEISMIC_SLAM = new KeyMapping(
        "key.u_heroes.seismic_slam",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        com.mojang.blaze3d.platform.InputConstants.KEY_C,
        "key.categories.u_heroes"
    );

    public static final KeyMapping SCANNER = new KeyMapping(
        "key.u_heroes.scanner",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z, CATEGORY);

    /** Cycle AVA's visual size (Small → Medium → Large). */
    public static final KeyMapping AVA_RESIZE = new KeyMapping(
        "key.u_heroes.ava_resize",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_N, CATEGORY);

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(AVA_SHIELD);
        event.register(BOOSTER_DASH);
        event.register(JETPACK);
        event.register(POWER_PUNCH);
        event.register(SEISMIC_SLAM);
        event.register(SCANNER);
        event.register(AVA_RESIZE);
    }

    public static void registerKeybinds() { /* handled by event */ }
}