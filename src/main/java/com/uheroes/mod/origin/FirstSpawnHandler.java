package com.uheroes.mod.origin;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.network.AsteroidPositionPacket;
import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.core.network.TriggerImpactSequencePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class FirstSpawnHandler {

    private static final String FLAG = "u_heroes_origin_spawned";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        CompoundTag data = player.getPersistentData();

        // Only trigger once per player
        if (data.getBoolean(FLAG)) return;
        data.putBoolean(FLAG, true);

        // Delay by 1 tick so world is fully loaded
        serverLevel.getServer().execute(() -> {
            BlockPos spawnPos = player.getRespawnPosition() != null
                ? player.getRespawnPosition()
                : serverLevel.getSharedSpawnPos();

            BlockPos craterPos = AsteroidCraterGenerator.generate(serverLevel, spawnPos);

            UHeroesMod.LOGGER.info("[U-Heroes] Asteroid crater generated at {}", craterPos);

            // Send crater position to client
            ModNetwork.sendToPlayer(new AsteroidPositionPacket(craterPos), player);

            // Trigger cinematic sequence on client
            ModNetwork.sendToPlayer(new TriggerImpactSequencePacket(), player);
        });
    }
}
