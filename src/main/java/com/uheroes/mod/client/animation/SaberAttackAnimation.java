package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

public class SaberAttackAnimation implements IAnimation {

    public static final SaberAttackAnimation INSTANCE = new SaberAttackAnimation();
    private SaberAttackAnimation() {}

    private boolean active = false;
    private int type = 0;
    private float progress = 0f;
    private static final float SPEED = 0.18f;

    public void play(int attackType) {
        this.type = attackType;
        this.progress = 0f;
        this.active = true;
    }

    @Override public boolean isActive() { return active; }

    @Override
    public void setupAnim(float tickDelta) {
        if (!active) return;
        progress += SPEED;
        if (progress >= 1f) {
            progress = 1f;
            active = false;
        }
    }

    private float ease(float t) {
        return t * t * (3f - 2f * t);
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (!active && progress == 0f) return store;
        float t = ease(progress);

        switch (this.type) {
            case 0 -> {
                if (type == TransformType.ROTATION) {
                    return switch (modelName) {
                        case "rightArm" -> new Vec3f(
                            lerp(t, (float) Math.toRadians(-30), (float) Math.toRadians(-10)),
                            lerp(t, (float) Math.toRadians(-80), (float) Math.toRadians(60)),
                            lerp(t, (float) Math.toRadians(-20), (float) Math.toRadians(30))
                        );
                        case "torso" -> new Vec3f(
                            0f,
                            lerp(t, (float) Math.toRadians(25), (float) Math.toRadians(-15)),
                            0f
                        );
                        default -> store;
                    };
                }
            }
            case 1 -> {
                if (type == TransformType.ROTATION) {
                    return switch (modelName) {
                        case "rightArm" -> new Vec3f(
                            lerp(t, (float) Math.toRadians(-160), (float) Math.toRadians(40)),
                            lerp(t, (float) Math.toRadians(-20), (float) Math.toRadians(10)),
                            lerp(t, (float) Math.toRadians(-15), (float) Math.toRadians(20))
                        );
                        case "leftArm" -> new Vec3f(
                            lerp(t, (float) Math.toRadians(-60), (float) Math.toRadians(-20)),
                            0f,
                            (float) Math.toRadians(15)
                        );
                        case "torso" -> new Vec3f(
                            lerp(t, (float) Math.toRadians(-10), (float) Math.toRadians(15)),
                            lerp(t, (float) Math.toRadians(15), (float) Math.toRadians(-10)),
                            0f
                        );
                        default -> store;
                    };
                }
            }
            case 2 -> {
                if (type == TransformType.ROTATION) {
                    return switch (modelName) {
                        case "rightArm" -> new Vec3f(
                            lerp(t, (float) Math.toRadians(-90), (float) Math.toRadians(-20)),
                            lerp(t, (float) Math.toRadians(20), 0f),
                            (float) Math.toRadians(-10)
                        );
                        case "leftArm" -> new Vec3f(
                            lerp(t, (float) Math.toRadians(-70), (float) Math.toRadians(-10)),
                            lerp(t, (float) Math.toRadians(-20), 0f),
                            (float) Math.toRadians(10)
                        );
                        case "torso" -> new Vec3f(
                            lerp(t, (float) Math.toRadians(-15), (float) Math.toRadians(5)),
                            0f, 0f
                        );
                        default -> store;
                    };
                } else if (type == TransformType.POSITION && modelName.equals("rightArm")) {
                    return new Vec3f(0f, lerp(t, -1.5f, 0f), lerp(t, -2f, 0f));
                }
            }
        }
        return store;
    }

    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }
}
