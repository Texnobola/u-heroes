package com.uheroes.mod.client.hud;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.network.ScannerDataPacket;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import com.uheroes.mod.init.ModKeybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ScannerHUD {

    private static volatile List<ScannerDataPacket.TargetInfo> targets = new ArrayList<>();
    public static boolean scannerActive = false;
    private static int    flashTick     = 0;

    public static void updateTargets(List<ScannerDataPacket.TargetInfo> t) { targets = new ArrayList<>(t); }
    public static List<ScannerDataPacket.TargetInfo> getTargets() { return targets; }
    public static void clearTargets() { targets = new ArrayList<>(); }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "scanner_hud",
            (gui, graphics, partialTick, width, height) -> renderOverlay(graphics, width, height));
    }

    private static void renderOverlay(GuiGraphics g, int sw, int sh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !scannerActive) return;
        if (!NanoSuitHandler.isWearingNanoHelmet(mc.player)) return;

        List<ScannerDataPacket.TargetInfo> snap = targets;
        flashTick++;

        // ── Header bar ─────────────────────────────────────────────────────
        int headerY = 8;
        String headerText = "[ NANO SCANNER ]";
        int headerX = sw - 170;
        // Header background
        g.fill(headerX - 4, headerY - 2, headerX + 164, headerY + 10, 0xCC000000);
        g.fill(headerX - 4, headerY + 10, headerX + 164, headerY + 11, 0xFF00FFFF);
        // Blinking dot
        if ((flashTick / 10) % 2 == 0)
            g.fill(headerX - 2, headerY + 1, headerX, headerY + 8, 0xFF00FF88);
        g.drawString(mc.font, "§b" + headerText, headerX + 3, headerY, 0xFFFFFF, false);

        if (snap.isEmpty()) {
            g.drawString(mc.font, "§7NO CONTACTS", headerX + 3, headerY + 14, 0xFFFFFF, false);
            return;
        }

        // ── Target rows ────────────────────────────────────────────────────
        int rowY = headerY + 14;
        for (int i = 0; i < snap.size(); i++) {
            ScannerDataPacket.TargetInfo t = snap.get(i);
            int rowH = 20;
            int rowX = headerX - 4;
            int rowW = 168;

            // Row background — alternating
            int bgAlpha = (i % 2 == 0) ? 0xBB000000 : 0x99001118;
            g.fill(rowX, rowY, rowX + rowW, rowY + rowH, bgAlpha);

            // Left accent bar — color by health
            int accentColor = healthColor(t.healthPct);
            g.fill(rowX, rowY, rowX + 2, rowY + rowH, accentColor);

            // Target number
            g.drawString(mc.font, "§8" + (i + 1), rowX + 4, rowY + 2, 0xFFFFFF, false);

            // Name (truncated)
            String name = t.name.length() > 14 ? t.name.substring(0, 13) + "…" : t.name;
            g.drawString(mc.font, "§f" + name, rowX + 14, rowY + 2, 0xFFFFFF, false);

            // Distance — right-aligned
            String distStr = String.format("%.0fm", t.distance);
            int distW = mc.font.width(distStr);
            g.drawString(mc.font, "§7" + distStr, rowX + rowW - distW - 4, rowY + 2, 0xFFFFFF, false);

            // Health bar
            int barX  = rowX + 14;
            int barW  = 100;
            int filled = (int)(barW * t.healthPct);
            int barY2  = rowY + 13;
            g.fill(barX, barY2, barX + barW, barY2 + 3, 0xFF1A1A1A);
            if (filled > 0)
                g.fill(barX, barY2, barX + filled, barY2 + 3, accentColor);
            // Health % text
            String hpStr = (int)(t.healthPct * 100) + "%";
            g.drawString(mc.font, "§8" + hpStr, barX + barW + 3, barY2 - 1, 0xFFFFFF, false);

            rowY += rowH;
        }

        // ── Bottom border ───────────────────────────────────────────────────
        g.fill(headerX - 4, rowY, headerX + 164, rowY + 1, 0xFF00FFFF);

        // ── Targets count ───────────────────────────────────────────────────
        g.drawString(mc.font, "§8TARGETS: §b" + snap.size(), headerX - 4, rowY + 3, 0xFFFFFF, false);
    }

    private static int healthColor(float hp) {
        if (hp > 0.6f) return 0xFF00FF44;   // green
        if (hp > 0.3f) return 0xFFFFBB00;   // orange
        return 0xFFFF2222;                    // red
    }
}