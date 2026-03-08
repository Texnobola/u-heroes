package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks Epic Fight combo progression for LaserSword and applies flux costs + damage multipliers.
 * Attacks 8-10 are special finisher moves with increasing flux costs and damage.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class EpicFightComboEvents {
    
    private static final Map<UUID, Integer> COMBO_INDEX = new HashMap<>();
    private static final Map<UUID, Long> LAST_ATTACK_TIME = new HashMap<>();
    private static final long COMBO_RESET_TIME = 2000L;
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        
        if (player.level().isClientSide()) {
            return;
        }
        
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) {
            return;
        }
        
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        long lastTime = LAST_ATTACK_TIME.getOrDefault(playerId, 0L);
        
        if (currentTime - lastTime > COMBO_RESET_TIME) {
            COMBO_INDEX.put(playerId, 0);
        }
        
        int comboIndex = COMBO_INDEX.getOrDefault(playerId, 0);
        comboIndex++;
        
        COMBO_INDEX.put(playerId, comboIndex);
        LAST_ATTACK_TIME.put(playerId, currentTime);
        
        if (comboIndex == 8) {
            if (FluxCapability.consume(player, 1)) {
                event.setAmount(event.getAmount() * 1.4f);
                spawnParticles(player, ParticleTypes.CRIT, 12);
            }
        } else if (comboIndex == 9) {
            if (FluxCapability.consume(player, 2)) {
                event.setAmount(event.getAmount() * 1.8f);
                spawnParticles(player, ParticleTypes.CRIT, 16);
            }
        } else if (comboIndex == 10) {
            if (FluxCapability.consume(player, 3)) {
                event.setAmount(event.getAmount() * 2.5f);
                spawnParticles(player, ParticleTypes.ENCHANTED_HIT, 20);
            }
            COMBO_INDEX.put(playerId, 0);
        }
    }
    
    private static void spawnParticles(Player player, net.minecraft.core.particles.ParticleOptions particle, int count) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particle,
                player.getX(), player.getY() + 1.0, player.getZ(),
                count, 0.5, 0.5, 0.5, 0.2);
        }
    }
}
