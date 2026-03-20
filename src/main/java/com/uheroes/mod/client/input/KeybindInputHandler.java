package com.uheroes.mod.client.input;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.hud.ScannerHUD;
import com.uheroes.mod.core.network.ScannerPacket;
import com.uheroes.mod.event.ScannerGlowEvents;
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

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class KeybindInputHandler {

    private static boolean prevShieldHeld  = false;
    private static boolean prevScannerHeld = false;
    private static int      scanTickTimer   = 0;
    private static boolean prevJetpackHeld = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        if (!NanoSuitHandler.isWearingFullNanoSuit(mc.player)) {
            if (prevShieldHeld)  { ModNetwork.sendToServer(new AVAShieldPacket(false)); prevShieldHeld = false; }
            if (prevJetpackHeld) { ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.JETPACK_OFF)); prevJetpackHeld = false; }
            return;
        }

        // Dash (tap)
        while (ModKeybinds.BOOSTER_DASH.consumeClick())
            ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.DASH));

        // Power Punch (tap)
        while (ModKeybinds.POWER_PUNCH.consumeClick()) {
            ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.POWER_PUNCH));
            // Trigger punch animation immediately on the client — packet goes to server,
            // but animation must fire here since BoosterHandler runs server-side
            if (mc.player != null)
                com.uheroes.mod.client.animation.NanoSuitWalkAnimHandler.triggerPunchAnim(mc.player);
        }

        // AVA resize cycle (tap) — N key
        while (ModKeybinds.AVA_RESIZE.consumeClick())
            ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.AVA_RESIZE));

        // AVA Shield (hold → change only)
        boolean shieldNow = ModKeybinds.AVA_SHIELD.isDown();
        if (shieldNow != prevShieldHeld) {
            ModNetwork.sendToServer(new AVAShieldPacket(shieldNow));
            prevShieldHeld = shieldNow;
        }

        // Seismic Slam (tap C)
        while (ModKeybinds.SEISMIC_SLAM.consumeClick())
            ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.SEISMIC_SLAM));

        // Scanner (hold Z)
        boolean scannerNow = ModKeybinds.SCANNER.isDown()
            && NanoSuitHandler.isWearingFullNanoSuit(mc.player);
        if (scannerNow != prevScannerHeld) {
            prevScannerHeld = scannerNow;
            ScannerHUD.scannerActive = scannerNow;
            ModNetwork.sendToServer(new ScannerPacket(scannerNow));
            if (!scannerNow) ScannerHUD.clearTargets();
            scanTickTimer = 0;
        }
        if (scannerNow) {
            scanTickTimer++;
            if (scanTickTimer % 10 == 0)
                ModNetwork.sendToServer(new ScannerPacket(true));
        }
        ScannerGlowEvents.tickGlow();

        // Jetpack (hold → change only)
        boolean jetpackNow = ModKeybinds.JETPACK.isDown();
        if (jetpackNow != prevJetpackHeld) {
            ModNetwork.sendToServer(new BoosterPacket(
                jetpackNow ? BoosterPacket.Action.JETPACK_ON : BoosterPacket.Action.JETPACK_OFF));
            prevJetpackHeld = jetpackNow;
        }
    }
}