package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

/**
 * Smooth 3-phase saber combo animation with lerp + smoothstep.
 *
 * Pose data is stored as float[] in SaberAttackData — Vec3f is only
 * constructed at the final return to avoid the package-private x/y/z issue.
 */
public class SaberComboAnimation implements IAnimation {

    public static final SaberComboAnimation INSTANCE = new SaberComboAnimation();

    public enum Phase { IDLE, WINDUP, STRIKE, RECOVERY }

    public static final int WINDUP_TICKS   = 10;
    public static final int STRIKE_TICKS   = 8;
    public static final int RECOVERY_TICKS = 14;

    private Phase phase      = Phase.IDLE;
    private int   comboIndex = 0;
    private int   ticksLeft  = 0;
    private int   totalTicks = 0;

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

    @Override public boolean isActive()          { return phase != Phase.IDLE; }
    @Override public void    setupAnim(float td) {}

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (phase == Phase.IDLE) return store;
        if (type != TransformType.ROTATION) return store;

        SaberAttackData a = SaberAttackData.ATTACKS[comboIndex];

        float progress = 1.0f - ((ticksLeft - tickDelta) / (float) totalTicks);
        progress = Math.max(0f, Math.min(1f, progress));
        float t = smoothstep(progress);

        float[] from, to;

        switch (phase) {
            case WINDUP:
                from = ZERO;
                to   = getPose(a, modelName, Phase.WINDUP);
                break;
            case STRIKE:
                from = getPose(a, modelName, Phase.WINDUP);
                to   = getPose(a, modelName, Phase.STRIKE);
                break;
            case RECOVERY:
                from = getPose(a, modelName, Phase.STRIKE);
                to   = getPose(a, modelName, Phase.RECOVERY);
                break;
            default:
                return store;
        }

        if (from == null || to == null) return store;

        // Construct Vec3f only here — never access .x/.y/.z fields
        return new Vec3f(
            from[0] + (to[0] - from[0]) * t,
            from[1] + (to[1] - from[1]) * t,
            from[2] + (to[2] - from[2]) * t
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static final float[] ZERO = {0f, 0f, 0f};

    private static float smoothstep(float t) {
        return t * t * (3f - 2f * t);
    }

    private static float[] getPose(SaberAttackData a, String bone, Phase phase) {
        switch (bone) {
            case "rightArm":
                return phase == Phase.WINDUP   ? a.windupRightArm
                     : phase == Phase.STRIKE   ? a.strikeRightArm
                     :                           a.recoveryRightArm;
            case "leftArm":
                return phase == Phase.WINDUP   ? a.windupLeftArm
                     : phase == Phase.STRIKE   ? a.strikeLeftArm
                     :                           a.recoveryLeftArm;
            case "body":
                return phase == Phase.WINDUP   ? a.windupBody
                     : phase == Phase.STRIKE   ? a.strikeBody
                     :                           a.recoveryBody;
            default:
                return null;
        }
    }
}