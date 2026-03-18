package com.uheroes.mod.init;

import com.uheroes.mod.UHeroesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UHeroesMod.MOD_ID);

    public static final RegistryObject<SoundEvent> NANO_SUIT_EQUIP = SOUNDS.register("nano_suit_equip",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(UHeroesMod.MOD_ID, "nano_suit_equip")));

    public static final RegistryObject<SoundEvent> LASER_SWORD_SWING = SOUNDS.register("laser_sword_swing",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(UHeroesMod.MOD_ID, "laser_sword_swing")));

    public static final RegistryObject<SoundEvent> LASER_SWORD_HIT = SOUNDS.register("laser_sword_hit",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(UHeroesMod.MOD_ID, "laser_sword_hit")));

    public static final RegistryObject<SoundEvent> AVA_ACTIVATE = SOUNDS.register("ava_activate",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(UHeroesMod.MOD_ID, "ava_activate")));

    public static final RegistryObject<SoundEvent> AVA_SHIELD_ON = SOUNDS.register("ava_shield_on",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(UHeroesMod.MOD_ID, "ava_shield_on")));

    public static final RegistryObject<SoundEvent> AVA_BLOCK = SOUNDS.register("ava_block",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(UHeroesMod.MOD_ID, "ava_block")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}