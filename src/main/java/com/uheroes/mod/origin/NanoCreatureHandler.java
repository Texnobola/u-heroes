package com.uheroes.mod.origin;

import com.uheroes.mod.event.AVAEvents;
import com.uheroes.mod.heroes.nanotech.ava.AVACapability;
import com.uheroes.mod.init.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class NanoCreatureHandler {
    public static void grantNanoSuit(ServerPlayer player) {
        player.setItemSlot(EquipmentSlot.HEAD,  new ItemStack(ModItems.NANO_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.NANO_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS,  new ItemStack(ModItems.NANO_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET,  new ItemStack(ModItems.NANO_BOOTS.get()));
        player.getInventory().add(new ItemStack(ModItems.LASER_SWORD.get()));

        // Spawn AVA companion immediately after suit integration
        if (player.level() instanceof ServerLevel level) {
            player.getCapability(AVACapability.INSTANCE).ifPresent(ava ->
                AVAEvents.spawnAVA(player, level, ava));
        }
    }
}