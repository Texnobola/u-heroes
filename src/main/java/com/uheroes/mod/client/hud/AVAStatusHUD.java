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

/**
 * HUD overlay that shows AVA's current state in the top-right corner.
 *
 * <p>Renders only when the player wears the full Nano Suit and AVA is present.
 * Matches the existing Flux Meter visual language: cyan, parallelogram segments,
 * monospace-style label.
 *
 * <pre>
 *   ┌─ AVA ─────────────────┐
 *   │ ◈ PASSIVE              │   (dim cyan, slow pulse)
 *   │ ◈ ALERT                │   (bright cyan, faster pulse)
 *   │ ◈ INTERCEPT            │   (orange, rapid pulse)
 *   │ ■■■■■■■■■■  SHIELD     │   (white bar when shield active)
 *   └────────────────────────┘
 * </pre>
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AVAStatusHUD {

    // Client-side cached AVA state — updated by scanning nearby entities
    private static AVAEntity.AVAState clientState    = AVAEntity.AVAState.PASSIVE;
    private static boolean            shieldActive   = false;
    private static float              shieldRadius   = 0f;
    private static boolean            avaPresent     = false;

    // Smooth animation values
    private static float statePulse   = 0f;
    private static int   alertFlicker = 0;

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

    // ─── Client tick: scan for AVA entity ────────────────────────────────────

    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
    public static class TickHandler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) { avaPresent = false; return; }

            // Find the AVA entity linked to this player (scan nearby entities)
            mc.level.entitiesForRendering().forEach(e -> {
                if (e instanceof AVAEntity ava) {
                    if (ava.getOwnerUUID().filter(id -> id.equals(mc.player.getUUID())).isPresent()) {
                        avaPresent   = true;
                        clientState  = ava.getAVAState();
                        shieldActive = ava.isShieldActive();
                        shieldRadius = ava.getShieldRadius();
                    }
                }
            });

            // Animation
            statePulse = (float)(System.currentTimeMillis() / 300.0 % (Math.PI * 2));
            if (alertFlicker > 0) alertFlicker--;
        }
    }

    // ─── Render ───────────────────────────────────────────────────────────────

    private static void renderAVAStatus(GuiGraphics g, int screenWidth) {
        int panelW = 120;
        int x      = screenWidth - panelW - 12;
        int y      = 12;

        long now     = System.currentTimeMillis();
        float pulse  = (float)(Math.sin(now / 300.0) * 0.5 + 0.5);
        float fPulse = (float)(Math.sin(now / 120.0) * 0.5 + 0.5); // fast

        // ── Panel background ──────────────────────────────────────────────────
        g.fill(x - 2, y - 2, x + panelW + 2, y + 36, 0x88000810);

        // Top border line
        g.fill(x - 2, y - 2, x + panelW + 2, y - 1,
            0xFF003344);

        // "AVA" label
        g.drawString(Minecraft.getInstance().font, "AVA", x, y, 0xFF00AACC, false);

        // ── State indicator ───────────────────────────────────────────────────
        String stateLabel;
        int stateColor;
        switch (clientState) {
            case ALERT -> {
                stateLabel = "ALERT";
                int a      = (int)(fPulse * 200 + 55);
                stateColor = (a << 24) | 0x00FFFF;
            }
            case INTERCEPT -> {
                stateLabel = "INTERCEPT";
                int a      = (int)(fPulse * 200 + 55);
                stateColor = (a << 24) | 0xFF8800;
            }
            case ATTACK -> {
                stateLabel = "ATTACK";
                int a      = (int)(fPulse * 200 + 55);
                stateColor = (a << 24) | 0xFF2244;
            }
            default -> {
                stateLabel = "PASSIVE";
                int a      = (int)(pulse * 80 + 80);
                stateColor = (a << 24) | 0x004455;
            }
        }

        // Diamond icon
        int dotColor = (0xFF000000) | (stateColor & 0x00FFFFFF);
        g.drawString(Minecraft.getInstance().font, "\u25C8", x, y + 12, dotColor, false);
        g.drawString(Minecraft.getInstance().font, stateLabel, x + 10, y + 12, stateColor, false);

        // ── Shield bar ────────────────────────────────────────────────────────
        if (shieldActive || shieldRadius > 0.1f) {
            float barFill  = Math.min(shieldRadius / 2.5f, 1.0f);
            int   barWidth = (int)(barFill * (panelW - 4));

            // Background
            g.fill(x, y + 26, x + panelW - 4, y + 33, 0xFF0A1A22);
            // Fill
            int shieldAlpha = shieldActive ? 0xFF : (int)(pulse * 180 + 75);
            int shieldColor = (shieldAlpha << 24) | 0xCCEEFF;
            g.fill(x, y + 26, x + barWidth, y + 33, shieldColor);
            // Label
            g.drawString(Minecraft.getInstance().font, "SHIELD",
                x + panelW - 38, y + 26,
                shieldActive ? 0xFFCCEEFF : 0xFF334455, false);
        } else {
            // Dim "SHIELD OFF" hint
            int dimColor = (int)(pulse * 40 + 20);
            g.drawString(Minecraft.getInstance().font, "SHIELD",
                x + panelW - 38, y + 26,
                (dimColor << 24) | 0x334455, false);
        }
    }
}