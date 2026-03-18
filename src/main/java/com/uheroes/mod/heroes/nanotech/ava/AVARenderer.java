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
 * Renders AVA with:
 * - Dynamic scale from entity (Small / Medium / Large presets)
 * - Shield active → scale up model to 2× to visually show barrier expansion
 * - Translucent render type for holo transparency
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
                                    MultiBufferSource buffer, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void render(AVAEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int light) {
        poseStack.pushPose();

        // Base size from entity preset (Small / Medium / Large)
        float base = entity.getRenderScale();
        // When shield is active, grow the visual model to show the barrier
        float shieldMult = entity.isShieldActive() ? 2.2f : 1.0f;
        float scale = base * shieldMult;

        poseStack.scale(scale, scale, scale);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, light);
        poseStack.popPose();
    }
}