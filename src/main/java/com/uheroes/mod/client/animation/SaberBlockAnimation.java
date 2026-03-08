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
    public void setupAnim(float tickDelta) {
        // static pose — no per-frame state needed
    }

    /**
     * Vec3f extends Vector3 where x/y/z are package-private — cannot be set directly.
     * setX/setY/setZ do not exist either.
     * Solution: return a NEW Vec3f(x, y, z) for bones we control.
     * For unmodified bones, return store unchanged.
     */
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (type != TransformType.ROTATION) return store;

        switch (modelName) {
            case "rightArm": return new Vec3f(-1.3f, 0.0f, -0.5f);
            case "leftArm":  return new Vec3f(-1.1f, 0.0f,  0.6f);
            case "body":     return new Vec3f( 0.05f, 0.0f, 0.0f);
            default:         return store;
        }
    }
}