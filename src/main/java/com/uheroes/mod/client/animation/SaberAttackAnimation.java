package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

/**
 * Three saber attack animations with windup → strike → recovery phases.
 *
 * Type 0 — Horizontal slash (right to left sweep)
 * Type 1 — Overhead diagonal slam (top-right down to bottom-left)
 * Type 2 — Forward thrust / stab
 *
 * Phase speeds (progress per frame at ~60fps):
 *   Windup   — 0.04  (~25 frames, ~0.42s) slow wind-back
 *   Strike   — 0.30  (~3-4 frames)        snappy impact
 *   Recovery — 0.028 (~36 frames, ~0.6s)  slow follow-through
 */
public class SaberAttackAnimation implements IAnimation {

    public static final SaberAttackAnimation INSTANCE = new SaberAttackAnimation();
    private SaberAttackAnimation() {}

    // ── State ───────────────────────────────────────────────────
    private boolean active = false;
    private int attackType = 0;

    private static final int PHASE_WINDUP   = 0;
    private static final int PHASE_STRIKE   = 1;
    private static final int PHASE_RECOVERY = 2;

    private int   phase     = PHASE_WINDUP;
    private float phaseT    = 0f;   // 0 → 1 within each phase

    private static final float WINDUP_SPEED   = 0.040f;
    private static final float STRIKE_SPEED   = 0.300f;
    private static final float RECOVERY_SPEED = 0.028f;

    // ── Control ─────────────────────────────────────────────────
    public void play(int type) {
        this.attackType = type;
        this.phase      = PHASE_WINDUP;
        this.phaseT     = 0f;
        this.active     = true;
    }

    @Override public boolean isActive() { return active; }

    // ── Tick ────────────────────────────────────────────────────
    @Override
    public void setupAnim(float tickDelta) {
        if (!active) return;

        float speed = switch (phase) {
            case PHASE_WINDUP   -> WINDUP_SPEED;
            case PHASE_STRIKE   -> STRIKE_SPEED;
            default             -> RECOVERY_SPEED;
        };

        phaseT += speed;
        if (phaseT >= 1f) {
            phaseT = 0f;
            phase++;
            if (phase > PHASE_RECOVERY) {
                active = false;
                phase  = PHASE_WINDUP;
            }
        }
    }

    // ── Easing ──────────────────────────────────────────────────
    /** Smooth-step — used for windup and recovery */
    private float smooth(float t) { return t * t * (3f - 2f * t); }

    /** Ease-out cubic — strike snaps in and decelerates at end */
    private float easeOut(float t) { float u = 1f - t; return 1f - u * u * u; }

    private float lerp(float a, float b, float t) { return a + (b - a) * t; }

    // ── Bone helper ─────────────────────────────────────────────
    /**
     * Blend between three keyframes: rest → windup → strike → recovery → rest
     * Returns the correct value for the current phase.
     */
    private float key(float rest, float windup, float strike) {
        return switch (phase) {
            case PHASE_WINDUP   -> lerp(rest,   windup, smooth(phaseT));
            case PHASE_STRIKE   -> lerp(windup, strike, easeOut(phaseT));
            default /*RECOVERY*/-> lerp(strike, rest,   smooth(phaseT));
        };
    }

