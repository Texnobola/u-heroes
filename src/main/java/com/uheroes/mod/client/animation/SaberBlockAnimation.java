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

    /**
     * Called once per frame before per-bone transforms.
     * No per-frame setup needed for a static pose — leave empty.
     */
    @Override
    public void setupAnim(float tickDelta) {
        // static pose — no per-frame state needed
    }

    /**
     * Called per bone per frame. Return modified store for bones we control,
     * return store unchanged for everything else (vanilla handles them).
     *
     * Vec3f fields are direct public fields: store.x, store.y, store.z
     * Values are in radians.
     */
    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f store) {
        if (type != TransformType.ROTATION) return store;

        switch (modelName) {
            case "rightArm":
                store.x = -1.3f;
                store.z = -0.5f;
                break;
            case "leftArm":
                store.x = -1.1f;
                store.z =  0.6f;
                break;
            case "body":
                store.x =  0.05f;
                break;
            // head untouched — free look preserved
        }

        return store;
    }
}