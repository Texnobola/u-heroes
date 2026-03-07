package com.uheroes.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class NanoSuitArmorRenderer extends GeoArmorRenderer<NanoSuitArmorItem> {

    public NanoSuitArmorRenderer() {
        super(new GeoModel<NanoSuitArmorItem>() {
            @Override
            public ResourceLocation getModelResource(NanoSuitArmorItem animatable) {
                return switch (animatable.getType()) {
                    case HELMET     -> new ResourceLocation("u_heroes", "geo/nanosuit_helmet.geo.json");
                    case CHESTPLATE -> new ResourceLocation("u_heroes", "geo/nanosuit_chestplate.geo.json");
                    case LEGGINGS   -> new ResourceLocation("u_heroes", "geo/nanosuit_leggings.geo.json");
                    case BOOTS      -> new ResourceLocation("u_heroes", "geo/nanosuit_boots.geo.json");
                };
            }
            @Override
            public ResourceLocation getTextureResource(NanoSuitArmorItem animatable) {
                return switch (animatable.getType()) {
                    case HELMET     -> new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_helmet.png");
                    case CHESTPLATE -> new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_chestplate.png");
                    case LEGGINGS   -> new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_leggings.png");
                    case BOOTS      -> new ResourceLocation("u_heroes", "textures/entity/armor/nanosuit_boots.png");
                };
            }
            @Override
            public ResourceLocation getAnimationResource(NanoSuitArmorItem animatable) {
                return null;
            }
        });
    }
}