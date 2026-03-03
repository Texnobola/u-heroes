package com.uheroes.mod.init;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitArmorItem;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, UHeroesMod.MOD_ID);

    public static final RegistryObject<Item> NANO_HELMET = ITEMS.register("nano_helmet",
        () -> new NanoSuitArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    
    public static final RegistryObject<Item> NANO_CHESTPLATE = ITEMS.register("nano_chestplate",
        () -> new NanoSuitArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    
    public static final RegistryObject<Item> NANO_LEGGINGS = ITEMS.register("nano_leggings",
        () -> new NanoSuitArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    
    public static final RegistryObject<Item> NANO_BOOTS = ITEMS.register("nano_boots",
        () -> new NanoSuitArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));
    
    public static final RegistryObject<Item> LASER_SWORD = ITEMS.register("laser_sword",
        () -> new LaserSwordItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
