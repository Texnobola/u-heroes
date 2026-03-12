package com.uheroes.mod.origin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.Random;

/**
 * Movie-style asteroid impact cinematic.
 *
 * Timeline (ticks at 20tps):
 *   0  –  15 : Cinematic bars in, fade from black, camera sweeps to sky
 *   15 –  75 : Asteroid fireball falls across screen (3 seconds)
 *   75 –  85 : IMPACT — white flash + heavy shake + explosion sound + particles
 *   85 – 100 : Smoke lingers, camera holds
 *  100 – 130 : Bars fade out, camera releases to player
 *  130 – 160 : "3 years ago..." title + subtitle
 *  160        : End
 */
@OnlyIn(Dist.CLIENT)
public class AsteroidImpactSequence {

    public static BlockPos craterPos = null;

    private static boolean active             = false;
    private static int     tick               = 0;
    private static int     pendingDelay       = -1;

    private static float   barsProgress       = 0f;
    private static float   fadeAlpha          = 0f;
    private static boolean fadeWhite          = false;
    private static float   shakeIntensity     = 0f;
    private static float   cameraOverridePitch= 0f;
    private static float   cameraOverrideBlend= 0f;

    private static final Random RAND = new Random();

    // ── Entry points ──────────────────────────────────────────────────────────

    public static void start() {
        active              = true;
        tick                = 0;
        pendingDelay        = -1;
        barsProgress        = 0f;
        fadeAlpha           = 1f;
        fadeWhite           = false;
        shakeIntensity      = 0f;
        cameraOverridePitch = -65f;
        cameraOverrideBlend = 0f;
    }

    public static void startDelayed(int ticks) {
        pendingDelay = ticks;
        active       = false;
    }

