package com.uheroes.mod.init;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.origin.NanoCreatureEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UHeroesMod.MOD_ID);

    public static final RegistryObject<EntityType<NanoCreatureEntity>> NANO_CREATURE =
        ENTITIES.register("nano_creature", () -> EntityType.Builder
            .<NanoCreatureEntity>of(NanoCreatureEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 0.6f)
            .build("nano_creature"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
