package com.uheroes.mod.client.animation;

/**
 * Stores pose data as float[3] {x, y, z} arrays — avoids Vec3f field access
 * which is package-private in PlayerAnimator's Vector3 base class.
 * Vec3f is only constructed at the final return point in SaberComboAnimation.
 */
public class SaberAttackData {

    public final String  name;
    public final float[] windupRightArm,   strikeRightArm,   recoveryRightArm;
    public final float[] windupLeftArm,    strikeLeftArm,    recoveryLeftArm;
    public final float[] windupBody,       strikeBody,       recoveryBody;
    public final int     fluxCost;
    public final float   damageMultiplier;

    private SaberAttackData(String name,
                             float[] wRA, float[] sRA, float[] rRA,
                             float[] wLA, float[] sLA, float[] rLA,
                             float[] wB,  float[] sB,  float[] rB,
                             int fluxCost, float damageMult) {
        this.name             = name;
        this.windupRightArm   = wRA; this.strikeRightArm   = sRA; this.recoveryRightArm   = rRA;
        this.windupLeftArm    = wLA; this.strikeLeftArm    = sLA; this.recoveryLeftArm    = rLA;
        this.windupBody       = wB;  this.strikeBody       = sB;  this.recoveryBody       = rB;
        this.fluxCost         = fluxCost;
        this.damageMultiplier = damageMult;
    }

    private static final float[] N = {0f, 0f, 0f};

    public static final SaberAttackData[] ATTACKS = {

        // 1 — Horizontal Slash
        new SaberAttackData("Horizontal Slash",
            new float[]{-0.2f,-0.5f, 0.1f}, new float[]{-0.4f, 0.7f,-0.1f}, new float[]{-0.1f, 0.1f, 0f},
            new float[]{-0.1f, 0.2f, 0.0f}, new float[]{-0.3f,-0.3f, 0.1f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{ 0.0f,-0.25f,0.0f}, new float[]{ 0.05f,0.3f, 0.0f}, N,
            0, 1.0f),

        // 2 — Vertical Downslash
        new SaberAttackData("Vertical Downslash",
            new float[]{-1.3f, 0.0f, 0.1f}, new float[]{ 0.6f, 0.0f,-0.1f}, new float[]{-0.2f, 0.0f, 0f},
            new float[]{-0.3f, 0.0f,-0.1f}, new float[]{-0.1f, 0.0f, 0.1f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{-0.1f, 0.0f, 0.0f}, new float[]{ 0.15f,0.0f, 0.0f}, N,
            0, 1.0f),

        // 3 — Upward Cut
        new SaberAttackData("Upward Cut",
            new float[]{ 0.4f, 0.1f,-0.3f}, new float[]{-1.1f,-0.1f, 0.4f}, new float[]{-0.2f, 0.0f, 0.1f},
            new float[]{ 0.1f, 0.0f, 0.1f}, new float[]{-0.4f, 0.0f,-0.1f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{ 0.08f,0.1f, 0.0f}, new float[]{-0.08f,-0.1f,0.0f}, N,
            0, 1.0f),

        // 4 — Thrust
        new SaberAttackData("Thrust",
            new float[]{-0.3f, 0.4f, 0.0f}, new float[]{-0.7f,-0.4f, 0.0f}, new float[]{-0.3f, 0.0f, 0f},
            new float[]{-0.2f,-0.2f, 0.0f}, new float[]{-0.1f, 0.1f, 0.0f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{ 0.0f,-0.15f,0.0f}, new float[]{ 0.12f,0.15f,0.0f}, N,
            0, 1.0f),

        // 5 — Diagonal Cut
        new SaberAttackData("Diagonal Cut",
            new float[]{-1.0f,-0.4f,-0.2f}, new float[]{ 0.3f, 0.5f, 0.3f}, new float[]{-0.1f, 0.1f, 0.1f},
            new float[]{-0.2f, 0.2f,-0.1f}, new float[]{-0.3f,-0.2f, 0.1f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{ 0.0f,-0.3f, 0.0f}, new float[]{ 0.05f,0.35f,0.0f}, N,
            0, 1.0f),

        // 6 — Spin Slash
        new SaberAttackData("Spin Slash",
            new float[]{-0.2f,-0.4f,-0.1f}, new float[]{-0.6f, 0.8f, 0.2f}, new float[]{-0.2f, 0.0f, 0f},
            new float[]{-0.1f, 0.3f,-0.1f}, new float[]{-0.4f,-0.5f, 0.2f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{ 0.0f, 0.5f, 0.0f}, new float[]{ 0.05f,-0.6f,0.0f}, N,
            0, 1.0f),

        // 7 — Cross Slash
        new SaberAttackData("Cross Slash",
            new float[]{-0.4f, 0.4f, 0.4f}, new float[]{-0.7f,-0.5f,-0.2f}, new float[]{-0.2f, 0.0f, 0f},
            new float[]{-0.3f,-0.2f, 0.1f}, new float[]{-0.2f, 0.3f,-0.1f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{ 0.0f, 0.25f,0.0f}, new float[]{ 0.05f,-0.25f,0.0f}, N,
            0, 1.0f),

        // 8 — Reverse Slash (1 flux, 1.4x)
        new SaberAttackData("Reverse Slash",
            new float[]{-0.2f,-0.3f,-0.7f}, new float[]{-0.8f, 0.5f, 0.5f}, new float[]{-0.2f, 0.1f, 0.1f},
            new float[]{-0.1f, 0.3f, 0.2f}, new float[]{-0.5f,-0.3f,-0.1f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{ 0.0f,-0.2f, 0.0f}, new float[]{ 0.08f,0.25f,0.0f}, N,
            1, 1.4f),

        // 9 — Overhead Smash (2 flux, 1.8x)
        new SaberAttackData("Overhead Smash",
            new float[]{-1.4f, 0.0f, 0.15f}, new float[]{ 0.8f, 0.0f,-0.1f}, new float[]{-0.15f,0.0f, 0f},
            new float[]{-1.2f, 0.0f,-0.15f}, new float[]{ 0.5f, 0.0f, 0.1f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{-0.12f,0.0f, 0.0f},  new float[]{ 0.2f, 0.0f, 0.0f}, N,
            2, 1.8f),

        // 10 — Burst Strike (3 flux, 2.5x)
        new SaberAttackData("Burst Strike",
            new float[]{-0.7f,-0.2f, 0.1f}, new float[]{-1.2f, 0.0f, 0.7f}, new float[]{-0.2f, 0.0f, 0.1f},
            new float[]{-0.3f, 0.3f, 0.1f}, new float[]{-0.6f,-0.2f,-0.1f}, new float[]{-0.1f, 0.0f, 0f},
            new float[]{ 0.08f,-0.25f,0.0f},new float[]{ 0.12f,0.1f, 0.0f}, N,
            3, 2.5f),
    };
}