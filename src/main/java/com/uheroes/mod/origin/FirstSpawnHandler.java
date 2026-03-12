package com.uheroes.mod.origin;

import com.uheroes.mod.UHeroesMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thewinnt.cutscenes.CutsceneManager;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class FirstSpawnHandler {

    private static final String FLAG = "u_heroes_origin_spawned";
    private static final ResourceLocation ASTEROID_CUTSCENE =
        new ResourceLocation(UHeroesMod.MOD_ID, "asteroid_impact");

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        CompoundTag data = player.getPersistentData();

        // Only trigger once per player
        if (data.getBoolean(FLAG)) return;
        data.putBoolean(FLAG, true);

        // Delay 2s so player connection is fully established and world is loaded on client
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (Exception ignored) {}
            serverLevel.getServer().execute(() -> {
                BlockPos spawnPos = player.getRespawnPosition() != null
                    ? player.getRespawnPosition()
                    : serverLevel.getSharedSpawnPos();

                BlockPos craterPos = AsteroidCraterGenerator.generate(serverLevel, spawnPos);

                UHeroesMod.LOGGER.info("[U-Heroes] Asteroid crater generated at {}", craterPos);

                // Start CutsceneAPI cinematic, centered on crater
                Vec3 craterCenter = Vec3.atCenterOf(craterPos);
                CutsceneManager.startCutscene(
                    ASTEROID_CUTSCENE,
                    craterCenter,  // startPos — path offsets are relative to this
                    Vec3.ZERO,     // camRot — rotation handled by look_at_point in JSON
                    Vec3.ZERO,     // pathRot — no path rotation
                    player
                );
            });
        }).start();
    }
}