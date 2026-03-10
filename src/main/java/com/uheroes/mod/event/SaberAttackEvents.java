package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.hud.FluxMeterHUD;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import mod.chloeprime.aaaparticles.api.client.effekseer.AAALevel;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class SaberAttackEvents {
    
    private static final ParticleEmitterInfo SABER_SLASH = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "effeks/saber_slash")
    );
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) {
            return;
        }
        
        if (player.level().isClientSide) {
            int currentCombo = FluxMeterHUD.comboIndex;
            
            float scale = switch (currentCombo) {
                case 7 -> 1.2f;
                case 8 -> 1.6f;
                case 9 -> 2.2f;
                default -> 1.0f;
            };
            
            AAALevel.addParticle(
                player.level(),
                false,
                SABER_SLASH.clone()
                    .position(player.getX(), player.getY() + 1.0, player.getZ())
                    .rotation(0, player.getYRot(), 0)
                    .scale(scale)
            );
        }
    }
}
