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
 * - nano_ride: active when riding AVA (skate/surfboard pose)
 *
 * Uses the correct PlayerAnimationFactory API — registers once per player,
 * never duplicates layers.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NanoSuitWalkAnimHandler {

    private static final ResourceLocation RIDE_LAYER_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "nano_ride_layer");

    private static final ResourceLocation RIDE_ANIM_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.nano.ride");

    private static final ResourceLocation FLIGHT_LAYER_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "nano_flight_layer");
    private static final ResourceLocation FLIGHT_CHARGE_ANIM_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.nano.flight_charge");
    private static final ResourceLocation FLIGHT_CRUISE_ANIM_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.nano.flight_cruise");

    private static final ResourceLocation PUNCH_LAYER_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "nano_punch_layer");
    private static final ResourceLocation PUNCH_ANIM_ID =
        new ResourceLocation(UHeroesMod.MOD_ID, "animation.nano.power_punch");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Ride layer — priority 22 (above walk)
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
            RIDE_LAYER_ID, 22,
            (AbstractClientPlayer p) -> new ModifierLayer<>()
        );
        // Flight layer — priority 25, holds on last frame during flight
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
            FLIGHT_LAYER_ID, 25,
            (AbstractClientPlayer p) -> new ModifierLayer<>()
        );
        // Punch layer — priority 30, plays once on power punch
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
            PUNCH_LAYER_ID, 30,
            (AbstractClientPlayer p) -> new ModifierLayer<>()
        );
        UHeroesMod.LOGGER.debug("[U-Heroes] Walk/ride animation layers registered");
    }

    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
    public static class TickHandler {

        private static boolean wasRiding  = false;

        @SubscribeEvent
        @SuppressWarnings("unchecked")
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) { wasRiding = false; return; }

            boolean wearing = NanoSuitHandler.isWearingFullNanoSuit(player);

            // Riding AVA?
            boolean isRiding = wearing && (player.getVehicle() instanceof AVAEntity);

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
            }

            // ── Flight animation ───────────────────────────────────────────────
            com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler.isWearingNanoChestplate(player);
            var flightData2 = PlayerAnimationAccess.getPlayerAssociatedData(player);
            ModifierLayer<IAnimation> flightLayer =
                (ModifierLayer<IAnimation>) flightData2.get(FLIGHT_LAYER_ID);
            if (flightLayer != null) {
                boolean spaceHeld = com.uheroes.mod.init.ModKeybinds.JETPACK.isDown();
                boolean hasChest  = com.uheroes.mod.heroes.nanotech.armor.NanoSuitHandler
                    .isWearingNanoChestplate(player);
                com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.FlightPhase phase;
                if (!hasChest) {
                    phase = com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.FlightPhase.IDLE;
                } else if (spaceHeld && player.onGround()) {
                    phase = com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.FlightPhase.CHARGING;
                } else if (!player.onGround() && spaceHeld) {
                    phase = com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.FlightPhase.CRUISING;
                } else if (!player.onGround() && !spaceHeld) {
                    phase = com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.FlightPhase.GLIDING;
                } else {
                    phase = com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.FlightPhase.IDLE;
                }
                if (phase != lastFlightPhase) {
                    lastFlightPhase = phase;
                    switch (phase) {
                        case CHARGING -> {
                            KeyframeAnimation a = PlayerAnimationRegistry.getAnimation(FLIGHT_CHARGE_ANIM_ID);
                            if (a != null) flightLayer.setAnimation(new KeyframeAnimationPlayer(a));
                        }
                        case THRUSTING, CRUISING, GLIDING -> {
                            KeyframeAnimation a = PlayerAnimationRegistry.getAnimation(FLIGHT_CRUISE_ANIM_ID);
                            if (a != null) flightLayer.setAnimation(new KeyframeAnimationPlayer(a));
                        }
                        case IDLE -> flightLayer.setAnimation(null);
                    }
                }
            }

            // ── Punch animation cleanup ────────────────────────────────────────
            // Clear the layer once the animation finishes so the player isn't frozen
            var punchData = PlayerAnimationAccess.getPlayerAssociatedData(player);
            ModifierLayer<IAnimation> punchLayer =
                (ModifierLayer<IAnimation>) punchData.get(PUNCH_LAYER_ID);
            if (punchLayer != null) {
                IAnimation current = punchLayer.getAnimation();
                if (current != null && !current.isActive()) {
                    punchLayer.setAnimation(null);
                }
            }
                }
    }

    /** Call from BoosterHandler client-side when power punch fires. */
    @SuppressWarnings("unchecked")
    public static void triggerPunchAnim(net.minecraft.client.player.LocalPlayer player) {
        var data = dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess.getPlayerAssociatedData(player);
        dev.kosmx.playerAnim.api.layered.ModifierLayer<dev.kosmx.playerAnim.api.layered.IAnimation> layer =
            (dev.kosmx.playerAnim.api.layered.ModifierLayer<dev.kosmx.playerAnim.api.layered.IAnimation>) data.get(PUNCH_LAYER_ID);
        if (layer == null) return;
        dev.kosmx.playerAnim.core.data.KeyframeAnimation anim =
            dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry.getAnimation(PUNCH_ANIM_ID);
        if (anim == null) return;
        layer.setAnimation(new dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer(anim));
    }

    // Track last set flight anim to avoid redundant setAnimation calls
    private static com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.FlightPhase lastFlightPhase =
        com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.FlightPhase.IDLE;

    private static boolean isAnimKey(IAnimation anim, ResourceLocation id) {
        return anim != null; // simplified — we track by phase
    }
}