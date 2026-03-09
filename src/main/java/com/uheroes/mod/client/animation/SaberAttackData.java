package com.uheroes.mod.client.animation;

public class SaberAttackData {
    
    public static final Attack[] ATTACKS = new Attack[10];
    
    static {
        ATTACKS[0] = new Attack(0, 1.0f, new float[]{-0.8f, 0.0f, 0.0f}, new float[]{-1.5f, 0.0f, -0.3f}, new float[]{-0.3f, 0.0f, 0.0f});
        ATTACKS[1] = new Attack(0, 1.0f, new float[]{-0.8f, 0.0f, 0.0f}, new float[]{-1.5f, 0.0f, 0.3f}, new float[]{-0.3f, 0.0f, 0.0f});
        ATTACKS[2] = new Attack(0, 1.0f, new float[]{-1.0f, 0.0f, -0.5f}, new float[]{-1.6f, 0.0f, 0.5f}, new float[]{-0.4f, 0.0f, 0.0f});
        ATTACKS[3] = new Attack(0, 1.0f, new float[]{-1.0f, 0.0f, 0.5f}, new float[]{-1.6f, 0.0f, -0.5f}, new float[]{-0.4f, 0.0f, 0.0f});
        ATTACKS[4] = new Attack(0, 1.0f, new float[]{-0.9f, 0.0f, -0.4f}, new float[]{-1.7f, 0.0f, 0.4f}, new float[]{-0.5f, 0.0f, 0.0f});
        ATTACKS[5] = new Attack(0, 1.0f, new float[]{-0.9f, 0.0f, 0.4f}, new float[]{-1.7f, 0.0f, -0.4f}, new float[]{-0.5f, 0.0f, 0.0f});
        ATTACKS[6] = new Attack(0, 1.0f, new float[]{-1.1f, 0.0f, -0.6f}, new float[]{-1.8f, 0.0f, 0.6f}, new float[]{-0.6f, 0.0f, 0.0f});
        ATTACKS[7] = new Attack(1, 1.4f, new float[]{-0.5f, 0.0f, 0.8f}, new float[]{-1.9f, 0.0f, -0.8f}, new float[]{-0.7f, 0.0f, 0.0f});
        ATTACKS[8] = new Attack(2, 1.8f, new float[]{0.5f, 0.0f, 0.0f}, new float[]{-2.2f, 0.0f, 0.0f}, new float[]{-0.8f, 0.0f, 0.0f});
        ATTACKS[9] = new Attack(3, 2.5f, new float[]{-0.3f, 0.0f, -0.9f}, new float[]{-2.5f, 0.0f, 0.0f}, new float[]{-0.9f, 0.0f, 0.0f});
    }
    
    public static class Attack {
        public final int fluxCost;
        public final float damageMultiplier;
        public final float[] windupRightArm;
        public final float[] strikeRightArm;
        public final float[] recoveryRightArm;
        
        public Attack(int fluxCost, float damageMultiplier, float[] windup, float[] strike, float[] recovery) {
            this.fluxCost = fluxCost;
            this.damageMultiplier = damageMultiplier;
            this.windupRightArm = windup;
            this.strikeRightArm = strike;
            this.recoveryRightArm = recovery;
        }
    }
}
