package com.uheroes.mod.heroes.nanotech.armor;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.core.flux.FluxCapability;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handler for Nano-Tech Suit passive effects.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class NanoSuitHandler {
    
    private static final java.util.Map<java.util.UUID, Integer> prevPieceCount = new java.util.HashMap<>();
    
    /**
     * Counts how many Nano Suit pieces the player is wearing.
     */
    public static int getNanoSuitPieceCount(Player player) {
        int count = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof NanoSuitArmorItem) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Checks if player is wearing full Nano Suit.
     */
    public static boolean isWearingFullNanoSuit(Player player) {
        return getNanoSuitPieceCount(player) == 4;
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        
        Player player = event.player;
        int pieces = getNanoSuitPieceCount(player);
        
        int prev = prevPieceCount.getOrDefault(player.getUUID(), 0);
        if (pieces > prev) {
            // Player just equipped a new Nano piece
            player.level().playSound(null,
                player.blockPosition(),
                com.uheroes.mod.init.ModSounds.NANO_SUIT_EQUIP.get(),
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.0f, 1.0f);
        }
        prevPieceCount.put(player.getUUID(), pieces);
        
        if (pieces == 0) {
            return;
        }
        
        // Apply movement effects based on pieces worn
        if (pieces >= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 0, true, false));
        }
        
        if (pieces >= 3) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 0, true, false));
        }
        
        if (pieces == 4) {
            // Upgrade Strength to level II for full set
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1, true, false));
            
            // Self-Repair: repair armor every 60 ticks
            if (player.tickCount % 60 == 0) {
                repairArmor(player);
            }
            
            // Adaptive Regeneration
            if (player.getHealth() < player.getMaxHealth() && player.getFoodData().getSaturationLevel() > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 0, true, false));
                
                // Consume flux every 40 ticks while healing
                if (player.tickCount % 40 == 0) {
                    if (FluxCapability.consume(player, 1)) {
                        player.getFoodData().setSaturation(player.getFoodData().getSaturationLevel() - 1.0f);
                    }
                }
            }
            
            // Flux Amplification: 50% bonus regen (once per second)
            if (player.tickCount % 20 == 0) {
                player.getCapability(FluxCapability.INSTANCE).ifPresent(flux -> {
                    if (flux.getCurrentFlux() < flux.getMaxFlux()) {
                        int bonusRegen = Math.max(1, Math.round(flux.getRegenRate() * 0.5f));
                        flux.addFlux(bonusRegen);
                    }
                });
            }
        }
    }
    
    private static void repairArmor(Player player) {
        boolean repaired = false;
        
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof NanoSuitArmorItem && armor.isDamaged()) {
                if (FluxCapability.consume(player, 1)) {
                    armor.setDamageValue(armor.getDamageValue() - 1);
                    repaired = true;
                }
            }
        }
        
        if (repaired && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, 
                SoundSource.PLAYERS, 0.3f, 2.0f);
        }
    }
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player) || !player.level().isClientSide) {
            return;
        }
        
        if (isWearingFullNanoSuit(player) && player.tickCount % 20 == 0) {
            // Spawn cyan electric particles
            for (int i = 0; i < 4; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.8;
                double offsetY = player.getRandom().nextDouble() * 1.5;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.8;
                
                player.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    0, 0.05, 0);
            }
        }
    }
}
