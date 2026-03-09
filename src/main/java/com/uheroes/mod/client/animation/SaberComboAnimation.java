package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

public class SaberComboAnimation implements IAnimation {
    
    public static final SaberComboAnimation INSTANCE = new SaberComboAnimation();
    
    private static final int WINDUP_TICKS = 10;
    private static final int STRIKE_TICKS = 8;
    private static final int RECOVERY_TICKS = 14;
    private static final int TOTAL_TICKS = WINDUP_TICKS + STRIKE_TICKS + RECOVERY_TICKS;
    
    private boolean active = false;
    private int currentAttack = 0;
    private int animTick = 0;
    
    private SaberComboAnimation() {}
    
    public void startAttack(int attackIndex) {
        this.currentAttack = attackIndex;
        this.animTick = 0;
        this.active = true;
    }
    
    public void tick() {
        if (active) {
            animTick++;
            if (animTick >= TOTAL_TICKS) {
                active = false;
                animTick = 0;
            }
        }
    }
    
    public boolean isPlaying() {
        return active;
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void setupAnim(float tickDelta) {
    }
    
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (!active || type != TransformType.ROTATION || !modelName.equals("rightArm")) {
            return store;
        }
        
        if (currentAttack < 0 || currentAttack >= SaberAttackData.ATTACKS.length) {
            return store;
        }
        
        SaberAttackData.Attack attack = SaberAttackData.ATTACKS[currentAttack];
        float progress = (animTick + tickDelta) / TOTAL_TICKS;
        float[] target;
        
        if (animTick < WINDUP_TICKS) {
            float t = smoothstep((animTick + tickDelta) / WINDUP_TICKS);
            target = lerp(new float[]{0, 0, 0}, attack.windupRightArm, t);
        } else if (animTick < WINDUP_TICKS + STRIKE_TICKS) {
            float t = smoothstep((animTick - WINDUP_TICKS + tickDelta) / STRIKE_TICKS);
            target = lerp(attack.windupRightArm, attack.strikeRightArm, t);
        } else {
            float t = smoothstep((animTick - WINDUP_TICKS - STRIKE_TICKS + tickDelta) / RECOVERY_TICKS);
            target = lerp(attack.strikeRightArm, attack.recoveryRightArm, t);
        }
        
        return new Vec3f(target[0], target[1], target[2]);
    }
    
    private float[] lerp(float[] a, float[] b, float t) {
        return new float[]{
            a[0] + (b[0] - a[0]) * t,
            a[1] + (b[1] - a[1]) * t,
            a[2] + (b[2] - a[2]) * t
        };
    }
    
    private float smoothstep(float t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }
}
