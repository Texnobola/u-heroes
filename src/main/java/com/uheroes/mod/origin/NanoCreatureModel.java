package com.uheroes.mod.origin;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NanoCreatureModel extends GeoModel<NanoCreatureEntity> {

    @Override
    public ResourceLocation getModelResource(NanoCreatureEntity entity) {
        return new ResourceLocation("u_heroes", "geo/entity/nano_creature.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NanoCreatureEntity entity) {
        return new ResourceLocation("u_heroes", "textures/entity/armor/nano_creature.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NanoCreatureEntity entity) {
        return new ResourceLocation("u_heroes", "animations/nano_creature.animation.json");
    }
}