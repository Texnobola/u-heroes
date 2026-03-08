package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;

public class SaberBlockAnimation implements IAnimation {

    public static final SaberBlockAnimation INSTANCE = new SaberBlockAnimation();

    private boolean active = false;

    private SaberBlockAnimation() {}

    public void setActive(boolean active) { this.active = active; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void setupAnim(float tickDelta) {}

    /**
     * Saber high guard pose — blade points upward-diagonal in front of body.
     *
     * z was negative (-0.9) → blade pointed DOWN (wrong).
     * z flipped to positive (+0.7) → rolls arm other direction, blade points UP.
     * x reduced to -0.8 → arm not as far forward, more natural raised guard.
     * y 0.35 → arm crossed slightly toward body center.
     */
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (type != TransformType.ROTATION) return store;

        switch (modelName) {
            case "rightArm": return new Vec3f(-0.8f,  0.35f,  0.7f);
            case "leftArm":  return new Vec3f(-0.4f,  0.0f,   0.3f);
            case "body":     return new Vec3f( 0.08f, 0.15f,  0.0f);
            default:         return store;
        }
    }
}