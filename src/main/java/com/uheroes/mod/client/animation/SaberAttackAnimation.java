package com.uheroes.mod.client.animation;

import com.mojang.math.Axis;
import com.mojang.math.Vector3f;
import com.uheroes.mod.UHeroesMod;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.renderer.PoseStack;

/**
 * Three saber attacks with windup → strike → recovery, tick-based timing.
 *
 * ANGLE LIMITS enforced to prevent arm-in-body clipping:
 *   Pitch: -120° to +60°   (was -155 which clipped through torso)
 *   Yaw:    -50° to +55°   (was ±85 which rotated arm inside body)
 *   Roll:   -25° to +25°
 */
public class SaberAttackAnimation implements IAnimation {

    public static final SaberAttackAnimation INSTANCE = new SaberAttackAnimation();
    private SaberAttackAnimation() {}

    // ── Phase timing (ticks at 20/s) ────────────────────────────
    private static final int PHASE_WINDUP   = 0;
    private static final int PHASE_STRIKE   = 1;
    private static final int PHASE_RECOVERY = 2;
    private static final int WINDUP_TICKS   = 8;
    private static final int STRIKE_TICKS   = 3;
    private static final int RECOVERY_TICKS = 14;

    // ── State ────────────────────────────────────────────────────
    private boolean active     = false;
    private int     attackType = 0;
    private int     phase      = PHASE_WINDUP;
    private int     phaseTick  = 0;
    private float   phaseT     = 0f;  // interpolated 0→1, set in setupAnim

    // ── Public API ───────────────────────────────────────────────
    public void play(int type) {
        attackType = type;
        phase      = PHASE_WINDUP;
        phaseTick  = 0;
        phaseT     = 0f;
        active     = true;
    }

    public void tick() {
        if (!active) return;
        phaseTick++;
        if (phaseTick >= phaseDuration()) {
            phaseTick = 0;
            phase++;
            if (phase > PHASE_RECOVERY) {
                active = false;
                phase  = PHASE_WINDUP;
            }
        }
    }

    public int  getPhase()      { return phase; }
    public float getPhaseT()    { return phaseT; }
    public int  getAttackType() { return attackType; }

    @Override public boolean isActive() { return active; }

    @Override
    public void setupAnim(float partialTick) {
        if (!active) return;
        phaseT = Math.min((phaseTick + partialTick) / phaseDuration(), 1f);
    }

    // ── Third-person bone transforms ─────────────────────────────
    @Override
    public Vec3f get3DTransform(String bone, TransformType type,
                                float partialTick, Vec3f store) {
        if (!active) return store;
        return switch (attackType) {
            case 0 -> horizontalSlash(bone, type, store);
            case 1 -> overheadSlam   (bone, type, store);
            case 2 -> thrustStab     (bone, type, store);
            default -> store;
        };
    }

    // ── First-person PoseStack transform ─────────────────────────
    /**
     * Call this from RenderHandEvent (main hand only) to animate
     * the first-person arm. Applies rotations relative to the hand pivot.
     */
    public void applyFirstPersonTransform(PoseStack ps) {
        if (!active) return;
        // Pivot point: move to wrist, rotate, move back
        ps.translate(0.56f, -0.52f, -0.72f);
        switch (attackType) {
            case 0 -> applyFP_HorizontalSlash(ps);
            case 1 -> applyFP_OverheadSlam(ps);
            case 2 -> applyFP_ThrustStab(ps);
        }
        ps.translate(-0.56f, 0.52f, 0.72f);
    }

    private void applyFP_HorizontalSlash(PoseStack ps) {
        // Windup: pull right; Strike: sweep left; Recovery: return
        float yaw   = rad(key(0, -45,  50));
        float pitch = rad(key(0, -15,  10));
        float roll  = rad(key(0, -20,  15));
        ps.mulPose(Axis.YP.rotation(yaw));
        ps.mulPose(Axis.XP.rotation(pitch));
        ps.mulPose(Axis.ZP.rotation(roll));
    }

    private void applyFP_OverheadSlam(PoseStack ps) {
        // Windup: raise high; Strike: slam down
        float pitch = rad(key(-5, -100,  45));
        float yaw   = rad(key( 0,  -20,  15));
        float roll  = rad(key( 0,  -15,  20));
        ps.mulPose(Axis.XP.rotation(pitch));
        ps.mulPose(Axis.YP.rotation(yaw));
        ps.mulPose(Axis.ZP.rotation(roll));
    }

