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
 * Client-side keybind polling handler.
 *
 * <p>Runs every client tick. When a keybind fires it sends the corresponding
 * C→S packet. The server validates Flux, cooldowns, and suit requirements —
 * the client never applies effects directly.
 *
 * <h2>Jetpack</h2>
 * Jetpack is continuous — we send its state to the server every tick when the
 * key changes, using a dedicated packet.  For simplicity we piggyback on the
 * existing vanilla jump input detection rather than adding another packet type:
 * the server's {@code BoosterHandler.tickJetpack()} already gates on whether
 * the player is airborne. This avoids the classic "infinite jump" exploit where
 * a client floods jump packets.
 *
 * <h2>Shield</h2>
 * We track the previous-frame held state and only send a packet on
 * press/release transitions, not every tick, to avoid flooding the server.
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
            // Reset states when suit is removed
            prevShieldHeld  = false;
            prevJetpackHeld = false;
            return;
        }

        // ── Dash (tap) ────────────────────────────────────────────────────────
        while (ModKeybinds.BOOSTER_DASH.consumeClick()) {
            ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.DASH));
        }

        // ── Power Punch (tap) ─────────────────────────────────────────────────
        while (ModKeybinds.POWER_PUNCH.consumeClick()) {
            ModNetwork.sendToServer(new BoosterPacket(BoosterPacket.Action.POWER_PUNCH));
        }

        // ── AVA Shield (hold — send only on state change) ─────────────────────
        boolean shieldNow = ModKeybinds.AVA_SHIELD.isDown();
        if (shieldNow != prevShieldHeld) {
            ModNetwork.sendToServer(new AVAShieldPacket(shieldNow));
            prevShieldHeld = shieldNow;
        }

        // ── Jetpack (hold — server gates via NanoSuitHandler already, so we
        //    re-use a direct server-tick approach; no separate packet needed)
        // The server's NanoSuitHandler.onPlayerTick checks ModKeybinds.JETPACK
        // via a server-safe proxy.  Nothing to do on the client here.
    }
}