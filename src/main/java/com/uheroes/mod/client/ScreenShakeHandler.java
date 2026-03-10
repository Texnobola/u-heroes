package com.uheroes.mod.client;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.hud.FluxMeterHUD;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class ScreenShakeHandler {
    
    public static float shakeIntensity = 0f;
    public static int shakeTicks = 0;
    private static int totalShakeTicks = 0;
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LocalPlayer player)) {
            return;
        }
        
        if (!player.level().isClientSide()) {
            return;
        }
        
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) {
            return;
        }
        
        int comboIndex = FluxMeterHUD.comboIndex;
        
        if (comboIndex == 9) {
            shakeIntensity = 1.2f;
            shakeTicks = 8;
            totalShakeTicks = 8;
        } else if (comboIndex == 8) {
            shakeIntensity = 0.8f;
            shakeTicks = 6;
            totalShakeTicks = 6;
        } else if (comboIndex == 7) {
            shakeIntensity = 0.5f;
            shakeTicks = 4;
            totalShakeTicks = 4;
        }
    }
    
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (shakeTicks <= 0) {
            return;
        }
        
        float progress = shakeTicks / (float) totalShakeTicks;
        float offset = (float)(Math.sin(shakeTicks * 2.5) * shakeIntensity * progress);
        
        event.setYaw((float)(event.getYaw() + offset));
        event.setPitch((float)(event.getPitch() + offset * 0.5f));
        
        shakeTicks--;
    }
}
