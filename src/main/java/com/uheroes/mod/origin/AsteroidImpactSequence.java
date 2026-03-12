package com.uheroes.mod.origin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

import java.util.Random;

/**
 * Movie-style asteroid cinematic.
 *
 * The camera is locked to face the crater (both yaw + pitch),
 * and the asteroid is drawn as a 2D screen-space fireball using
 * GuiGraphics.fill() (Embeddium-safe, no TRIANGLE_FAN).
 *
 * Timeline:
 *   0–15   : Fade from black, cinematic bars slide in, camera locks to sky
 *   15–75  : Fireball falls across screen toward crater direction
 *   75–85  : WHITE FLASH + heavy shake + explosion sound + particles
 *   85–110 : Smoke at crater, hold shot
 *   110–140: Bars out, camera releases to player
 *   140–170: Title text "— 3 years ago —"
 *   170    : End
 */
@OnlyIn(Dist.CLIENT)
public class AsteroidImpactSequence {

    public static BlockPos craterPos = null;

    private static boolean active              = false;
    private static int     tick                = 0;
    private static int     pendingDelay        = -1;

    // Overlay state
    private static float   barsProgress        = 0f;
    private static float   fadeAlpha           = 0f;
    private static boolean fadeWhite           = false;
    private static float   shakeIntensity      = 0f;

    // Camera lock: yaw toward crater, pitch slightly upward
    private static float   targetYaw           = 0f;
    private static float   cameraBlend         = 0f; // 0=player, 1=locked

    private static final Random RAND = new Random();

    // ── Entry points ──────────────────────────────────────────────────────────

