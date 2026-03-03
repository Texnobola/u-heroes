package com.uheroes.mod.init;

import com.uheroes.mod.UHeroesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UHeroesMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> U_HEROES_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.u_heroes.main"))
            .icon(() -> Items.NETHERITE_CHESTPLATE.getDefaultInstance())
            .displayItems((params, output) -> {
                output.accept(ModItems.LASER_SWORD.get());
                output.accept(ModItems.NANO_HELMET.get());
                output.accept(ModItems.NANO_CHESTPLATE.get());
                output.accept(ModItems.NANO_LEGGINGS.get());
                output.accept(ModItems.NANO_BOOTS.get());
            })
            .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
