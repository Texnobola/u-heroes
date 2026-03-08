package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.core.util.Vec3f;

/**
 * Defines the 10 saber combo attacks.
 * Each attack has THREE poses: windup → strike → recovery (back toward neutral).
 * The animation lerps smoothly between them.
 *
 * Attacks 1-7 are free. Attacks 8-10 cost flux and deal bonus damage.
 */
public class SaberAttackData {

    public final String name;

    // Three pose keyframes — all bones: rightArm, leftArm, body
    public final Vec3f windupRightArm;
    public final Vec3f strikeRightArm;
    public final Vec3f recoveryRightArm;

    public final Vec3f windupLeftArm;
    public final Vec3f strikeLeftArm;
    public final Vec3f recoveryLeftArm;

    public final Vec3f windupBody;
    public final Vec3f strikeBody;
    public final Vec3f recoveryBody;

    public final int   fluxCost;
    public final float damageMultiplier;

    private SaberAttackData(String name,
                             Vec3f wRA, Vec3f sRA, Vec3f rRA,
                             Vec3f wLA, Vec3f sLA, Vec3f rLA,
                             Vec3f wB,  Vec3f sB,  Vec3f rB,
                             int fluxCost, float damageMult) {
        this.name              = name;
        this.windupRightArm    = wRA;
        this.strikeRightArm    = sRA;
        this.recoveryRightArm  = rRA;
        this.windupLeftArm     = wLA;
        this.strikeLeftArm     = sLA;
        this.recoveryLeftArm   = rLA;
        this.windupBody        = wB;
        this.strikeBody        = sB;
        this.recoveryBody      = rB;
        this.fluxCost          = fluxCost;
        this.damageMultiplier  = damageMult;
    }

    // neutral / rest pose
    private static final Vec3f N = new Vec3f(0f, 0f, 0f);

