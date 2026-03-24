package com.uheroes.mod.client.input;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.hud.ScannerHUD;
import com.uheroes.mod.core.network.AVAShieldPacket;
import com.uheroes.mod.core.network.BoosterPacket;
import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.core.network.ScannerPacket;
import com.uheroes.mod.event.ScannerGlowEvents;
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
    private static boolean prevJetpackHeld = false;
    private static boolean prevScannerHeld = false;
    private static int     scanTickTimer   = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        boolean fullSuit     = NanoSuitHandler.isWearingFullNanoSuit(mc.player);
        boolean hasChest     = NanoSuitHandler.isWearingNanoChestplate(mc.player);
        boolean hasLeggings  = NanoSuitHandler.isWearingNanoLeggings(mc.player);
        boolean hasHelmet    = NanoSuitHandler.isWearingNanoHelmet(mc.player);
        boolean hasBoots     = NanoSuitHandler.isWearingNanoBoots(mc.player);

        // ── Dash (V) — leggings ───────────────────────────────────────────────
        if (hasLeggings)
            while (ModKeybinds.BOOSTER_DASH.consumeClick())
                ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.DASH));

        // ── Power Punch (G) — leggings ────────────────────────────────────────
        if (hasLeggings)
            while (ModKeybinds.POWER_PUNCH.consumeClick()) {
                ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.POWER_PUNCH));
                if (mc.player != null)
                    com.uheroes.mod.client.animation.NanoSuitWalkAnimHandler.triggerPunchAnim(mc.player);
            }

        // ── Seismic Slam (C) — boots ──────────────────────────────────────────
        if (hasBoots)
            while (ModKeybinds.SEISMIC_SLAM.consumeClick())
                ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.SEISMIC_SLAM));

        // ── AVA resize (N) — full suit ────────────────────────────────────────
        if (fullSuit)
            while (ModKeybinds.AVA_RESIZE.consumeClick())
                ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.AVA_RESIZE));

        // ── AVA Shield (R hold) — full suit ───────────────────────────────────
        boolean shieldNow = ModKeybinds.AVA_SHIELD.isDown() && fullSuit;
        if (shieldNow != prevShieldHeld) {
            ModNetwork.sendToServer(new AVAShieldPacket(shieldNow));
            prevShieldHeld = shieldNow;
        }
        if (!fullSuit && prevShieldHeld) {
            ModNetwork.sendToServer(new AVAShieldPacket(false));
            prevShieldHeld = false;
        }

        // ── Jetpack (Space hold) — CHESTPLATE ONLY ────────────────────────────
        boolean jetpackNow = ModKeybinds.JETPACK.isDown() && hasChest;
        if (jetpackNow != prevJetpackHeld) {
            ModNetwork.sendToServer(new BoosterPacket(
                jetpackNow ? BoosterPacket.Action.JETPACK_ON : BoosterPacket.Action.JETPACK_OFF));
            prevJetpackHeld = jetpackNow;
        }
        // Make sure jetpack turns off if chestplate removed while held
        if (!hasChest && prevJetpackHeld) {
            ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.JETPACK_OFF));
            prevJetpackHeld = false;
        }

        // ── Scanner (Z hold) — helmet ─────────────────────────────────────────
        boolean scannerNow = ModKeybinds.SCANNER.isDown() && hasHelmet;
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
    }
}