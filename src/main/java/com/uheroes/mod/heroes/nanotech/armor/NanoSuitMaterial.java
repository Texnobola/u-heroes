package com.uheroes.mod.heroes.nanotech.armor;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.init.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Armor material for Nano-Tech Suit.
 */
public class NanoSuitMaterial implements ArmorMaterial {
    private static final int[] DURABILITY = {13, 15, 16, 11};
    private static final int[] PROTECTION = {4, 9, 7, 4};
    
    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return DURABILITY[type.ordinal()] * 45;
    }
    
    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return PROTECTION[type.ordinal()];
    }
    
    @Override
    public int getEnchantmentValue() {
        return 18;
    }
    
    @Override
    public SoundEvent getEquipSound() {
        return ModSounds.NANO_SUIT_EQUIP.get();
    }
    
    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.NETHERITE_INGOT);
    }
    
    @Override
    public String getName() {
        return UHeroesMod.MOD_ID + ":nano_suit";
    }
    
    @Override
    public float getToughness() {
        return 3.5f;
    }
    
    @Override
    public float getKnockbackResistance() {
        return 0.2f;
    }
}
