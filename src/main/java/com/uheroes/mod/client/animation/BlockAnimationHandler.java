package com.uheroes.mod.client.animation;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import dev.kosmx.playerAnim.api.layered.IAnimation;
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
    
    private static LocalPlayer lastRegisteredPlayer = null;
    private static final ModifierLayer<IAnimation> BLOCK_LAYER = new ModifierLayer<>();
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            lastRegisteredPlayer = null;
            return;
        }
        
        if (player != lastRegisteredPlayer) {
            try {
                PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(10, BLOCK_LAYER);
                lastRegisteredPlayer = player;
            } catch (Exception e) {
                UHeroesMod.LOGGER.warn("Failed to register saber block animation layer: {}", e.getMessage());
                return;
            }
        }
        
        boolean shouldBlock = player.isCrouching() && player.getMainHandItem().getItem() instanceof LaserSwordItem;
        
        if (shouldBlock) {
            SaberBlockAnimation.INSTANCE.setActive(true);
            if (BLOCK_LAYER.getAnimation() != SaberBlockAnimation.INSTANCE) {
                BLOCK_LAYER.setAnimation(SaberBlockAnimation.INSTANCE);
            }
        } else {
            SaberBlockAnimation.INSTANCE.setActive(false);
            BLOCK_LAYER.setAnimation(null);
        }
    }
}
