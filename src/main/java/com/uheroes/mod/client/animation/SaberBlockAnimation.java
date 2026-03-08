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
     * Saber guard pose — right hand raised and crossed in front of body,
     * saber angled diagonally upward as a defensive stance.
     *
     * Key difference from bare-hand block:
     *   rightArm y: rotated inward so arm crosses body toward center
     *   rightArm z: rolled so the grip faces properly, blade angled up-forward
     *   leftArm: pulled back as counterbalance
     *
     * All values in radians.
     */
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (type != TransformType.ROTATION) return store;

        switch (modelName) {
            // Right arm: raised forward, crossed inward, rolled so saber points diagonally up
            case "rightArm": return new Vec3f(-1.2f, 0.4f, -0.9f);
            // Left arm: slightly raised and pulled back — counterbalance stance
            case "leftArm":  return new Vec3f(-0.5f, 0.0f,  0.4f);
            // Body: slight forward lean into the guard
            case "body":     return new Vec3f( 0.1f, 0.15f, 0.0f);
            default:         return store;
        }
    }
}