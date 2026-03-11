package com.uheroes.mod.init;

import com.uheroes.mod.UHeroesMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, UHeroesMod.MOD_ID);

    public static final RegistryObject<Block> NANO_ALLOY_BLOCK = BLOCKS.register("nano_alloy_block",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(5.0f, 8.0f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()));

    public static void registerBlockItems(DeferredRegister<Item> items) {
        items.register("nano_alloy_block",
            () -> new BlockItem(NANO_ALLOY_BLOCK.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