    public static void start() {
        active          = true;
        tick            = 0;
        pendingDelay    = -1;
        barsProgress    = 0f;
        fadeAlpha       = 1f;   // start fully black
        fadeWhite       = false;
        shakeIntensity  = 0f;
        cameraBlend     = 0f;

        // Compute yaw toward crater from player position
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && craterPos != null) {
            double dx = craterPos.getX() - mc.player.getX();
            double dz = craterPos.getZ() - mc.player.getZ();
            targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        } else {
            targetYaw = mc != null && mc.player != null ? mc.player.getYRot() : 0f;
        }
    }

    public static void startDelayed(int ticks) {
        pendingDelay = ticks;
        active       = false;
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

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

        // Phase 1: intro 0–15
        if (tick <= 15) {
            float t      = tick / 15f;
            barsProgress = t;
            fadeAlpha    = 1f - t;
            fadeWhite    = false;
            cameraBlend  = t;
        }

        // Phase 2: fireball falling 15–75
        if (tick > 15 && tick <= 75) {
            barsProgress = 1f;
            fadeAlpha    = 0f;
            cameraBlend  = 1f;
            shakeIntensity = 0f;

            // Distant rumble at tick 40
            if (tick == 40 && mc.player != null) {
                var snd = ForgeRegistries.SOUND_EVENTS
                    .getValue(new ResourceLocation("entity.generic.explode"));
                if (snd != null) mc.level.playLocalSound(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    snd, SoundSource.AMBIENT, 0.6f, 0.3f, false);
            }
        }

        // Phase 3: IMPACT tick 75
        if (tick == 75) {
            fadeWhite      = true;
            fadeAlpha      = 1f;
            shakeIntensity = 10f;

            if (mc.player != null) {
                var snd = ForgeRegistries.SOUND_EVENTS
                    .getValue(new ResourceLocation("entity.generic.explode"));
                if (snd != null) mc.level.playLocalSound(
                    mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    snd, SoundSource.AMBIENT, 3.0f, 0.5f, false);
            }
            if (craterPos != null && mc.level != null) {
                for (int i = 0; i < 6; i++) {
                    double ox = (RAND.nextDouble() - 0.5) * 10;
                    double oz = (RAND.nextDouble() - 0.5) * 10;
                    mc.level.addParticle(ParticleTypes.EXPLOSION_EMITTER,
                        craterPos.getX() + ox, craterPos.getY() + 1, craterPos.getZ() + oz,
                        0, 0, 0);
                }
            }
        }

        // Flash fades 75–85
        if (tick > 75 && tick <= 85) {
            float t        = (tick - 75) / 10f;
            shakeIntensity = 10f * (1f - t);
            fadeAlpha      = 1f - t;
            fadeWhite      = true;
        }

        // Phase 4: smoke hold 85–110
        if (tick > 85 && tick <= 110) {
            fadeAlpha      = 0f;
            fadeWhite      = false;
            shakeIntensity = 0f;

            if (craterPos != null && mc.level != null && tick % 2 == 0) {
                for (int i = 0; i < 4; i++) {
                    double ox = (RAND.nextDouble() - 0.5) * 7;
                    double oz = (RAND.nextDouble() - 0.5) * 7;
                    mc.level.addParticle(ParticleTypes.LARGE_SMOKE,
                        craterPos.getX() + ox, craterPos.getY() + 1, craterPos.getZ() + oz,
                        0, 0.12, 0);
                }
            }
        }

        // Phase 5: bars out + camera releases 110–140
        if (tick > 110 && tick <= 140) {
            float t      = (tick - 110) / 30f;
            barsProgress = 1f - t;
            cameraBlend  = 1f - t;
        }

        // Phase 6: title 140
        if (tick == 140 && mc.gui != null) {
            mc.gui.setTitle(Component.literal("§7— 3 years ago —"));
            mc.gui.setSubtitle(Component.literal("§bSomething fell from the sky..."));
            mc.gui.setTimes(10, 70, 30);
        }

        // End 170
        if (tick >= 170) {
            active         = false;
            tick           = 0;
            barsProgress   = 0f;
            fadeAlpha      = 0f;
            shakeIntensity = 0f;
            cameraBlend    = 0f;
        }
    }

    // ── Camera lock ───────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!active && cameraBlend <= 0) return;

        if (shakeIntensity > 0) {
            event.setYaw  (event.getYaw()   + (RAND.nextFloat() - 0.5f) * 2f * shakeIntensity);
            event.setPitch(event.getPitch() + (RAND.nextFloat() - 0.5f) * 2f * shakeIntensity);
        }

        if (cameraBlend > 0f) {
            // Lock yaw toward crater
            float curYaw   = (float) event.getYaw();
            float blendYaw = lerpAngle(curYaw, targetYaw, cameraBlend);
            event.setYaw(blendYaw);

            // Lock pitch to look slightly up (-30°) to see the sky
            float curPitch   = (float) event.getPitch();
            float blendPitch = curPitch + (-30f - curPitch) * cameraBlend;
            event.setPitch(blendPitch);
        }
    }

    private static float lerpAngle(float from, float to, float t) {
        float diff = ((to - from) % 360 + 540) % 360 - 180;
        return from + diff * t;
    }

    // ── Overlay ───────────────────────────────────────────────────────────────
    // Uses only GuiGraphics.fill() — fully Embeddium-safe, no TRIANGLE_FAN.

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!active && pendingDelay < 0) return;

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics g = event.getGuiGraphics();
        int W = mc.getWindow().getGuiScaledWidth();
        int H = mc.getWindow().getGuiScaledHeight();

        // Full-screen fade (black or white)
        if (fadeAlpha > 0f) {
            int a = (int)(fadeAlpha * 255);
            int col = fadeWhite
                ? argb(a, 255, 255, 255)
                : argb(a, 0, 0, 0);
            g.fill(0, 0, W, H, col);
        }

        // Cinematic bars
        if (barsProgress > 0f) {
            int barH = (int)(H * 0.10f * barsProgress);
            g.fill(0, 0,       W, barH,   argb(255, 0, 0, 0));
            g.fill(0, H-barH,  W, H,      argb(255, 0, 0, 0));
        }

        // Asteroid fireball — only during phase 2 (ticks 15–75)
        if (active && tick >= 15 && tick < 75) {
            float p = easeIn((tick - 15) / 60f); // 0→1

            // Trajectory: falls from upper-center toward lower-center
            // (camera is locked to face crater, so this matches the "sky above crater" view)
            float startX = W * 0.50f;
            float startY = H * 0.03f;  // near top of screen
            float endX   = W * 0.48f;
            float endY   = H * 0.62f;  // lower area (above cinematic bar)

            float ax = startX + (endX - startX) * p;
            float ay = startY + (endY - startY) * p;

            // Size grows with proximity
            int size = Math.max(2, (int)(3 + 18 * p));

            // Trail
            int trailCount = 10;
            for (int i = trailCount; i >= 1; i--) {
                float tp  = Math.max(0, p - i * 0.018f);
                float tx  = startX + (endX - startX) * easeIn(tp);
                float ty  = startY + (endY - startY) * easeIn(tp);
                int   ts  = Math.max(1, (int)(size * 0.6f * (1f - (float) i / trailCount)));
                int   ta  = (int)((1f - (float) i / trailCount) * 0.5f * 255);
                if (ta > 3) fillRect(g, tx, ty, ts, argb(ta, 255, 110, 20));
            }

            // Glow layers (outer → inner)
            int sizeL  = (int)(size * 2.8f);
            int sizeMid= (int)(size * 1.8f);
            fillRect(g, ax, ay, sizeL,   argb(80,  255,  60,  0));
            fillRect(g, ax, ay, sizeMid, argb(160, 255, 150, 30));
            fillRect(g, ax, ay, size,    argb(255, 255, 225, 80));
            fillRect(g, ax, ay, Math.max(1, size/3), argb(255, 255, 255, 240));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static float easeIn(float t) {
        return t * t;
    }

    /** Pack ARGB components into a single int (Minecraft fill format) */
    private static int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Draw a centred square of side length size*2 at (cx, cy) */
    private static void fillRect(GuiGraphics g, float cx, float cy, int halfSize, int color) {
        int x1 = (int)(cx - halfSize);
        int y1 = (int)(cy - halfSize);
        int x2 = (int)(cx + halfSize);
        int y2 = (int)(cy + halfSize);
        g.fill(x1, y1, x2, y2, color);
    }
}