    // ── Main transform ──────────────────────────────────────────
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type,
                                float tickDelta, Vec3f store) {
        if (!active) return store;

        return switch (attackType) {
            case 0 -> horizontalSlash(modelName, type, store);
            case 1 -> overheadSlam   (modelName, type, store);
            case 2 -> thrustStab     (modelName, type, store);
            default -> store;
        };
    }

    // ────────────────────────────────────────────────────────────
    // Type 0 — Horizontal Slash  (right → left sweep)
    //   Windup:   right arm swings back-right, body rotates right
    //   Strike:   arm sweeps hard left across body, body snaps left
    //   Recovery: slow return to neutral
    // ────────────────────────────────────────────────────────────
    private Vec3f horizontalSlash(String bone, TransformType type, Vec3f store) {
        if (type == TransformType.ROTATION) {
            return switch (bone) {
                case "rightArm" -> new Vec3f(
                    rad(key(-20,  -30,  20)),   // pitch: slight droop in windup, rise on strike
                    rad(key(  0,  -70,  80)),   // yaw:   pull right, sweep hard left
                    rad(key(  0,  -30,  20))    // roll:  tilt back, snap forward
                );
                case "leftArm" -> new Vec3f(
                    rad(key(  0,  -30, -15)),
                    rad(key(  0,   20, -20)),
                    rad(key(  0,   15,  -5))
                );
                case "body" -> new Vec3f(
                    rad(key(  0,    5,   5)),
                    rad(key(  0,   30, -25)),   // body coils right, uncoils left
                    rad(key(  0,    0,   0))
                );
                default -> store;
            };
        }
        return store;
    }

    // ────────────────────────────────────────────────────────────
    // Type 1 — Overhead Diagonal Slam  (top-right → bottom-left)
    //   Windup:   arm lifts high and tilts right, body leans back
    //   Strike:   arm hammers diagonally down-left, fast
    //   Recovery: arm hangs low, body straightens
    // ────────────────────────────────────────────────────────────
    private Vec3f overheadSlam(String bone, TransformType type, Vec3f store) {
        if (type == TransformType.ROTATION) {
            return switch (bone) {
                case "rightArm" -> new Vec3f(
                    rad(key(-20, -160,  60)),   // pitch: swings from high up to slammed down
                    rad(key(  0,  -30,  20)),   // yaw:   right to slightly left
                    rad(key(  0,  -20,  30))    // roll
                );
                case "leftArm" -> new Vec3f(
                    rad(key(  0,  -50, -15)),   // left arm raises for balance in windup
                    rad(key(  0,  -15,  10)),
                    rad(key(  0,   20,   5))
                );
                case "body" -> new Vec3f(
                    rad(key(  0,  -15,  20)),   // body arches back in windup, crunches on slam
                    rad(key(  0,   20, -10)),
                    rad(key(  0,    0,   0))
                );
                default -> store;
            };
        }
        if (type == TransformType.POSITION && bone.equals("rightArm")) {
            // arm lifts up in windup, slams down on strike
            float y = key(0f, -2.5f, 1.5f);
            return new Vec3f(0f, y, 0f);
        }
        return store;
    }

    // ────────────────────────────────────────────────────────────
    // Type 2 — Thrust / Stab  (lunge straight forward)
    //   Windup:   both arms pull back to hip, body leans back
    //   Strike:   arms extend fully forward, body lunges
    //   Recovery: slow draw back
    // ────────────────────────────────────────────────────────────
    private Vec3f thrustStab(String bone, TransformType type, Vec3f store) {
        if (type == TransformType.ROTATION) {
            return switch (bone) {
                case "rightArm" -> new Vec3f(
                    rad(key(-20,   30, -100)),  // pulls down in windup, then stabs forward hard
                    rad(key(  0,   15,  -10)),
                    rad(key(  0,  -15,  -10))
                );
                case "leftArm" -> new Vec3f(
                    rad(key(  0,   20,  -80)),
                    rad(key(  0,  -15,   10)),
                    rad(key(  0,   15,   10))
                );
                case "body" -> new Vec3f(
                    rad(key(  0,   15,  -20)),  // body leans back in windup, lunges forward
                    rad(key(  0,    0,    0)),
                    rad(key(  0,    0,    0))
                );
                default -> store;
            };
        }
        if (type == TransformType.POSITION) {
            if (bone.equals("rightArm")) {
                float z = key(0f, 1.5f, -3f);  // pull back then lunge forward
                return new Vec3f(0f, 0f, z);
            }
            if (bone.equals("leftArm")) {
                float z = key(0f, 1.2f, -2.5f);
                return new Vec3f(0f, 0f, z);
            }
        }
        return store;
    }

    private float rad(float deg) { return (float) Math.toRadians(deg); }
}