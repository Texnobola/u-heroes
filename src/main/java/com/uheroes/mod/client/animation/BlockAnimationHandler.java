package com.uheroes.mod.client.animation;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class BlockAnimationHandler {
    
    private static final String LAYER_KEY = "u_heroes_saber_block";
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        
        boolean shouldBlock = player.isCrouching() && player.getMainHandItem().getItem() instanceof LaserSwordItem;
        
        try {
            ModifierLayer<SaberBlockAnimation> animLayer = (ModifierLayer<SaberBlockAnimation>) 
                PlayerAnimationAccess.getPlayerAnimLayer(player).get(LAYER_KEY);
            
            if (animLayer == null) {
                animLayer = new ModifierLayer<>(SaberBlockAnimation.INSTANCE);
                PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(10, LAYER_KEY, animLayer);
            }
            
            if (shouldBlock) {
                SaberBlockAnimation.INSTANCE.setActive(true);
                if (animLayer.getAnimation() != SaberBlockAnimation.INSTANCE) {
                    animLayer.setAnimation(SaberBlockAnimation.INSTANCE);
                }
            } else {
                SaberBlockAnimation.INSTANCE.setActive(false);
                animLayer.setAnimation(null);
            }
        } catch (Exception e) {
            UHeroesMod.LOGGER.warn("Failed to update saber block animation: {}", e.getMessage());
        }
    }
}
