package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

public class SaberBlockAnimation implements IAnimation {
    
    public static final SaberBlockAnimation INSTANCE = new SaberBlockAnimation();
    
    private boolean active = false;
    
    private SaberBlockAnimation() {}
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (type != TransformType.ROTATION) {
            return store;
        }
        
        switch (modelName) {
            case "rightArm":
                store.setX(-1.3f);
                store.setZ(-0.5f);
                break;
            case "leftArm":
                store.setX(-1.1f);
                store.setZ(0.6f);
                break;
            case "body":
                store.setX(0.05f);
                break;
        }
        
        return store;
    }
}
