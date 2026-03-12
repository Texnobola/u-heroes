package com.uheroes.mod.origin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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

@OnlyIn(Dist.CLIENT)
public class AsteroidImpactSequence {

    public static BlockPos craterPos = null;

    private static boolean active = false;
    private static int tickCounter = 0;
    private static float shakeIntensity = 0f;
    private static float fadeAlpha = 0f;
    private static int pendingDelay = -1;
    private static final Random RAND = new Random();

    public static void start() {
        active = true;
        tickCounter = 0;
        shakeIntensity = 0f;
        fadeAlpha = 0f;
        pendingDelay = -1;
    }

    // Call this from the packet — waits for world to fully load before starting
    public static void startDelayed(int ticks) {
        pendingDelay = ticks;
        active = false;
        tickCounter = 0;
        shakeIntensity = 0f;
        fadeAlpha = 0f;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Handle pending delay — wait for world to be ready
        if (pendingDelay >= 0) {
            Minecraft _mc = Minecraft.getInstance();
            if (_mc.player != null && _mc.level != null) {
                pendingDelay--;
                if (pendingDelay <= 0) {
                    start();
                }
            }
            return;
        }

        if (!active) return;
        tickCounter++;
        Minecraft mc = Minecraft.getInstance();

        // (0.5s = 10 ticks) Distant rumble
        if (tickCounter == 10 && mc.player != null) {
            net.minecraft.sounds.SoundEvent _snd1 = ForgeRegistries.SOUND_EVENTS
                .getValue(new ResourceLocation("entity.generic.explode"));
            if (_snd1 != null) mc.level.playLocalSound(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                _snd1, SoundSource.AMBIENT, 1.5f, 0.4f, false
            );
        }

        // (0.5s → 1.5s = 10-30 ticks) Screen shake
        if (tickCounter >= 10 && tickCounter <= 30) {
            float progress = (tickCounter - 10) / 20f;
            shakeIntensity = 3.0f * (1f - progress); // Decreases over time
        } else if (tickCounter > 30) {
            shakeIntensity = 0f;
        }

        // (1.5s → 2.5s = 30-50 ticks) Fade to black
        if (tickCounter >= 30 && tickCounter <= 50) {
            fadeAlpha = (tickCounter - 30) / 20f;
        }

        // (2.5s = 50 ticks) Impact sound
        if (tickCounter == 50 && mc.player != null) {
            net.minecraft.sounds.SoundEvent _snd2 = ForgeRegistries.SOUND_EVENTS
                .getValue(new ResourceLocation("entity.generic.explode"));
            if (_snd2 != null) mc.level.playLocalSound(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                _snd2, SoundSource.AMBIENT, 2.0f, 0.6f, false
            );
        }

        // (3.0s → 4.0s = 60-80 ticks) Fade back in
        if (tickCounter >= 60 && tickCounter <= 80) {
            fadeAlpha = 1f - ((tickCounter - 60) / 20f);
        } else if (tickCounter > 80) {
            fadeAlpha = 0f;
        }

        // (4.0s = 80 ticks) Spawn particles at crater
        if (tickCounter == 80 && craterPos != null && mc.level != null) {
            // 3 explosion emitters
            for (int i = 0; i < 3; i++) {
                double offsetX = (RAND.nextDouble() - 0.5) * 6;
                double offsetZ = (RAND.nextDouble() - 0.5) * 6;
                mc.level.addParticle(ParticleTypes.EXPLOSION_EMITTER,
                    craterPos.getX() + offsetX,
                    craterPos.getY() + 2,
                    craterPos.getZ() + offsetZ,
                    0, 0, 0);
            }

            // 20 large smoke particles
            for (int i = 0; i < 20; i++) {
                double offsetX = (RAND.nextDouble() - 0.5) * 6;
                double offsetY = RAND.nextDouble() * 3;
                double offsetZ = (RAND.nextDouble() - 0.5) * 6;
                mc.level.addParticle(ParticleTypes.LARGE_SMOKE,
                    craterPos.getX() + offsetX,
                    craterPos.getY() + offsetY,
                    craterPos.getZ() + offsetZ,
                    0, 0.1, 0);
            }
        }

        // (4.5s = 90 ticks) Show subtitle
        if (tickCounter == 90 && mc.gui != null) {
            mc.gui.setTitle(Component.empty());
            mc.gui.setSubtitle(Component.literal("§bSomething crashed nearby..."));
            mc.gui.setTimes(10, 60, 20);
        }

        // End sequence at 5.5s (110 ticks)
        if (tickCounter >= 110) {
            active = false;
            tickCounter = 0;
            shakeIntensity = 0f;
            fadeAlpha = 0f;
        }
    }

    @SubscribeEvent
    public static void onCameraShake(ViewportEvent.ComputeCameraAngles event) {
        if (!active || shakeIntensity <= 0) return;

        float shakeYaw = (RAND.nextFloat() - 0.5f) * 2f * shakeIntensity;
        float shakePitch = (RAND.nextFloat() - 0.5f) * 2f * shakeIntensity;

        event.setYaw(event.getYaw() + shakeYaw);
        event.setPitch(event.getPitch() + shakePitch);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!active || fadeAlpha <= 0) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        // Draw black overlay
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int alpha = (int) (fadeAlpha * 255);
        buffer.vertex(matrix, 0, height, 0).color(0, 0, 0, alpha).endVertex();
        buffer.vertex(matrix, width, height, 0).color(0, 0, 0, alpha).endVertex();
        buffer.vertex(matrix, width, 0, 0).color(0, 0, 0, alpha).endVertex();
        buffer.vertex(matrix, 0, 0, 0).color(0, 0, 0, alpha).endVertex();

        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }
}