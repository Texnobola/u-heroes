package com.uheroes.mod;

import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.init.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(UHeroesMod.MOD_ID)
public class UHeroesMod {
    public static final String MOD_ID = "u_heroes";
    public static final Logger LOGGER = LogManager.getLogger();

    public UHeroesMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModParticles.register(modEventBus);
        ModCreativeTab.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::init);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(ModKeybinds::registerKeybinds);
    }
}
