package com.uheroes.mod.init;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.ava.AVABlasterEntity;
import com.uheroes.mod.heroes.nanotech.ava.AVAEntity;
import com.uheroes.mod.origin.AsteroidEntity;
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

    public static final RegistryObject<EntityType<AVAEntity>> AVA =
        ENTITIES.register("ava", () -> EntityType.Builder
            .<AVAEntity>of(AVAEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(32)
            .updateInterval(1)
            .noSummon()
            .build("ava"));

    public static final RegistryObject<EntityType<AVABlasterEntity>> AVA_BLASTER =
        ENTITIES.register("ava_blaster", () -> EntityType.Builder
            .<AVABlasterEntity>of(AVABlasterEntity::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(32)
            .updateInterval(1)
            .noSummon()
            .build("ava_blaster"));

    public static final RegistryObject<EntityType<AsteroidEntity>> ASTEROID =
        ENTITIES.register("asteroid", () -> EntityType.Builder
            .<AsteroidEntity>of(AsteroidEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)   // Small hitbox — visuals are handled by GeckoLib model
            .clientTrackingRange(128)
            .updateInterval(1)   // Sync position every tick for smooth flight
            .build("asteroid"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}