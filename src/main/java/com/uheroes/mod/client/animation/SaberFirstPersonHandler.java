package com.uheroes.mod.client.animation;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Applies SaberAttackAnimation to the FIRST-PERSON hand via RenderHandEvent.
 * PlayerAnimator's IAnimation system only affects the third-person model,
 * so first-person must be handled separately via PoseStack injection.
 */
@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class SaberFirstPersonHandler {

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        // Only animate main hand
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) return;
        if (!SaberAttackAnimation.INSTANCE.isActive()) return;

        // Inject our transform into the hand's PoseStack
        event.getPoseStack().pushPose();
        SaberAttackAnimation.INSTANCE.applyFirstPersonTransform(event.getPoseStack());
        // Note: we don't popPose() here — Forge applies the stack for the full render
        // If this causes issues, pop and re-render manually.
    }
}