    // ── Client tick ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (pendingDelay >= 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null) {
                pendingDelay--;
                if (pendingDelay <= 0) start();
            }
            return;
        }

        if (!active) return;
        tick++;
        Minecraft mc = Minecraft.getInstance();

        // Phase 1: intro (0-15) - fade in from black, bars in, camera up
        if (tick <= 15) {
            float t = tick / 15f;
            barsProgress        = t;
            fadeAlpha           = 1f - t;
            fadeWhite           = false;
            cameraOverrideBlend = t;
        }

        // Phase 2: asteroid falling (15-75)
        if (tick > 15 && tick <= 75) {
            barsProgress        = 1f;
            fadeAlpha           = 0f;
            cameraOverrideBlend = 1f;
            shakeIntensity      = 0f;

            // Soft distant rumble at tick 35
            if (tick == 35 && mc.player != null) {
                var snd = ForgeRegistries.SOUND_EVENTS
                    .getValue(new ResourceLocation("entity.generic.explode"));
                if (snd != null) mc.level.playLocalSound(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    snd, SoundSource.AMBIENT, 0.7f, 0.3f, false);
            }
        }

        // Phase 3: IMPACT (tick 75)
        if (tick == 75) {
            fadeWhite      = true;
            fadeAlpha      = 1f;
            shakeIntensity = 9f;

            if (mc.player != null) {
                var snd = ForgeRegistries.SOUND_EVENTS
                    .getValue(new ResourceLocation("entity.generic.explode"));
                if (snd != null) mc.level.playLocalSound(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    snd, SoundSource.AMBIENT, 3.0f, 0.5f, false);
            }
            if (craterPos != null && mc.level != null) {
                for (int i = 0; i < 5; i++) {
                    double ox = (RAND.nextDouble() - 0.5) * 8;
                    double oz = (RAND.nextDouble() - 0.5) * 8;
                    mc.level.addParticle(ParticleTypes.EXPLOSION_EMITTER,
                        craterPos.getX() + ox, craterPos.getY() + 1, craterPos.getZ() + oz,
                        0, 0, 0);
                }
            }
        }

        if (tick > 75 && tick <= 85) {
            float t = (tick - 75) / 10f;
            shakeIntensity = 9f * (1f - t);
            fadeAlpha      = 1f - t;
            fadeWhite      = true;
        }

        // Phase 4: smoke lingers (85-100)
        if (tick > 85 && tick <= 100) {
            fadeAlpha  = 0f;
            fadeWhite  = false;
            shakeIntensity = 0f;

            if (craterPos != null && mc.level != null && tick % 2 == 0) {
                for (int i = 0; i < 3; i++) {
                    double ox = (RAND.nextDouble() - 0.5) * 6;
                    double oz = (RAND.nextDouble() - 0.5) * 6;
                    mc.level.addParticle(ParticleTypes.LARGE_SMOKE,
                        craterPos.getX() + ox, craterPos.getY() + 1, craterPos.getZ() + oz,
                        0, 0.15, 0);
                }
            }
        }

        // Phase 5: bars out, camera releases (100-130)
        if (tick > 100 && tick <= 130) {
            float t = (tick - 100) / 30f;
            barsProgress        = 1f - t;
            cameraOverrideBlend = 1f - t;
        }

        // Phase 6: title (tick 130)
        if (tick == 130 && mc.gui != null) {
            mc.gui.setTitle(Component.literal("§7— 3 years ago —"));
            mc.gui.setSubtitle(Component.literal("§bSomething fell from the sky..."));
            mc.gui.setTimes(10, 60, 30);
        }

        // End
        if (tick >= 160) {
            active              = false;
            tick                = 0;
            barsProgress        = 0f;
            fadeAlpha           = 0f;
            shakeIntensity      = 0f;
            cameraOverrideBlend = 0f;
        }
    }

    // ── Camera ────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!active) return;

        if (shakeIntensity > 0) {
            event.setYaw  (event.getYaw()   + (RAND.nextFloat() - 0.5f) * 2f * shakeIntensity);
            event.setPitch(event.getPitch() + (RAND.nextFloat() - 0.5f) * 2f * shakeIntensity);
        }

        if (cameraOverrideBlend > 0f) {
            float blended = (float) event.getPitch()
                + (cameraOverridePitch - (float) event.getPitch()) * cameraOverrideBlend;
            event.setPitch(blended);
        }
    }

    // ── Overlay ───────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!active && pendingDelay < 0) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics g = event.getGuiGraphics();
        int W         = mc.getWindow().getGuiScaledWidth();
        int H         = mc.getWindow().getGuiScaledHeight();

        // Black / white full-screen fade
        if (fadeAlpha > 0f) {
            int alpha = (int)(fadeAlpha * 255);
            int r = fadeWhite ? 255 : 0;
            int gb = fadeWhite ? 255 : 0;
            drawRect(g, 0, 0, W, H, r, gb, gb, alpha);
        }

        // Cinematic bars
        if (barsProgress > 0f) {
            int barH = (int)(H * 0.1f * barsProgress);
            drawRect(g, 0, 0,       W, barH, 0, 0, 0, 255);
            drawRect(g, 0, H-barH,  W, barH, 0, 0, 0, 255);
        }

        // Asteroid fireball during phase 2 (ticks 15-75)
        if (active && tick >= 15 && tick < 75) {
            float progress = (tick - 15) / 60f; // 0→1

            // Screen-space trajectory: upper-center → lower-center with slight drift
            float startX = W * 0.5f;
            float startY = H * 0.04f;
            float endX   = W * 0.47f;
            float endY   = H * 0.70f;

            float p   = easeIn(progress);
            float astX = startX + (endX - startX) * p;
            float astY = startY + (endY - startY) * p;

            // Size grows with proximity (perspective)
            float size = 3f + 22f * p;

            // Trail (draw first, behind the ball)
            for (int i = 14; i >= 1; i--) {
                float tp    = Math.max(0, progress - i * 0.012f);
                float trailX = startX + (endX - startX) * easeIn(tp);
                float trailY = startY + (endY - startY) * easeIn(tp);
                float ta    = (1f - (float) i / 14f) * 0.35f;
                float ts    = size * 0.55f * (1f - (float) i / 14f);
                int alpha   = (int)(ta * 255);
                if (alpha > 2) drawCircle(g, trailX, trailY, ts, 255, 130, 20, alpha);
            }

            // Outer halo (orange, semi-transparent)
            drawCircle(g, astX, astY, size * 2.5f, 255,  60,  0,  (int)(0.35f * 255));
            // Mid glow (bright orange)
            drawCircle(g, astX, astY, size * 1.6f, 255, 160, 30,  (int)(0.65f * 255));
            // Core (yellow-white)
            drawCircle(g, astX, astY, size,        255, 230, 90,  255);
            // Hot center (pure white)
            drawCircle(g, astX, astY, size * 0.35f, 255, 255, 245, 255);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static float easeIn(float t) {
        return t * t;
    }

    private static void drawRect(GuiGraphics g, int x, int y, int w, int h,
                                  int r, int gr, int b, int a) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f m   = g.pose().last().pose();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buf.vertex(m, x,     y + h, 0).color(r, gr, b, a).endVertex();
        buf.vertex(m, x + w, y + h, 0).color(r, gr, b, a).endVertex();
        buf.vertex(m, x + w, y,     0).color(r, gr, b, a).endVertex();
        buf.vertex(m, x,     y,     0).color(r, gr, b, a).endVertex();
        BufferUploader.drawWithShader(buf.end());
        RenderSystem.disableBlend();
    }

    private static void drawCircle(GuiGraphics g, float cx, float cy, float radius,
                                    int r, int gr, int b, int a) {
        if (radius < 0.5f || a <= 0) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f m   = g.pose().last().pose();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        buf.vertex(m, cx, cy, 0).color(r, gr, b, a).endVertex();
        int seg = 24;
        for (int i = 0; i <= seg; i++) {
            double angle = (2 * Math.PI * i) / seg;
            buf.vertex(m,
                (float)(cx + Math.cos(angle) * radius),
                (float)(cy + Math.sin(angle) * radius),
                0).color(r, gr, b, a).endVertex();
        }

        BufferUploader.drawWithShader(buf.end());
        RenderSystem.disableBlend();
    }
}