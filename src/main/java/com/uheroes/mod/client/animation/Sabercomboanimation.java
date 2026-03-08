package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

/**
 * Drives the 10-attack saber combo animation.
 *
 * THREE phases per attack, all smoothly LERPED:
 *   WINDUP   — body coils / arm winds back        (10 ticks)
 *   STRIKE   — explosive snap to strike pose       (8  ticks)
 *   RECOVERY — drift back toward neutral           (14 ticks)
 *
 * Lerp means the pose is interpolated every frame — no snapping,
 * no jitter, fully fluid motion.
 */
public class SaberComboAnimation implements IAnimation {

    public static final SaberComboAnimation INSTANCE = new SaberComboAnimation();

    public enum Phase { IDLE, WINDUP, STRIKE, RECOVERY }

    // Tick durations — slow enough to feel weighty, fast enough to feel snappy
    public static final int WINDUP_TICKS   = 10;
    public static final int STRIKE_TICKS   = 8;
    public static final int RECOVERY_TICKS = 14;

    private Phase phase      = Phase.IDLE;
    private int   comboIndex = 0;
    private int   ticksLeft  = 0;
    private int   totalTicks = 0; // total ticks for current phase (for lerp)

    private SaberComboAnimation() {}

    public void startAttack(int index) {
        this.comboIndex = index % SaberAttackData.ATTACKS.length;
        enterPhase(Phase.WINDUP, WINDUP_TICKS);
    }

    private void enterPhase(Phase p, int ticks) {
        this.phase      = p;
        this.ticksLeft  = ticks;
        this.totalTicks = ticks;
    }

    /** Called every client tick by SaberComboHandler */
    public void tick() {
        if (phase == Phase.IDLE) return;
        ticksLeft--;
        if (ticksLeft <= 0) {
            switch (phase) {
                case WINDUP:   enterPhase(Phase.STRIKE,   STRIKE_TICKS);   break;
                case STRIKE:   enterPhase(Phase.RECOVERY, RECOVERY_TICKS); break;
                case RECOVERY: phase = Phase.IDLE; ticksLeft = 0;          break;
                default:       break;
            }
        }
    }

    public Phase getPhase()      { return phase; }
    public int   getComboIndex() { return comboIndex; }

    @Override public boolean isActive()              { return phase != Phase.IDLE; }
    @Override public void    setupAnim(float td)     {}

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (phase == Phase.IDLE) return store;
        if (type != TransformType.ROTATION) return store;

        SaberAttackData a = SaberAttackData.ATTACKS[comboIndex];

        // Progress 0.0 → 1.0 through current phase, accounting for sub-tick delta
        float progress = 1.0f - ((ticksLeft - tickDelta) / (float) totalTicks);
        progress = Math.max(0f, Math.min(1f, progress));

        // Smoothstep — makes motion ease in and out (removes the snappy JOJO feel)
        float t = smoothstep(progress);

        Vec3f from, to;

        switch (phase) {
            case WINDUP:
                from = neutral(modelName);
                to   = getWindup(a, modelName);
                break;
            case STRIKE:
                from = getWindup(a, modelName);
                to   = getStrike(a, modelName);
                break;
            case RECOVERY:
                from = getStrike(a, modelName);
                to   = getRecovery(a, modelName);
                break;
            default:
                return store;
        }

        if (from == null || to == null) return store;

        return lerp(from, to, t);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static float smoothstep(float t) {
        return t * t * (3f - 2f * t);
    }

    private static Vec3f lerp(Vec3f a, Vec3f b, float t) {
        return new Vec3f(
            a.x + (b.x - a.x) * t,
            a.y + (b.y - a.y) * t,
            a.z + (b.z - a.z) * t
        );
    }

    private static final Vec3f ZERO = new Vec3f(0f, 0f, 0f);

    private static Vec3f neutral(String bone) { return ZERO; }

    private static Vec3f getWindup(SaberAttackData a, String bone) {
        switch (bone) {
            case "rightArm": return a.windupRightArm;
            case "leftArm":  return a.windupLeftArm;
            case "body":     return a.windupBody;
            default:         return null;
        }
    }

    private static Vec3f getStrike(SaberAttackData a, String bone) {
        switch (bone) {
            case "rightArm": return a.strikeRightArm;
            case "leftArm":  return a.strikeLeftArm;
            case "body":     return a.strikeBody;
            default:         return null;
        }
    }

    private static Vec3f getRecovery(SaberAttackData a, String bone) {
        switch (bone) {
            case "rightArm": return a.recoveryRightArm;
            case "leftArm":  return a.recoveryLeftArm;
            case "body":     return a.recoveryBody;
            default:         return null;
        }
    }
}