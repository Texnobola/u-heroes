package com.uheroes.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitArmorItem;
import net.minecraft.client.Minecraft;
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
    public ResourceLocation getModelLocation(NanoSuitArmorItem animatable) {
        if (this.currentStack == null)
            return new ResourceLocation("u_heroes", "geo/nanosuit_helmet.geo.json");
        return switch (this.currentStack.getEquipmentSlot()) {
            case HEAD -> new ResourceLocation("u_heroes", "geo/nanosuit_helmet.geo.json");
            case CHEST -> new ResourceLocation("u_heroes", "geo/nanosuit_chestplate.geo.json");
            case LEGS -> new ResourceLocation("u_heroes", "geo/nanosuit_leggings.geo.json");
            case FEET -> new ResourceLocation("u_heroes", "geo/nanosuit_boots.geo.json");
            default -> new ResourceLocation("u_heroes", "geo/nanosuit_helmet.geo.json");
        };
    }
    
    @Override
    public ResourceLocation getTextureLocation(NanoSuitArmorItem animatable) {
        if (this.currentStack == null)
            return new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_helmet.png");
        return switch (this.currentStack.getEquipmentSlot()) {
            case HEAD -> new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_helmet.png");
            case CHEST -> new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_chestplate.png");
            case LEGS -> new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_leggings.png");
            case FEET -> new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_boots.png");
            default -> new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_helmet.png");
        };
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
