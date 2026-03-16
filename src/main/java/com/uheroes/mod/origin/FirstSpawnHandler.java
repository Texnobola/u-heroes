package com.uheroes.mod.origin;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.thewinnt.cutscenes.CutsceneManager;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class FirstSpawnHandler {

    private static final String FLAG = "u_heroes_origin_spawned";
    private static final ResourceLocation ASTEROID_CUTSCENE =
        new ResourceLocation(UHeroesMod.MOD_ID, "asteroid_impact");

    private static final double SPAWN_OFFSET_X =   0;
    private static final double SPAWN_OFFSET_Y = 220;
    private static final double SPAWN_OFFSET_Z =  80;

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(Level.OVERWORLD)) return;

        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(FLAG)) return;
        data.putBoolean(FLAG, true);

        // Give guide book immediately on first join
        giveGuideBook(player);

        // Delay 2s for world/client to be ready, then trigger cinematic
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (Exception ignored) {}
            serverLevel.getServer().execute(() -> {

                BlockPos spawnPos = player.getRespawnPosition() != null
                    ? player.getRespawnPosition()
                    : serverLevel.getSharedSpawnPos();

                BlockPos craterPos = AsteroidCraterGenerator.pickCraterPos(serverLevel, spawnPos);
                Vec3 craterCenter  = Vec3.atCenterOf(craterPos);

                UHeroesMod.LOGGER.info("[U-Heroes] Asteroid will impact at {}", craterPos);

                AsteroidEntity asteroid = ModEntities.ASTEROID.get().create(serverLevel);
                if (asteroid != null) {
                    Vec3 spawnVec = craterCenter.add(SPAWN_OFFSET_X, SPAWN_OFFSET_Y, SPAWN_OFFSET_Z);
                    asteroid.moveTo(spawnVec.x, spawnVec.y, spawnVec.z, 0f, 0f);
                    asteroid.initFlight(craterCenter);
                    serverLevel.addFreshEntity(asteroid);
                }

                CutsceneManager.startCutscene(
                    ASTEROID_CUTSCENE,
                    craterCenter,
                    Vec3.ZERO,
                    Vec3.ZERO,
                    player
                );
            });
        }).start();
    }

    private static void giveGuideBook(ServerPlayer player) {
        try {
            if (!ModList.get().isLoaded("patchouli")) return;

            var bookItem = ForgeRegistries.ITEMS
                .getValue(new ResourceLocation("patchouli", "guide_book"));
            if (bookItem == null) return;

            ItemStack guideStack = new ItemStack(bookItem);
            CompoundTag tag = new CompoundTag();
            tag.putString("patchouli:book", "u_heroes:u_heroes_guide");
            guideStack.setTag(tag);

            player.addItem(guideStack);
        } catch (Exception e) {
            UHeroesMod.LOGGER.warn("[U-Heroes] Could not give guide book: {}", e.getMessage());
        }
    }
}