package com.uheroes.mod;

import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.core.loot.ModLootModifiers;
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
        ModLootModifiers.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::init);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(ModKeybinds::registerKeybinds);
    }

    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onEntityAttributeCreation(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
            event.put(ModEntities.NANO_CREATURE.get(),
                com.uheroes.mod.origin.NanoCreatureEntity.createAttributes().build());
            event.put(ModEntities.AVA.get(),
                com.uheroes.mod.heroes.nanotech.ava.AVAEntity.createAttributes().build());
            // AsteroidEntity extends Entity, not LivingEntity — no attributes needed
        }
    }

    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class ClientModEvents {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onRegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(
                ModEntities.NANO_CREATURE.get(),
                com.uheroes.mod.client.renderer.NanoCreatureRenderer::new
            );
            event.registerEntityRenderer(
                ModEntities.ASTEROID.get(),
                com.uheroes.mod.client.renderer.AsteroidRenderer::new
            );
            // AVA uses Minecraft's default invisible renderer until GeckoLib model is ready
            event.registerEntityRenderer(
                ModEntities.AVA.get(),
                com.uheroes.mod.client.renderer.AVARenderer::new
            );
        }
    }
}