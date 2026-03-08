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

    /**
     * v3: arm was swinging too far left (y=0.35 too high).
     * Reduce y to bring arm in front of body.
     * Increase x (more negative) to raise arm higher — high guard.
     * Reduce z slightly so blade isn't rolling fully to the side.
     */
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (type != TransformType.ROTATION) return store;

        switch (modelName) {
            case "rightArm": return new Vec3f(-1.1f,  0.1f,  0.5f);
            case "leftArm":  return new Vec3f(-0.4f,  0.0f,  0.3f);
            case "body":     return new Vec3f( 0.08f, 0.1f,  0.0f);
            default:         return store;
        }
    }
}