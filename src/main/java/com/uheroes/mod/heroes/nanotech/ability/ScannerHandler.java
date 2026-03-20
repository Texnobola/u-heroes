package com.uheroes.mod.heroes.nanotech.ability;

import com.uheroes.mod.UHeroesMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.*;

/**
 * Server-side scanner logic.
 * Tracks which entities are "marked" by a player's scanner.
 * Marked entities are prioritised by AVA and highlighted on the client.
 */
public class ScannerHandler {

    public static final int SCAN_RANGE  = 24;   // blocks
    public static final int MAX_TARGETS = 8;

    // player UUID → set of marked entity IDs
    private static final Map<UUID, Set<Integer>> markedTargets = new HashMap<>();

    public static Set<Integer> getMarked(Player player) {
        return markedTargets.getOrDefault(player.getUUID(), Collections.emptySet());
    }

    public static boolean isMarked(Player player, int entityId) {
        return getMarked(player).contains(entityId);
    }

    /**
     * Called every tick while the player holds Z.
     * Refreshes the marked-target list from nearby hostiles.
     */
    public static void tickScan(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel sl)) return;

        List<LivingEntity> nearby = sl.getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(SCAN_RANGE),
            e -> e != player && e.isAlive() && isHostile(e, player)
        );

        // Sort by distance, cap at MAX_TARGETS
        nearby.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
        if (nearby.size() > MAX_TARGETS) nearby = nearby.subList(0, MAX_TARGETS);

        Set<Integer> ids = new HashSet<>();
        for (LivingEntity e : nearby) ids.add(e.getId());

        markedTargets.put(player.getUUID(), ids);
    }

    /** Called when scanner is released — clear marks. */
    public static void clearScan(ServerPlayer player) {
        markedTargets.remove(player.getUUID());
    }

    private static boolean isHostile(LivingEntity e, Player player) {
        if (e instanceof Monster) return true;
        if (e instanceof Player) return false;
        return !e.isAlliedTo(player);
    }
}