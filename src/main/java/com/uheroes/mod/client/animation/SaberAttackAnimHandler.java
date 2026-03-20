package com.uheroes.mod.client.animation;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SaberAttackAnimHandler {

    private static final ResourceLocation ATTACK_LAYER_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "saber_attack_layer");

    private static final ResourceLocation[] ATTACK_ANIMS = {
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.saber.attack1"),
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.saber.attack2"),
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.saber.attack3"),
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.saber.attack4"),
    };

    public static int lastSwingIndex = 0; // used by SaberAttackEvents for VFX roll
    private static int swingCounter  = 0;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
            ATTACK_LAYER_ID, 28,  // above ride(22), below punch(30)
            (AbstractClientPlayer p) -> new ModifierLayer<>()
        );
        UHeroesMod.LOGGER.debug("[U-Heroes] Saber attack animation layer registered");
    }

    /** Called from SaberAttackEvents when the player swings. Cycles through 4 attacks. */
    @SuppressWarnings("unchecked")
    public static void onSwing() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) return;

        lastSwingIndex = swingCounter % 4;
        swingCounter++;

        var data = PlayerAnimationAccess.getPlayerAssociatedData(player);
        ModifierLayer<IAnimation> layer =
            (ModifierLayer<IAnimation>) data.get(ATTACK_LAYER_ID);
        if (layer == null) return;

        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(ATTACK_ANIMS[lastSwingIndex]);
        if (anim == null) {
            UHeroesMod.LOGGER.warn("[U-Heroes] Attack anim {} not found!", ATTACK_ANIMS[lastSwingIndex]);
            return;
        }
        layer.setAnimation(new KeyframeAnimationPlayer(anim));
    }

    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
    public static class TickHandler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            // Nothing needed here — PlayerAnimationFactory handles layer lifecycle
        }
    }
}