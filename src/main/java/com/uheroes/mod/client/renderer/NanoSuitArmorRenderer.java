package com.uheroes.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * GeckoLib renderer for Nano-Tech Suit armor.
 */
public class NanoSuitArmorRenderer extends GeoArmorRenderer<NanoSuitArmorItem> {
    public NanoSuitArmorRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation("u_heroes", "armor/nanosuit")));
    }
    
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (Minecraft.getInstance().getCameraEntity() == null) return;
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
