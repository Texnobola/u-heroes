package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

/**
 * Drives the 10-attack saber combo animation.
 *
 * Each attack has two phases:
 *   WINDUP  — brief wind-up pose (4 ticks)
 *   STRIKE  — snap to strike pose (8 ticks)
 *   IDLE    — no pose applied
 *
 * SaberComboHandler ticks this every client tick and advances phases.
 */
public class SaberComboAnimation implements IAnimation {

    public static final SaberComboAnimation INSTANCE = new SaberComboAnimation();

    public enum Phase { IDLE, WINDUP, STRIKE }

    public static final int WINDUP_TICKS = 4;
    public static final int STRIKE_TICKS = 8;

    private Phase phase       = Phase.IDLE;
    private int   comboIndex  = 0;   // which of the 10 attacks (0-9)
    private int   ticksLeft   = 0;

    private SaberComboAnimation() {}

    // Called by SaberComboHandler when player left-clicks
    public void startAttack(int index) {
        this.comboIndex = index % SaberAttackData.ATTACKS.length;
        this.phase      = Phase.WINDUP;
        this.ticksLeft  = WINDUP_TICKS;
    }

    // Called every client tick by SaberComboHandler
    public void tick() {
        if (phase == Phase.IDLE) return;
        ticksLeft--;
        if (ticksLeft <= 0) {
            if (phase == Phase.WINDUP) {
                phase     = Phase.STRIKE;
                ticksLeft = STRIKE_TICKS;
            } else {
                phase     = Phase.IDLE;
                ticksLeft = 0;
            }
        }
    }

    public Phase getPhase()      { return phase; }
    public int   getComboIndex() { return comboIndex; }

    @Override
    public boolean isActive() { return phase != Phase.IDLE; }

    @Override
    public void setupAnim(float tickDelta) {}

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (phase == Phase.IDLE) return store;
        if (type != TransformType.ROTATION) return store;

        SaberAttackData attack = SaberAttackData.ATTACKS[comboIndex];
        boolean isWindup = (phase == Phase.WINDUP);

        switch (modelName) {
            case "rightArm":
                return isWindup ? attack.windupArm  : attack.strikeArm;
            case "body":
                return isWindup ? attack.windupBody : attack.strikeBody;
            default:
                return store;
        }
    }
}