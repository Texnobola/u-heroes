package com.uheroes.mod.init;

import com.uheroes.mod.UHeroesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UHeroesMod.MOD_ID);

    public static final RegistryObject<SoundEvent> NANO_SUIT_EQUIP = SOUNDS.register("nano_suit_equip",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(UHeroesMod.MOD_ID, "nano_suit_equip")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
