package com.uheroes.mod.heroes.nanotech.weapon;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * GeckoLib renderer for Laser Sword.
 */
public class LaserSwordRenderer extends GeoItemRenderer<LaserSwordItem> {
    public LaserSwordRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation("u_heroes", "nanosuit_laser_saber")));
    }
}
