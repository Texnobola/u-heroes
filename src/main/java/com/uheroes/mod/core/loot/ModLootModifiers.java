package com.uheroes.mod.core.loot;

import com.mojang.serialization.Codec;
import com.uheroes.mod.UHeroesMod;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, UHeroesMod.MOD_ID);

    public static final RegistryObject<Codec<LifeEssenceLootModifier>> LIFE_ESSENCE_FROM_LEAVES =
        LOOT_MODIFIERS.register("life_essence_from_leaves", LifeEssenceLootModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }
}