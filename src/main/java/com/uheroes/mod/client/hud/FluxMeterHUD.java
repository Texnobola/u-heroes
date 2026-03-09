package com.uheroes.mod.client.hud;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FluxMeterHUD {
    private static final int BAR_WIDTH = 160;
    private static final int BAR_HEIGHT = 14;
    private static final int SEGMENT_COUNT = 10;
    private static final int SEGMENT_WIDTH = 14;
    private static final int SEGMENT_GAP = 2;
    private static final int COMBO_RESET_TICKS = 40;
    
    // Combo counter
    public static int comboIndex = 0;
    public static int ticksSinceAttack = 0;
    private static float lastAttackStrength = 1.0f;
    
    // Hit flash
    public static int flashTicks = 0;
    public static int flashType = 0;
    
    // Flux drain animation
    private static float previousFlux = 0;
    private static float drainAmount = 0;
    private static int drainTicks = 0;
    
    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "flux_meter", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.player.isSpectator() || mc.options.hideGui) return;
            renderFluxMeter(graphics, screenWidth, screenHeight);
            renderHitFlash(graphics, screenWidth, screenHeight);
        });
    }
    
    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
    public static class TickHandler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            
            // Combo tracking
            if (mc.player.getMainHandItem().getItem() instanceof LaserSwordItem) {
                float currentAttackStrength = mc.player.getAttackStrengthScale(0f);
                if (lastAttackStrength == 1.0f && currentAttackStrength < 1.0f) {
                    comboIndex = (comboIndex + 1) % 10;
                    ticksSinceAttack = 0;
                }
                lastAttackStrength = currentAttackStrength;
                
                ticksSinceAttack++;
                if (ticksSinceAttack > COMBO_RESET_TICKS) {
                    comboIndex = 0;
                }
            } else {
                comboIndex = 0;
                ticksSinceAttack = 0;
            }
            
            // Flash decay
            if (flashTicks > 0) flashTicks--;
            
            // Drain animation
            if (drainTicks > 0) drainTicks--;
            
            int currentFlux = ClientFluxData.getCurrentFlux();
            if (currentFlux < previousFlux) {
                drainAmount = previousFlux - currentFlux;
                drainTicks = 10;
            }
            previousFlux += (currentFlux - previousFlux) * 0.3f;
        }
    }
    
    private static void renderFluxMeter(GuiGraphics graphics, int screenWidth, int screenHeight) {
        int x = 10;
        int y = screenHeight - 32;
        
        int current = ClientFluxData.getCurrentFlux();
        int max = ClientFluxData.getMaxFlux();
        float percent = current / (float) max;
        int filledSegments = (int) (percent * SEGMENT_COUNT);
        
        long time = System.currentTimeMillis();
        float pulse = (float)(Math.sin(time / 300.0) * 0.5 + 0.5);
        
        // Label
        graphics.drawString(Minecraft.getInstance().font, "NEURAL FLUX", x, y - 10, 0xFF00CCFF, false);
        
        // Combo counter
        if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof LaserSwordItem && ticksSinceAttack < COMBO_RESET_TICKS) {
            float fadeAlpha = 1.0f - (ticksSinceAttack / (float)COMBO_RESET_TICKS);
            int alpha = (int)(fadeAlpha * 255) << 24;
            int comboColor = (comboIndex >= 7) ? (alpha | 0xFF6600) : (alpha | 0x00FFFF);
            String comboText = "[ " + (comboIndex + 1) + " / 10 ]";
            graphics.drawString(Minecraft.getInstance().font, comboText, x, y - 20, comboColor, false);
        }
        
        // Segments with drain animation
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            int segX = x + i * (SEGMENT_WIDTH + SEGMENT_GAP);
            boolean filled = i < filledSegments;
            
            int skew = 3;
            drawParallelogram(graphics, segX, y, SEGMENT_WIDTH, BAR_HEIGHT, skew, filled, percent, pulse, time);
            
            // Drain ghost bar
            if (drainTicks > 0) {
                float drainPercent = (previousFlux / max);
                int drainSegments = (int)(drainPercent * SEGMENT_COUNT);
                if (i >= filledSegments && i < drainSegments) {
                    float drainAlpha = drainTicks / 10f;
                    int alpha = (int)(drainAlpha * 255);
                    int drainColor = (alpha << 24) | 0xFFFFFF;
                    drawDrainOverlay(graphics, segX, y, SEGMENT_WIDTH, BAR_HEIGHT, skew, drainColor);
                }
            }
        }
        
        // Value text
        String valueText = current + " / " + max;
        graphics.drawString(Minecraft.getInstance().font, valueText, x + BAR_WIDTH + 5, y + 3, 0xFFFFFFFF, false);
    }
    
    private static void renderHitFlash(GuiGraphics graphics, int screenWidth, int screenHeight) {
        if (flashTicks <= 0) return;
        
        float alpha = flashTicks / 12f;
        int baseAlpha = (int)(alpha * 170);
        int color = 0;
        
        if (flashType == 8) {
            color = (baseAlpha << 24) | 0xFF4400;
        } else if (flashType == 9) {
            color = (baseAlpha << 24) | 0xFF0000;
        } else if (flashType == 10) {
            color = (baseAlpha << 24) | 0xFF0022;
        }
        
        int edgeWidth = 60;
        // Top
        graphics.fillGradient(0, 0, screenWidth, edgeWidth, color, 0x00000000);
        // Bottom
        graphics.fillGradient(0, screenHeight - edgeWidth, screenWidth, screenHeight, 0x00000000, color);
        // Left
        graphics.fillGradient(0, 0, edgeWidth, screenHeight, color, 0x00000000);
        // Right
        graphics.fillGradient(screenWidth - edgeWidth, 0, screenWidth, screenHeight, 0x00000000, color);
    }
    
    private static void drawDrainOverlay(GuiGraphics graphics, int x, int y, int width, int height, int skew, int color) {
        for (int row = 0; row < height; row++) {
            int x1 = x + skew - (row * skew / height);
            int x2 = x1 + width;
            graphics.fill(x1, y + row, x2, y + row + 1, color);
        }
    }
    
    private static void drawParallelogram(GuiGraphics graphics, int x, int y, int width, int height, int skew, boolean filled, float percent, float pulse, long time) {
        int fillColor = filled ? 0xFF00FFFF : 0xFF0A1A1A;
        
        if (filled && percent >= 0.99f) {
            int white = (int)(pulse * 255);
            fillColor = 0xFF000000 | (white << 16) | (white << 8) | 0xFF;
        }
        
        if (filled && percent < 0.2f) {
            float redPulse = (float)(Math.sin(time / 200.0) * 0.5 + 0.5);
            int red = (int)(redPulse * 255);
            fillColor = 0xFF000000 | (red << 16) | 0x2200;
        }
        
        if (filled && percent > 0.8f) {
            float orangePulse = (float)(Math.sin(time / 250.0) * 0.5 + 0.5);
            if (orangePulse > 0.7f) {
                fillColor = 0xFFFF6600;
            }
        }
        
        for (int row = 0; row < height; row++) {
            int x1 = x + skew - (row * skew / height);
            int x2 = x1 + width;
            graphics.fill(x1, y + row, x2, y + row + 1, fillColor);
            
            if (row % 2 == 0) {
                graphics.fill(x1, y + row, x2, y + row + 1, 0x22000000);
            }
        }
        
        int borderColor = filled ? 0xFF00AAAA : 0xFF1A2A2A;
        drawParallelogramBorder(graphics, x, y, width, height, skew, borderColor);
    }
    
    private static void drawParallelogramBorder(GuiGraphics graphics, int x, int y, int width, int height, int skew, int color) {
        graphics.fill(x + skew, y, x + width + skew, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        for (int i = 0; i < height; i++) {
            int edgeX = x + skew - (i * skew / height);
            graphics.fill(edgeX, y + i, edgeX + 1, y + i + 1, color);
        }
        for (int i = 0; i < height; i++) {
            int edgeX = x + width + skew - (i * skew / height);
            graphics.fill(edgeX, y + i, edgeX + 1, y + i + 1, color);
        }
    }
}
