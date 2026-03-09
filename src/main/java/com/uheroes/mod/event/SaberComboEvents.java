package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.animation.SaberAttackData;
import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class SaberComboEvents {
    
    private static final Map<UUID, Integer> COMBO_INDEX = new HashMap<>();
    private static final Map<UUID, Long> LAST_ATTACK_TIME = new HashMap<>();
    private static final long COMBO_RESET_TIME = 2000L;
    
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        
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
        COMBO_INDEX.put(playerId, (comboIndex + 1) % 10);
        LAST_ATTACK_TIME.put(playerId, currentTime);
    }
    
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
        int comboIndex = COMBO_INDEX.getOrDefault(playerId, 0);
        
        if (comboIndex < 0 || comboIndex >= SaberAttackData.ATTACKS.length) {
            return;
        }
        
        SaberAttackData.Attack attack = SaberAttackData.ATTACKS[comboIndex];
        
        if (attack.fluxCost > 0) {
            if (!FluxCapability.consume(player, attack.fluxCost)) {
                return;
            }
            
            event.setAmount(event.getAmount() * attack.damageMultiplier);
            
            if (player.level() instanceof ServerLevel serverLevel) {
                int particleCount = comboIndex == 9 ? 20 : 12;
                var particleType = comboIndex == 9 ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.CRIT;
                
                serverLevel.sendParticles(particleType,
                    event.getEntity().getX(),
                    event.getEntity().getY() + 1.0,
                    event.getEntity().getZ(),
                    particleCount, 0.5, 0.5, 0.5, 0.2);
            }
        }
    }
}
