package com.uheroes.mod.client.animation;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import net.minecraft.client.model.geom.ModelPart;

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
        if (!active) {
            return;
        }
        
        ModelPart rightArm = this.get3DModel().rightArm;
        ModelPart leftArm = this.get3DModel().leftArm;
        ModelPart body = this.get3DModel().body;
        
        if (rightArm != null) {
            rightArm.xRot = -1.3f;
            rightArm.zRot = -0.5f;
        }
        
        if (leftArm != null) {
            leftArm.xRot = -1.1f;
            leftArm.zRot = 0.6f;
        }
        
        if (body != null) {
            body.xRot = 0.05f;
        }
    }
}
