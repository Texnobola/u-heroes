package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

public class SaberBlockAnimation implements IAnimation {

    public static final SaberBlockAnimation INSTANCE = new SaberBlockAnimation();
    private boolean active = false;
    private SaberBlockAnimation() {}
    public void setActive(boolean active) { this.active = active; }

    @Override public boolean isActive() { return active; }
    @Override public void setupAnim(float tickDelta) {}

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (type == TransformType.ROTATION) {
            switch (modelName) {
                case "rightArm":
                    return new Vec3f((float)Math.toRadians(-60), 0.0f, (float)Math.toRadians(70));
                case "leftArm":
                    return new Vec3f((float)Math.toRadians(-101.80868), (float)Math.toRadians(31.94715), (float)Math.toRadians(-6.31274));
            }
        } else if (type == TransformType.POSITION && modelName.equals("rightArm")) {
            return new Vec3f(0.0f, -2.0f, 0.0f);
        }
        
        return store;
    }
}