package com.uheroes.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class NanoSuitArmorRenderer extends GeoArmorRenderer<NanoSuitArmorItem> {
    private HumanoidModel<?> storedBaseModel;

    public NanoSuitArmorRenderer() {
        super(new NanoSuitGeoModel());
    }

    public void setBaseModel(HumanoidModel<?> model) {
        this.storedBaseModel = model;
    }

    /**
     * GeckoLib's prepForRender() copies HumanoidModel bones to GeckoLib bones
     * using vanilla names: "head", "body", "rightArm", "leftArm".
     * Our geo.json uses "armorHead", "armorBody", "armorRightArm", "armorLeftArm", etc.
     * so prepForRender finds nothing. We manually copy ALL bones here instead.
     *
     * This is what makes PlayerAnimator poses (attack, punch, ride) apply to the armor.
     */
    @Override
    public void preRender(PoseStack poseStack, NanoSuitArmorItem animatable,
                         BakedGeoModel model, MultiBufferSource bufferSource,
                         VertexConsumer buffer, boolean isReRender, float partialTick,
                         int packedLight, int packedOverlay,
                         float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer,
                isReRender, partialTick, packedLight, packedOverlay,
                red, green, blue, alpha);
        if (storedBaseModel == null) return;

        // Copy every animated bone from the PlayerAnimator-modified HumanoidModel
        // to the correct "armorXxx" bones in our GeckoLib model
        applyPartToBone(storedBaseModel.head,     "armorHead");
        applyPartToBone(storedBaseModel.body,     "armorBody");
        applyPartToBone(storedBaseModel.rightArm, "armorRightArm");
        applyPartToBone(storedBaseModel.leftArm,  "armorLeftArm");
        applyPartToBone(storedBaseModel.rightLeg, "armorRightLeg");
        applyPartToBone(storedBaseModel.leftLeg,  "armorLeftLeg");
        applyPartToBone(storedBaseModel.rightLeg, "armorRightBoot");
        applyPartToBone(storedBaseModel.leftLeg,  "armorLeftBoot");
    }

    private void applyPartToBone(ModelPart part, String boneName) {
        getGeoModel().getBone(boneName).ifPresent(bone -> {
            bone.setRotX(part.xRot);
            bone.setRotY(part.yRot);
            bone.setRotZ(part.zRot);
            // Also copy position offsets so translate keyframes work too
            bone.setPosX(part.x);
            bone.setPosY(part.y);
            bone.setPosZ(part.z);
        });
    }

    private static class NanoSuitGeoModel extends GeoModel<NanoSuitArmorItem> {
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
    }
}