package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

public class SaberAttackAnimation implements IAnimation {

    public static final SaberAttackAnimation INSTANCE = new SaberAttackAnimation();
    private SaberAttackAnimation() {}

    private static final int PHASE_WINDUP   = 0;
    private static final int PHASE_STRIKE   = 1;
    private static final int PHASE_RECOVERY = 2;
    private static final int WINDUP_TICKS   = 8;
    private static final int STRIKE_TICKS   = 3;
    private static final int RECOVERY_TICKS = 14;

    private boolean active     = false;
    private int     attackType = 0;
    private int     phase      = PHASE_WINDUP;
    private int     phaseTick  = 0;
    private float   phaseT     = 0f;

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

    // Expose state for first-person handler
    public int   getPhase()      { return phase; }
    public float getPhaseT()     { return phaseT; }
    public int   getAttackType() { return attackType; }

    // Expose computed key values for first-person handler to read
    public float getFPYaw()   {
        return switch (attackType) {
            case 0 -> key(0, -45,  50);
            case 1 -> key(0, -20,  15);
            default -> 0;
        };
    }
    public float getFPPitch() {
        return switch (attackType) {
            case 0 -> key(0,  -15,  10);
            case 1 -> key(-5, -100, 45);
            case 2 -> key(-5,  20, -75);
            default -> 0;
        };
    }
    public float getFPRoll() {
        return switch (attackType) {
            case 0 -> key(0, -20, 15);
            case 1 -> key(0, -15, 20);
            default -> 0;
        };
    }
    public float getFPTranslateZ() {
        return attackType == 2 ? key(0f, 0.08f, -0.22f) : 0f;
    }

    @Override public boolean isActive() { return active; }

    @Override
    public void setupAnim(float partialTick) {
        if (!active) return;
        phaseT = Math.min((phaseTick + partialTick) / phaseDuration(), 1f);
    }

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

    private Vec3f horizontalSlash(String bone, TransformType type, Vec3f store) {
        if (type == TransformType.ROTATION) {
            return switch (bone) {
                case "rightArm" -> new Vec3f(rad(key(-10,-25, 15)), rad(key(0,-48, 52)), rad(key(0,-20, 15)));
                case "leftArm"  -> new Vec3f(rad(key(  0,-20,-10)), rad(key(0, 18,-18)), rad(key(0, 12, -5)));
                case "body"     -> new Vec3f(rad(key(  0,  4,  4)), rad(key(0, 22,-20)), rad(0));
                default -> store;
            };
        }
        return store;
    }

    private Vec3f overheadSlam(String bone, TransformType type, Vec3f store) {
        if (type == TransformType.ROTATION) {
            return switch (bone) {
                case "rightArm" -> new Vec3f(rad(key(-10,-115, 55)), rad(key(0,-25, 18)), rad(key(0,-18, 25)));
                case "leftArm"  -> new Vec3f(rad(key(  0, -40,-12)), rad(key(0,-12, 10)), rad(key(0, 18,  5)));
                case "body"     -> new Vec3f(rad(key(  0, -12, 16)), rad(key(0, 18,-10)), rad(0));
                default -> store;
            };
        }
        if (type == TransformType.POSITION && bone.equals("rightArm"))
            return new Vec3f(0f, key(0f, -2f, 1.2f), 0f);
        return store;
    }

    private Vec3f thrustStab(String bone, TransformType type, Vec3f store) {
        if (type == TransformType.ROTATION) {
            return switch (bone) {
                case "rightArm" -> new Vec3f(rad(key(-10, 22,-85)), rad(key(0, 12, -8)), rad(key(0,-12, -8)));
                case "leftArm"  -> new Vec3f(rad(key(  0, 18,-70)), rad(key(0,-12,  8)), rad(key(0, 12,  8)));
                case "body"     -> new Vec3f(rad(key(  0, 12,-16)), rad(0), rad(0));
                default -> store;
            };
        }
        if (type == TransformType.POSITION) {
            if (bone.equals("rightArm")) return new Vec3f(0f, 0f, key(0f, 0.8f, -2f));
            if (bone.equals("leftArm"))  return new Vec3f(0f, 0f, key(0f, 0.7f,-1.8f));
        }
        return store;
    }

    private int phaseDuration() {
        return switch (phase) {
            case PHASE_WINDUP -> WINDUP_TICKS;
            case PHASE_STRIKE -> STRIKE_TICKS;
            default           -> RECOVERY_TICKS;
        };
    }

    private float smooth(float t)  { return t*t*(3f-2f*t); }
    private float easeOut(float t) { float u=1f-t; return 1f-u*u*u; }
    private float lerp(float a, float b, float t) { return a+(b-a)*t; }
    private float key(float rest, float windupPeak, float strikePeak) {
        return switch (phase) {
            case PHASE_WINDUP  -> lerp(rest,       windupPeak, smooth(phaseT));
            case PHASE_STRIKE  -> lerp(windupPeak, strikePeak, easeOut(phaseT));
            default            -> lerp(strikePeak, rest,       smooth(phaseT));
        };
    }
    private float rad(float deg) { return (float) Math.toRadians(deg); }
}