package com.uheroes.mod.client.animation;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler;
import com.uheroes.mod.heroes.nanotech.ava.AVAEntity;
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

/**
 * Handles two nano-suit animations:
 * - nano_walk: active when moving on ground with full suit
 * - nano_ride: active when riding AVA (skate/surfboard pose)
 *
 * Uses the correct PlayerAnimationFactory API — registers once per player,
 * never duplicates layers.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NanoSuitWalkAnimHandler {

    private static final ResourceLocation WALK_LAYER_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "nano_walk_layer");
    private static final ResourceLocation RIDE_LAYER_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "nano_ride_layer");

    private static final ResourceLocation WALK_ANIM_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.nano.walk");
    private static final ResourceLocation RIDE_ANIM_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.nano.ride");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Walk layer — priority 20
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
            WALK_LAYER_ID, 20,
            (AbstractClientPlayer p) -> new ModifierLayer<>()
        );
        // Ride layer — priority 22 (above walk)
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
            RIDE_LAYER_ID, 22,
            (AbstractClientPlayer p) -> new ModifierLayer<>()
        );
        UHeroesMod.LOGGER.debug("[U-Heroes] Walk/ride animation layers registered");
    }

    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
    public static class TickHandler {

        private static boolean wasWalking = false;
        private static boolean wasRiding  = false;

        @SubscribeEvent
        @SuppressWarnings("unchecked")
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) { wasWalking = false; wasRiding = false; return; }

            boolean wearing = NanoSuitHandler.isWearingFullNanoSuit(player);

            // Riding AVA?
            boolean isRiding = wearing && (player.getVehicle() instanceof AVAEntity);

            // Walking on ground with suit?
            boolean isWalking = wearing && !isRiding
                && player.onGround()
                && player.getDeltaMovement().horizontalDistance() > 0.02
                && !player.isSwimming() && !player.isFallFlying();

            var data = PlayerAnimationAccess.getPlayerAssociatedData(player);

            // ── Ride animation ─────────────────────────────────────────────
            if (isRiding != wasRiding) {
                wasRiding = isRiding;
                ModifierLayer<IAnimation> rideLayer =
                    (ModifierLayer<IAnimation>) data.get(RIDE_LAYER_ID);
                if (rideLayer != null) {
                    if (isRiding) {
                        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(RIDE_ANIM_ID);
                        if (anim != null) rideLayer.setAnimation(new KeyframeAnimationPlayer(anim));
                    } else {
                        rideLayer.setAnimation(null);
                    }
                }
                // Stop walk when riding starts
                if (isRiding) isWalking = false;
            }

            // ── Walk animation ─────────────────────────────────────────────
            if (isWalking != wasWalking) {
                wasWalking = isWalking;
                ModifierLayer<IAnimation> walkLayer =
                    (ModifierLayer<IAnimation>) data.get(WALK_LAYER_ID);
                if (walkLayer != null) {
                    if (isWalking) {
                        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(WALK_ANIM_ID);
                        if (anim != null) walkLayer.setAnimation(new KeyframeAnimationPlayer(anim));
                    } else {
                        walkLayer.setAnimation(null);
                    }
                }
            }
        }
    }
}