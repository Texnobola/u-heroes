package com.uheroes.mod.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.uheroes.mod.UHeroesMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * HUD overlay for Neural Flux meter.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FluxMeterHUD {
    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 9;
    private static final int BAR_X = 10;
    private static final int BAR_Y_OFFSET = 49;
    
    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "flux_meter", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = Minecraft.getInstance();
            
            if (mc.player == null || mc.player.isSpectator() || mc.options.hideGui) {
                return;
            }
            
            renderFluxMeter(graphics, screenWidth, screenHeight, partialTick);
        });
    }
    
    private static void renderFluxMeter(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick) {
        int x = BAR_X;
        int y = screenHeight - BAR_Y_OFFSET;
        
        float fluxPercent = ClientFluxData.getFluxPercent();
        int current = ClientFluxData.getCurrentFlux();
        int max = ClientFluxData.getMaxFlux();
        boolean overcharged = ClientFluxData.isOvercharged();
        
        // Background
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xB4001A1A);
        
        // Fill bar
        int fillWidth = (int) (BAR_WIDTH * fluxPercent);
        if (fillWidth > 0) {
            int color = interpolateColor(0xFF004D4D, 0xFF00F5FF, fluxPercent);
            
            graphics.fill(x, y, x + fillWidth, y + BAR_HEIGHT, color);
            
            if (overcharged) {
                float pulse = (float) Math.sin(System.currentTimeMillis() / 200.0) * 0.5f + 0.5f;
                int glowAlpha = (int) (pulse * 100);
                int glowColor = (glowAlpha << 24) | 0x00FFFFFF;
                graphics.fill(x, y, x + fillWidth, y + BAR_HEIGHT, glowColor);
            }
        }
        
        // Border
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y, 0xFF000000);
        graphics.fill(x - 1, y + BAR_HEIGHT, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);
        graphics.fill(x - 1, y, x, y + BAR_HEIGHT, 0xFF000000);
        graphics.fill(x + BAR_WIDTH, y, x + BAR_WIDTH + 1, y + BAR_HEIGHT, 0xFF000000);
        
        // Label
        Component label = Component.translatable("hud.u_heroes.flux_meter");
        graphics.drawString(Minecraft.getInstance().font, label, x, y - 10, 0xFF00F5FF, true);
        
        // Numbers
        String numbers = current + " / " + max;
        graphics.drawString(Minecraft.getInstance().font, numbers, x + BAR_WIDTH + 5, y + 1, 0xFFFFFFFF, true);
    }
    
    private static int interpolateColor(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
