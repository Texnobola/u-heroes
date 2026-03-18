package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * Custom nano-suit walking animation played via PlayerAnimator.
 *
 * <p>When the player is moving with the Nano Suit equipped:
 * <ul>
 *   <li>Body leans slightly forward (combat-ready)</li>
 *   <li>Arms held in a guard/ready position rather than vanilla swing</li>
 *   <li>Head tilt compensates for body lean</li>
 *   <li>Legs have a slightly wider, more planted stride</li>
 * </ul>
 *
 * <p>Activated by {@link NanoSuitWalkAnimHandler}. Speed-responsive: fast
 * movement = stronger lean.
 */
public class NanoSuitWalkAnimation implements IAnimation {

    public static final NanoSuitWalkAnimation INSTANCE = new NanoSuitWalkAnimation();
    private NanoSuitWalkAnimation() {}

    private boolean active = false;
    private float   blendFactor = 0f;   // 0–1, smoothly transitions in/out
    private float   walkTime    = 0f;   // advances while moving, drives stride

    // ─── Activation ──────────────────────────────────────────────────────────

    public void setActive(boolean a) { active = a; }

    @Override
    public boolean isActive() { return active || blendFactor > 0.01f; }

    // ─── Tick (called by handler) ─────────────────────────────────────────────

    public void tick(float speed) {
        // Blend in/out smoothly
        float target = active ? 1.0f : 0.0f;
        blendFactor += (target - blendFactor) * 0.18f;

        // Walk cycle advances proportionally to movement speed
        if (active) walkTime += speed * 2.5f;
    }

    // ─── PlayerAnimator transform ─────────────────────────────────────────────

    @Override
    public void setupAnim(float tickDelta) { }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type,
                                float tickDelta, Vec3f store) {
        if (blendFactor < 0.01f) return store;

        float b = blendFactor;
        float t = walkTime + tickDelta * 2.5f;

        if (type == TransformType.ROTATION) {
            return switch (modelName) {
                // Body: lean forward, slight side-to-side sway
                case "body" -> blend(store,
                    new Vec3f(0.08f, Mth.sin(t * 0.3f) * 0.02f, 0f), b);

                // Head: compensates for body lean so player still looks straight
                case "head" -> blend(store,
                    new Vec3f(-0.08f, 0f, 0f), b);

                // Right arm: lowered into guard position, small front-back swing
                case "rightArm" -> blend(store,
                    new Vec3f(-0.5f + Mth.sin(t) * 0.15f, -0.1f, 0.05f), b);

                // Left arm: mirror guard position
                case "leftArm" -> blend(store,
                    new Vec3f(-0.5f + Mth.sin(t + (float)Math.PI) * 0.15f, 0.1f, -0.05f), b);

                // Right leg: wider planted stride
                case "rightLeg" -> blend(store,
                    new Vec3f(Mth.sin(t) * 0.6f, 0f, 0.05f), b);

                // Left leg: opposite phase
                case "leftLeg" -> blend(store,
                    new Vec3f(Mth.sin(t + (float)Math.PI) * 0.6f, 0f, -0.05f), b);

                default -> store;
            };
        }

        if (type == TransformType.POSITION) {
            return switch (modelName) {
                // Slight bob when walking
                case "body" -> blend(store,
                    new Vec3f(0f, -Math.abs(Mth.sin(t)) * 0.03f * b, 0f), b);
                default -> store;
            };
        }

        return store;
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Vec3f blend(Vec3f original, Vec3f target, float factor) {
        return new Vec3f(
            original.getX() + (target.getX() - original.getX()) * factor,
            original.getY() + (target.getY() - original.getY()) * factor,
            original.getZ() + (target.getZ() - original.getZ()) * factor
        );
    }
}