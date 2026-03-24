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

    // Updated by ScannerDataPacket on the client
    private static volatile List<ScannerDataPacket.TargetInfo> targets = new ArrayList<>();
    public static boolean scannerActive = false;

    public static void updateTargets(List<ScannerDataPacket.TargetInfo> newTargets) {
        targets = new ArrayList<>(newTargets);
    }

    public static List<ScannerDataPacket.TargetInfo> getTargets() {
        return targets;
    }

    public static void clearTargets() {
        targets = new ArrayList<>();
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "scanner_hud",
            (gui, graphics, partialTick, width, height) -> renderOverlay(graphics));
    }

    private static void renderOverlay(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !scannerActive) return;
        if (!NanoSuitHandler.isWearingFullNanoSuit(mc.player)) return;

        List<ScannerDataPacket.TargetInfo> snap = targets;
        if (snap.isEmpty()) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // Draw a translucent dark panel on the right side
        int panelW = 160;
        int panelX = screenW - panelW - 8;
        int panelY = 40;
        int rowH   = 22;
        int panelH = snap.size() * rowH + 10;

        // Panel background
        g.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2,
               0x88000000);
        // Cyan border
        g.hLine(panelX - 2, panelX + panelW + 2, panelY - 2,    0xFF00FFFF);
        g.hLine(panelX - 2, panelX + panelW + 2, panelY + panelH + 2, 0xFF00FFFF);
        g.vLine(panelX - 2,    panelY - 2, panelY + panelH + 2, 0xFF00FFFF);
        g.vLine(panelX + panelW + 2, panelY - 2, panelY + panelH + 2, 0xFF00FFFF);

        // Header
        g.drawString(mc.font, "§b[ SCANNER ]", panelX + 2, panelY, 0xFFFFFF, false);
        panelY += 12;

        for (ScannerDataPacket.TargetInfo t : snap) {
            int y = panelY;

            // Name + distance
            String label = "§f" + t.name + " §7" + String.format("%.0fm", t.distance);
            g.drawString(mc.font, label, panelX, y, 0xFFFFFF, false);
            y += 10;

            // Health bar
            int barW = panelW - 4;
            int filledW = (int)(barW * t.healthPct);
            int barColor = t.healthPct > 0.5f ? 0xFF00FF44
                         : t.healthPct > 0.25f ? 0xFFFFAA00
                         : 0xFFFF2222;

            g.fill(panelX, y, panelX + barW, y + 3, 0xFF333333);
            if (filledW > 0)
                g.fill(panelX, y, panelX + filledW, y + 3, barColor);

            panelY += rowH;
        }
    }
}