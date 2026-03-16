package com.uheroes.mod.origin;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AsteroidModel extends GeoModel<AsteroidEntity> {

    @Override
    public ResourceLocation getModelResource(AsteroidEntity entity) {
        return new ResourceLocation("u_heroes", "geo/entity/asteroid.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AsteroidEntity entity) {
        return new ResourceLocation("u_heroes", "textures/entity/asteroid.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AsteroidEntity entity) {
        return new ResourceLocation("u_heroes", "animations/asteroid.animation.json");
    }
}