    private void applyFP_ThrustStab(PoseStack ps) {
        // Windup: pull back; Strike: thrust forward
        float pitch = rad(key(-5,  20, -75));
        float z     = key(0f, 0.1f, -0.25f); // subtle forward push
        ps.mulPose(Axis.XP.rotation(pitch));
        ps.translate(0, 0, z);
    }

    // ── Type 0 — Horizontal Slash ────────────────────────────────
    private Vec3f horizontalSlash(String bone, TransformType type, Vec3f store) {
        if (type == TransformType.ROTATION) {
            return switch (bone) {
                case "rightArm" -> new Vec3f(
                    rad(key(-10, -25,  15)),  // pitch
                    rad(key(  0, -48,  52)),  // yaw  — SAFE range ±55°
                    rad(key(  0, -20,  15))   // roll
                );
                case "leftArm" -> new Vec3f(
                    rad(key(  0, -20, -10)),
                    rad(key(  0,  18, -18)),
                    rad(key(  0,  12,  -5))
                );
                case "body" -> new Vec3f(
                    rad(key(  0,   4,   4)),
                    rad(key(  0,  22, -20)),
                    rad(key(  0,   0,   0))
                );
                default -> store;
            };
        }
        return store;
    }

    // ── Type 1 — Overhead Slam ───────────────────────────────────
    private Vec3f overheadSlam(String bone, TransformType type, Vec3f store) {
        if (type == TransformType.ROTATION) {
            return switch (bone) {
                case "rightArm" -> new Vec3f(
                    rad(key(-10, -115,  55)),  // pitch  — was -155, now -115 (safe)
                    rad(key(  0,  -25,  18)),
                    rad(key(  0,  -18,  25))
                );
                case "leftArm" -> new Vec3f(
                    rad(key(  0,  -40, -12)),
                    rad(key(  0,  -12,  10)),
                    rad(key(  0,   18,   5))
                );
                case "body" -> new Vec3f(
                    rad(key(  0,  -12,  16)),
                    rad(key(  0,   18, -10)),
                    rad(key(  0,    0,   0))
                );
                default -> store;
            };
        }
        if (type == TransformType.POSITION && bone.equals("rightArm")) {
            return new Vec3f(0f, key(0f, -2f, 1.2f), 0f);
        }
        return store;
    }

    // ── Type 2 — Thrust / Stab ───────────────────────────────────
    private Vec3f thrustStab(String bone, TransformType type, Vec3f store) {
        if (type == TransformType.ROTATION) {
            return switch (bone) {
                case "rightArm" -> new Vec3f(
                    rad(key(-10,  22, -85)),  // pitch — was -100, now -85
                    rad(key(  0,  12,  -8)),
                    rad(key(  0, -12,  -8))
                );
                case "leftArm" -> new Vec3f(
                    rad(key(  0,  18, -70)),
                    rad(key(  0, -12,   8)),
                    rad(key(  0,  12,   8))
                );
                case "body" -> new Vec3f(
                    rad(key(  0,  12, -16)),
                    rad(key(  0,   0,   0)),
                    rad(key(  0,   0,   0))
                );
                default -> store;
            };
        }
        if (type == TransformType.POSITION) {
            if (bone.equals("rightArm")) return new Vec3f(0f, 0f, key(0f, 0.8f, -2f));
            if (bone.equals("leftArm"))  return new Vec3f(0f, 0f, key(0f, 0.7f, -1.8f));
        }
        return store;
    }

    // ── Util ─────────────────────────────────────────────────────
    private int phaseDuration() {
        return switch (phase) {
            case PHASE_WINDUP -> WINDUP_TICKS;
            case PHASE_STRIKE -> STRIKE_TICKS;
            default           -> RECOVERY_TICKS;
        };
    }

    private float smooth(float t)  { return t * t * (3f - 2f * t); }
    private float easeOut(float t) { float u = 1f-t; return 1f - u*u*u; }
    private float lerp(float a, float b, float t) { return a + (b-a)*t; }

    private float key(float rest, float windupPeak, float strikePeak) {
        return switch (phase) {
            case PHASE_WINDUP  -> lerp(rest,       windupPeak, smooth(phaseT));
            case PHASE_STRIKE  -> lerp(windupPeak, strikePeak, easeOut(phaseT));
            default            -> lerp(strikePeak, rest,       smooth(phaseT));
        };
    }

    private float rad(float deg) { return (float) Math.toRadians(deg); }
}