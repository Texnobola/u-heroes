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
public class SaberAttackAnimHandler {

    private static LocalPlayer lastPlayer = null;
    private static int swingCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { lastPlayer = null; return; }

        if (player != lastPlayer) {
            try {
                PlayerAnimationAccess.getPlayerAnimLayer(player)
                        .addAnimLayer(25, SaberAttackAnimation.INSTANCE);
                lastPlayer = player;
            } catch (Exception e) {
                UHeroesMod.LOGGER.warn("[U-Heroes] Could not register attack anim layer: {}", e.getMessage());
            }
        }
    }

    public static void onSwing() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) return;

        SaberAttackAnimation.INSTANCE.play(swingCounter % 3);
        swingCounter++;
    }
}
