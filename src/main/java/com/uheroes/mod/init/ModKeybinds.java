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

    public static final KeyMapping AVA_SHIELD = new KeyMapping(
        "key.u_heroes.ava_shield",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        CATEGORY);

    public static final KeyMapping BOOSTER_DASH = new KeyMapping(
        "key.u_heroes.booster_dash",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        CATEGORY);

    public static final KeyMapping JETPACK = new KeyMapping(
        "key.u_heroes.jetpack",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_SPACE,
        CATEGORY);

    public static final KeyMapping POWER_PUNCH = new KeyMapping(
        "key.u_heroes.power_punch",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        CATEGORY);

    public static final KeyMapping SCANNER = new KeyMapping(
        "key.u_heroes.scanner",
        com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        CATEGORY);

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(AVA_SHIELD);
        event.register(BOOSTER_DASH);
        event.register(JETPACK);
        event.register(POWER_PUNCH);
        event.register(SCANNER);
    }

    /** Called from UHeroesMod.clientSetup — kept for compatibility but now a no-op.
     *  Registration happens via RegisterKeyMappingsEvent above. */
    public static void registerKeybinds() { /* no-op — handled by event */ }
}