package com.uheroes.mod.heroes.nanotech.weapon;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

/**
 * Tool tier for Nano-Tech weapons.
 */
public class NanoTechToolTier {
    public static final Tier NANO_TECH = new ForgeTier(
        4,
        2500,
        9.0f,
        4.0f,
        18,
        BlockTags.NEEDS_DIAMOND_TOOL,
        () -> Ingredient.of(Items.NETHERITE_INGOT)
    );
}
