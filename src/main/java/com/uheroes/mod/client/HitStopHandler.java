package com.uheroes.mod.client;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.hud.FluxMeterHUD;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class HitStopHandler {
    
    public static int hitStopTicks = 0;
    private static float originalTickLength = 50.0f;
    
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
        
        if (comboIndex >= 9) {
            hitStopTicks = 4;
        } else if (comboIndex >= 7) {
            hitStopTicks = 3;
        } else {
            hitStopTicks = 2;
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        
        if (hitStopTicks > 0) {
            mc.timer.tickDelta = 0.0f;
            hitStopTicks--;
        } else {
            mc.timer.tickDelta = 1.0f;
        }
    }
}
