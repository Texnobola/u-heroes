package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

public class CombatStanceAnimation implements IAnimation {
    
    public static final CombatStanceAnimation INSTANCE = new CombatStanceAnimation();
    
    private boolean active = false;
    
    private CombatStanceAnimation() {}
    
    public void setActive(boolean active) {
        this.active = active;
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
        if (type != TransformType.ROTATION) {
            return store;
        }
        
        switch (modelName) {
            case "rightArm":
                return new Vec3f(-0.4f, 0.1f, 0.2f);
            case "leftArm":
                return new Vec3f(-0.2f, 0.0f, 0.1f);
            case "body":
                return new Vec3f(0.05f, 0.05f, 0.0f);
            default:
                return store;
        }
    }
}
