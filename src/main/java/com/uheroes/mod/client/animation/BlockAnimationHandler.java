package com.uheroes.mod.client.animation;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
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
                PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(10, SaberBlockAnimation.INSTANCE);
                lastRegisteredPlayer = player;
            } catch (Exception e) {
                UHeroesMod.LOGGER.warn("Failed to register saber block animation layer: {}", e.getMessage());
                return;
            }
        }
        
        boolean shouldBlock = player.isCrouching() && player.getMainHandItem().getItem() instanceof LaserSwordItem;
        SaberBlockAnimation.INSTANCE.setActive(shouldBlock);
    }
}
