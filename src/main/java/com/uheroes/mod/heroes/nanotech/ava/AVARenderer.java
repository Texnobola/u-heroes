package com.uheroes.mod.heroes.nanotech.ava;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * AVA renderer.
 *
 * <p>Scale is applied by wrapping super.render() in a pushPose/scale/popPose.
 * This is the most reliable approach across GeckoLib versions — preRender
 * signatures change between minor releases.
 *
 * <p>isShieldActive() and getRenderScale() both read SynchedEntityData, so
 * they return correct values on the client side.
 */
public class AVARenderer extends GeoEntityRenderer<AVAEntity> {

    public AVARenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new AVAModel());
        this.shadowRadius = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(AVAEntity entity) {
        return new ResourceLocation("u_heroes", "textures/entity/ava.png");
    }

    @Override
    public RenderType getRenderType(AVAEntity entity, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void render(AVAEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int light) {
        poseStack.pushPose();

        // Base size preset (Small/Medium/Large) read from synced data
        float base = entity.getRenderScale();
        // Expand model when shield is active — synced from server
        float mult  = entity.isShieldActive() ? 2.2f : 1.0f;
        float scale = base * mult;

        poseStack.scale(scale, scale, scale);
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
        poseStack.popPose();
    }
}