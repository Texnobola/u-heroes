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

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class SaberBlockEvents {
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (player.level().isClientSide()) {
            return;
        }
        
        if (!player.isCrouching()) {
            return;
        }
        
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) {
            return;
        }
        
        if (!FluxCapability.consume(player, 1)) {
            return;
        }
        
        event.setAmount(event.getAmount() * 0.20f);
        
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 8; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.4;
                double offsetY = (player.getRandom().nextDouble() - 0.5) * 0.4;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.4;
                
                serverLevel.sendParticles(ParticleTypes.CRIT,
                    player.getX() + offsetX,
                    player.getY() + 1.2 + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0.15);
            }
        }
    }
}
