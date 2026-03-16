package com.uheroes.mod.client.renderer;

import com.uheroes.mod.origin.AsteroidEntity;
import com.uheroes.mod.origin.AsteroidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AsteroidRenderer extends GeoEntityRenderer<AsteroidEntity> {

    public AsteroidRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new AsteroidModel());
        this.shadowRadius = 0f; // No shadow for flying entity
    }

    @Override
    public ResourceLocation getTextureLocation(AsteroidEntity entity) {
        return new ResourceLocation("u_heroes", "textures/entity/asteroid.png");
    }
}