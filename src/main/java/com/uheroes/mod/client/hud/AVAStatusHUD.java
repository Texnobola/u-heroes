package com.uheroes.mod.client.hud;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import com.uheroes.mod.heroes.nanotech.ava.AVAEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AVAStatusHUD {

    private static String  clientStateLabel = "OFFLINE";
    private static boolean shieldActive     = false;
    private static boolean avaPresent       = false;
    private static boolean isRiding         = false;

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "ava_status",
            (gui, graphics, partialTick, screenWidth, screenHeight) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || mc.player.isSpectator() || mc.options.hideGui) return;
                if (!NanoSuitHandler.isWearingFullNanoSuit(mc.player)) return;
                if (!avaPresent) return;
                renderAVAStatus(graphics, screenWidth);
            });
    }

    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
    public static class TickHandler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) { avaPresent = false; return; }

            avaPresent = false;
            mc.level.entitiesForRendering().forEach(e -> {
                if (e instanceof AVAEntity ava) {
                    if (ava.getOwnerUUID().filter(id -> id.equals(mc.player.getUUID())).isPresent()) {
                        avaPresent   = true;
                        shieldActive = ava.isShieldActive();
                        isRiding     = ava.isVehicle();
                        clientStateLabel = isRiding ? "RIDING" : "ORBIT";
                    }
                }
            });
        }
    }

    private static void renderAVAStatus(GuiGraphics g, int screenWidth) {
        int panelW = 110;
        int x      = screenWidth - panelW - 12;
        int y      = 12;

        long  now   = System.currentTimeMillis();
        float pulse = (float)(Math.sin(now / 300.0) * 0.5 + 0.5);

        // Background
        g.fill(x - 2, y - 2, x + panelW + 2, y + 34, 0x88000810);
        g.fill(x - 2, y - 2, x + panelW + 2, y - 1, 0xFF003344);

        // Label
        g.drawString(Minecraft.getInstance().font, "AVA", x, y, 0xFF00AACC, false);

        // State
        int stateColor;
        if (isRiding) {
            int a = (int)(((float)(Math.sin(now / 150.0) * 0.5 + 0.5)) * 200 + 55);
            stateColor = (a << 24) | 0x00FF88;
        } else {
            int a = (int)(pulse * 80 + 80);
            stateColor = (a << 24) | 0x00CCFF;
        }
        g.drawString(Minecraft.getInstance().font, "\u25C8 " + clientStateLabel, x, y + 12, stateColor, false);

        // Shield bar
        if (shieldActive) {
            g.fill(x, y + 26, x + panelW - 4, y + 33, 0xFF0A1A22);
            g.fill(x, y + 26, x + panelW - 4, y + 33, 0xFFCCEEFF);
            g.drawString(Minecraft.getInstance().font, "SHIELD", x + panelW - 42, y + 26, 0xFFCCEEFF, false);
        } else {
            int a = (int)(pulse * 40 + 20);
            g.drawString(Minecraft.getInstance().font, "SHIELD",
                x + panelW - 42, y + 26, (a << 24) | 0x334455, false);
        }
    }
}