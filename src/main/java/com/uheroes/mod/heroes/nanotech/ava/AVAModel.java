package com.uheroes.mod.heroes.nanotech.ava;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for AVAEntity.
 *
 * <p>Geo:       {@code assets/u_heroes/geo/entity/ava_shield.geo.json}
 * <p>Texture:   {@code assets/u_heroes/textures/entity/ava.png}
 * <p>Animation: {@code assets/u_heroes/animations/ava_shield.animation.json}
 *
 * <p>Texture UV layout (256×256 sheet):
 * <pre>
 *   (0,   0)  16×16  — core glow dot
 *   (16,  0)   8×16  — ring_1 horizontal bars
 *   (16, 16)   8×16  — ring_1 vertical bars
 *   (32,  0)  16× 8  — ring_2 star bars
 *   (64,  0)  16× 8  — ring_3 octagon bars
 *   (128, 0)  24× 8  — hex_segments tile
 *   (0, 128) 128×64  — holo_plane_front (semi-transparent hex grid)
 * </pre>
 */
public class AVAModel extends GeoModel<AVAEntity> {

    private static final ResourceLocation GEO =
        new ResourceLocation("u_heroes", "geo/entity/ava_shield.geo.json");

    private static final ResourceLocation TEXTURE =
        new ResourceLocation("u_heroes", "textures/entity/ava.png");

    private static final ResourceLocation ANIMATION =
        new ResourceLocation("u_heroes", "animations/ava_shield.animation.json");

    @Override
    public ResourceLocation getModelResource(AVAEntity entity) {
        return GEO;
    }

    @Override
    public ResourceLocation getTextureResource(AVAEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AVAEntity entity) {
        return ANIMATION;
    }
}