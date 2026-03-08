package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.core.util.Vec3f;

/**
 * Defines the 10 saber combo attacks.
 * Each attack has:
 *   - windupArm / strikeArm : rightArm rotation in radians (x=raise, y=swing, z=roll)
 *   - windupBody / strikeBody: body rotation in radians
 *   - fluxCost: Neural Flux consumed on hit (0 = free)
 *   - damageMultiplier: outgoing damage multiplier
 *   - name: display / debug name
 *
 * Attacks 1-7 are free. Attacks 8-10 cost flux and deal bonus damage.
 */
public class SaberAttackData {

    public final String name;
    public final Vec3f windupArm;
    public final Vec3f strikeArm;
    public final Vec3f windupBody;
    public final Vec3f strikeBody;
    public final int fluxCost;
    public final float damageMultiplier;

    private SaberAttackData(String name,
                             Vec3f windupArm, Vec3f strikeArm,
                             Vec3f windupBody, Vec3f strikeBody,
                             int fluxCost, float damageMultiplier) {
        this.name            = name;
        this.windupArm       = windupArm;
        this.strikeArm       = strikeArm;
        this.windupBody      = windupBody;
        this.strikeBody      = strikeBody;
        this.fluxCost        = fluxCost;
        this.damageMultiplier = damageMultiplier;
    }

    // ── 10 attacks ────────────────────────────────────────────────────────────

    public static final SaberAttackData[] ATTACKS = {

        // 1 — Horizontal Slash: arm cocked right → sweeps left
        new SaberAttackData("Horizontal Slash",
            new Vec3f(-0.3f, -0.6f,  0.2f), new Vec3f(-0.5f,  0.8f,  0.0f),
            new Vec3f( 0.0f, -0.2f,  0.0f), new Vec3f( 0.0f,  0.2f,  0.0f),
            0, 1.0f),

        // 2 — Vertical Downslash: arm raised high → chopped down
        new SaberAttackData("Vertical Downslash",
            new Vec3f(-1.4f,  0.0f,  0.0f), new Vec3f( 0.5f,  0.0f,  0.0f),
            new Vec3f(-0.05f, 0.0f,  0.0f), new Vec3f( 0.1f,  0.0f,  0.0f),
            0, 1.0f),

        // 3 — Upward Cut: arm low + blade angled back → rising slash
        new SaberAttackData("Upward Cut",
            new Vec3f( 0.3f,  0.2f, -0.4f), new Vec3f(-1.2f, -0.1f,  0.5f),
            new Vec3f( 0.05f, 0.1f,  0.0f), new Vec3f(-0.05f,-0.1f,  0.0f),
            0, 1.0f),

        // 4 — Thrust: arm pulled back → stabbed forward
        new SaberAttackData("Thrust",
            new Vec3f(-0.4f,  0.3f,  0.0f), new Vec3f(-0.6f, -0.3f,  0.0f),
            new Vec3f( 0.0f, -0.1f,  0.0f), new Vec3f( 0.1f,  0.1f,  0.0f),
            0, 1.0f),

        // 5 — Diagonal Cut: high-right wind-up → sweeps to low-left
        new SaberAttackData("Diagonal Cut",
            new Vec3f(-1.1f, -0.4f, -0.3f), new Vec3f( 0.2f,  0.5f,  0.4f),
            new Vec3f( 0.0f, -0.3f,  0.0f), new Vec3f( 0.0f,  0.3f,  0.0f),
            0, 1.0f),

        // 6 — Spin Slash: wide body twist → explosive sweep
        new SaberAttackData("Spin Slash",
            new Vec3f(-0.3f, -0.5f, -0.2f), new Vec3f(-0.7f,  0.9f,  0.3f),
            new Vec3f( 0.0f,  0.4f,  0.0f), new Vec3f( 0.0f, -0.5f,  0.0f),
            0, 1.0f),

        // 7 — Cross Slash: arm swings one way then snaps across
        new SaberAttackData("Cross Slash",
            new Vec3f(-0.5f,  0.5f,  0.5f), new Vec3f(-0.8f, -0.5f, -0.3f),
            new Vec3f( 0.0f,  0.2f,  0.0f), new Vec3f( 0.0f, -0.2f,  0.0f),
            0, 1.0f),

        // 8 — Reverse Slash: arm twisted back → backhand strike (1 flux, 1.4x dmg)
        new SaberAttackData("Reverse Slash",
            new Vec3f(-0.3f, -0.3f, -0.8f), new Vec3f(-0.9f,  0.6f,  0.6f),
            new Vec3f( 0.0f, -0.2f,  0.0f), new Vec3f( 0.05f, 0.2f,  0.0f),
            1, 1.4f),

        // 9 — Overhead Smash: arms raised very high → slam down (2 flux, 1.8x dmg)
        new SaberAttackData("Overhead Smash",
            new Vec3f(-1.5f,  0.0f,  0.2f), new Vec3f( 0.7f,  0.0f, -0.1f),
            new Vec3f(-0.1f,  0.0f,  0.0f), new Vec3f( 0.15f, 0.0f,  0.0f),
            2, 1.8f),

        // 10 — Burst Strike: coiled → full lunge extension (3 flux, 2.5x dmg)
        new SaberAttackData("Burst Strike",
            new Vec3f(-0.8f, -0.3f,  0.1f), new Vec3f(-1.3f,  0.0f,  0.8f),
            new Vec3f( 0.05f,-0.2f,  0.0f), new Vec3f( 0.1f,  0.0f,  0.0f),
            3, 2.5f),
    };
}