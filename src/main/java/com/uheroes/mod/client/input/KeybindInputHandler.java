package com.uheroes.mod.client.input;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.network.AVAShieldPacket;
import com.uheroes.mod.core.network.BoosterPacket;
import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import com.uheroes.mod.init.ModKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side keybind polling. Sends C→S packets on state changes.
 * Shield and jetpack send only on press/release transitions to avoid flooding.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class KeybindInputHandler {

    private static boolean prevShieldHeld  = false;
    private static boolean prevJetpackHeld = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        if (!NanoSuitHandler.isWearingFullNanoSuit(mc.player)) {
            // Key was held when suit removed — send release packets
            if (prevShieldHeld)  { ModNetwork.sendToServer(new AVAShieldPacket(false)); prevShieldHeld  = false; }
            if (prevJetpackHeld) { ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.JETPACK_OFF)); prevJetpackHeld = false; }
            return;
        }

        // Dash (tap)
        while (ModKeybinds.BOOSTER_DASH.consumeClick()) {
            ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.DASH));
        }

        // Power Punch (tap)
        while (ModKeybinds.POWER_PUNCH.consumeClick()) {
            ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.POWER_PUNCH));
        }

        // AVA Shield (hold — send on change only)
        boolean shieldNow = ModKeybinds.AVA_SHIELD.isDown();
        if (shieldNow != prevShieldHeld) {
            ModNetwork.sendToServer(new AVAShieldPacket(shieldNow));
            prevShieldHeld = shieldNow;
        }

        // Jetpack (hold — send on change only)
        // player.jumping is protected; we relay the key state via packet instead
        boolean jetpackNow = ModKeybinds.JETPACK.isDown();
        if (jetpackNow != prevJetpackHeld) {
            ModNetwork.sendToServer(new BoosterPacket(
                jetpackNow ? BoosterPacket.Action.JETPACK_ON : BoosterPacket.Action.JETPACK_OFF));
            prevJetpackHeld = jetpackNow;
        }
    }
}