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
public class SaberComboHandler {
    
    private static LocalPlayer lastRegisteredPlayer = null;
    private static int comboIndex = 0;
    private static int inactivityTicks = 0;
    private static float lastAttackStrength = 1.0f;
    
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
                PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(20, SaberComboAnimation.INSTANCE);
                lastRegisteredPlayer = player;
            } catch (Exception e) {
                UHeroesMod.LOGGER.warn("Failed to register saber animations: {}", e.getMessage());
                return;
            }
        }
        
        boolean shouldBlock = player.isCrouching() && player.getMainHandItem().getItem() instanceof LaserSwordItem;
        SaberBlockAnimation.INSTANCE.setActive(shouldBlock);
        
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) {
            return;
        }
        
        SaberComboAnimation.INSTANCE.tick();
        
        float currentAttackStrength = player.getAttackStrengthScale(0f);
        
        if (lastAttackStrength == 1.0f && currentAttackStrength < 1.0f && !SaberComboAnimation.INSTANCE.isPlaying()) {
            SaberComboAnimation.INSTANCE.startAttack(comboIndex);
            comboIndex = (comboIndex + 1) % 10;
            inactivityTicks = 0;
        }
        
        lastAttackStrength = currentAttackStrength;
        
        if (!SaberComboAnimation.INSTANCE.isPlaying()) {
            inactivityTicks++;
            if (inactivityTicks > 40) {
                comboIndex = 0;
                inactivityTicks = 0;
            }
        }
    }
}
