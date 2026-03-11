package com.uheroes.mod.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
public class SaberFirstPersonHandler {

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) return;

        SaberAttackAnimation anim = SaberAttackAnimation.INSTANCE;
        if (!anim.isActive()) return;

        PoseStack ps = event.getPoseStack();

        float pitch = (float) Math.toRadians(anim.getFPPitch());
        float yaw   = (float) Math.toRadians(anim.getFPYaw());
        float roll  = (float) Math.toRadians(anim.getFPRoll());
        float tz    = anim.getFPTranslateZ();

        // Pivot around hand grip point, apply rotations, pivot back
        ps.translate(0.56f, -0.52f, -0.72f);
        ps.mulPose(new Quaternionf().rotationX(pitch));
        ps.mulPose(new Quaternionf().rotationY(yaw));
        ps.mulPose(new Quaternionf().rotationZ(roll));
        if (tz != 0f) ps.translate(0, 0, tz);
        ps.translate(-0.56f, 0.52f, 0.72f);
    }
}