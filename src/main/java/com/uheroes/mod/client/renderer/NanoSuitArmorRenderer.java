package com.uheroes.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * GeckoLib renderer for Nano-Tech Suit armor.
 */
public class NanoSuitArmorRenderer extends GeoArmorRenderer<NanoSuitArmorItem> {
    public NanoSuitArmorRenderer() {
        super(new GeoModel<NanoSuitArmorItem>() {
            @Override
            public ResourceLocation getModelResource(NanoSuitArmorItem animatable) {
                return new ResourceLocation("u_heroes", "geo/nanosuit_helmet.geo.json");
            }
            @Override
            public ResourceLocation getTextureResource(NanoSuitArmorItem animatable) {
                return new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_helmet.png");
            }
            @Override
            public ResourceLocation getAnimationResource(NanoSuitArmorItem animatable) {
                return null;
            }
        });
    }
    
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() == null || mc.level == null) return;
        try {
            super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        } catch (NullPointerException ignored) {}
    }
}
