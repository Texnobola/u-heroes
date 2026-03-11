package com.uheroes.mod.client.renderer;

import com.uheroes.mod.origin.NanoCreatureEntity;
import com.uheroes.mod.origin.NanoCreatureModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NanoCreatureRenderer extends GeoEntityRenderer<NanoCreatureEntity> {

    public NanoCreatureRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new NanoCreatureModel());
    }

    @Override
    public ResourceLocation getTextureLocation(NanoCreatureEntity entity) {
        return new ResourceLocation("u_heroes", "textures/entity/nano_creature.png");
    }
}
