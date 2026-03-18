package com.uheroes.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.uheroes.mod.heroes.nanotech.ava.AVAEntity;
import com.uheroes.mod.heroes.nanotech.ava.AVAModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Reads AVA's sizeIndex to pick the visual scale.
 * Sizes: Small=0.18, Medium=0.28, Large=0.42
 * Cycle with N key.
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
    public void render(AVAEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int light) {
        poseStack.pushPose();
        float s = entity.getRenderScale();
        poseStack.scale(s, s, s);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, light);
        poseStack.popPose();
    }
}