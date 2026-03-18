package com.uheroes.mod.client.renderer;

import com.uheroes.mod.heroes.nanotech.ava.AVAEntity;
import com.uheroes.mod.heroes.nanotech.ava.AVAModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renders AVA using GeckoLib.
 *
 * <p>Uses {@link RenderType#entityTranslucent} so the semi-transparent
 * holo plane and shield rings blend correctly. The model will play
 * the "idle" animation by default — add animation controller in
 * {@link AVAEntity} via GeckoLib's AnimatableManager when ready.
 */
public class AVARenderer extends GeoEntityRenderer<AVAEntity> {

    public AVARenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new AVAModel());
        // No shadow — AVA floats and a shadow would look wrong
        this.shadowRadius = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(AVAEntity entity) {
        return new ResourceLocation("u_heroes", "textures/entity/ava.png");
    }

    @Override
    public RenderType getRenderType(AVAEntity entity, ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        // Translucent so the holo plane alpha renders correctly
        return RenderType.entityTranslucent(texture);
    }
}