    public static final SaberAttackData[] ATTACKS = {

        // 1 — Horizontal Slash: wind-up cocks right, sweeps hard left, arm drifts back
        new SaberAttackData("Horizontal Slash",
            new Vec3f(-0.2f, -0.5f,  0.1f), new Vec3f(-0.4f,  0.7f, -0.1f), new Vec3f(-0.1f,  0.1f, 0f),
            new Vec3f(-0.1f,  0.2f,  0.0f), new Vec3f(-0.3f, -0.3f,  0.1f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f( 0.0f, -0.25f, 0.0f), new Vec3f( 0.05f, 0.3f,  0.0f), new Vec3f( 0.0f,  0.0f, 0f),
            0, 1.0f),

        // 2 — Vertical Downslash: raise high, chop down, settle
        new SaberAttackData("Vertical Downslash",
            new Vec3f(-1.3f,  0.0f,  0.1f), new Vec3f( 0.6f,  0.0f, -0.1f), new Vec3f(-0.2f,  0.0f, 0f),
            new Vec3f(-0.3f,  0.0f, -0.1f), new Vec3f(-0.1f,  0.0f,  0.1f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f(-0.1f,  0.0f,  0.0f), new Vec3f( 0.15f, 0.0f,  0.0f), N,
            0, 1.0f),

        // 3 — Upward Cut: dip low, rising slash, follow-through
        new SaberAttackData("Upward Cut",
            new Vec3f( 0.4f,  0.1f, -0.3f), new Vec3f(-1.1f, -0.1f,  0.4f), new Vec3f(-0.2f,  0.0f, 0.1f),
            new Vec3f( 0.1f,  0.0f,  0.1f), new Vec3f(-0.4f,  0.0f, -0.1f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f( 0.08f, 0.1f,  0.0f), new Vec3f(-0.08f,-0.1f,  0.0f), N,
            0, 1.0f),

        // 4 — Thrust: pull back, lunge forward, recoil
        new SaberAttackData("Thrust",
            new Vec3f(-0.3f,  0.4f,  0.0f), new Vec3f(-0.7f, -0.4f,  0.0f), new Vec3f(-0.3f,  0.0f, 0f),
            new Vec3f(-0.2f, -0.2f,  0.0f), new Vec3f(-0.1f,  0.1f,  0.0f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f( 0.0f, -0.15f, 0.0f), new Vec3f( 0.12f, 0.15f, 0.0f), N,
            0, 1.0f),

        // 5 — Diagonal Cut: high right, sweep to low left, drift
        new SaberAttackData("Diagonal Cut",
            new Vec3f(-1.0f, -0.4f, -0.2f), new Vec3f( 0.3f,  0.5f,  0.3f), new Vec3f(-0.1f,  0.1f, 0.1f),
            new Vec3f(-0.2f,  0.2f, -0.1f), new Vec3f(-0.3f, -0.2f,  0.1f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f( 0.0f, -0.3f,  0.0f), new Vec3f( 0.05f, 0.35f, 0.0f), N,
            0, 1.0f),

        // 6 — Spin Slash: body coils, explosive full sweep, unwind
        new SaberAttackData("Spin Slash",
            new Vec3f(-0.2f, -0.4f, -0.1f), new Vec3f(-0.6f,  0.8f,  0.2f), new Vec3f(-0.2f,  0.0f, 0f),
            new Vec3f(-0.1f,  0.3f, -0.1f), new Vec3f(-0.4f, -0.5f,  0.2f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f( 0.0f,  0.5f,  0.0f), new Vec3f( 0.05f,-0.6f,  0.0f), N,
            0, 1.0f),

        // 7 — Cross Slash: angled wind-up, snap across body, settle
        new SaberAttackData("Cross Slash",
            new Vec3f(-0.4f,  0.4f,  0.4f), new Vec3f(-0.7f, -0.5f, -0.2f), new Vec3f(-0.2f,  0.0f, 0f),
            new Vec3f(-0.3f, -0.2f,  0.1f), new Vec3f(-0.2f,  0.3f, -0.1f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f( 0.0f,  0.25f, 0.0f), new Vec3f( 0.05f,-0.25f, 0.0f), N,
            0, 1.0f),

        // 8 — Reverse Slash: twisted back-hand wind-up, powerful backhand strike (1 flux, 1.4x)
        new SaberAttackData("Reverse Slash",
            new Vec3f(-0.2f, -0.3f, -0.7f), new Vec3f(-0.8f,  0.5f,  0.5f), new Vec3f(-0.2f,  0.1f, 0.1f),
            new Vec3f(-0.1f,  0.3f,  0.2f), new Vec3f(-0.5f, -0.3f, -0.1f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f( 0.0f, -0.2f,  0.0f), new Vec3f( 0.08f, 0.25f, 0.0f), N,
            1, 1.4f),

        // 9 — Overhead Smash: both arms raised, whole body slams down (2 flux, 1.8x)
        new SaberAttackData("Overhead Smash",
            new Vec3f(-1.4f,  0.0f,  0.15f), new Vec3f( 0.8f,  0.0f, -0.1f), new Vec3f(-0.15f, 0.0f, 0f),
            new Vec3f(-1.2f,  0.0f, -0.15f), new Vec3f( 0.5f,  0.0f,  0.1f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f(-0.12f, 0.0f,  0.0f),  new Vec3f( 0.2f,  0.0f,  0.0f), N,
            2, 1.8f),

        // 10 — Burst Strike: coiled full-body lunge, explosive extension (3 flux, 2.5x)
        new SaberAttackData("Burst Strike",
            new Vec3f(-0.7f, -0.2f,  0.1f), new Vec3f(-1.2f,  0.0f,  0.7f), new Vec3f(-0.2f,  0.0f, 0.1f),
            new Vec3f(-0.3f,  0.3f,  0.1f), new Vec3f(-0.6f, -0.2f, -0.1f), new Vec3f(-0.1f,  0.0f, 0f),
            new Vec3f( 0.08f,-0.25f, 0.0f), new Vec3f( 0.12f, 0.1f,  0.0f), N,
            3, 2.5f),
    };
}