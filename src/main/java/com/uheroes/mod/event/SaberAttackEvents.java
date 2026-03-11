package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.animation.SaberAttackAnimHandler;
import com.uheroes.mod.client.hud.FluxMeterHUD;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class SaberAttackEvents {

    private static final ParticleEmitterInfo SABER_SLASH = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "saber_slash")
    );

    private static final double REACH = 1.5;
    private static final double RIGHT_OFFSET = 0.6;
    private static final double HEIGHT_OFFSET = 1.1;

    // Fires server-side — hitting an entity
    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            spawnSlash(event.getEntity(), false);
        }
    }

    // Fires client-side only — swing in air
    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
            spawnSlash(event.getEntity(), true);
        }
    }

    static void spawnSlash(Player player, boolean isClientSide) {
        if (player.level().isClientSide() != isClientSide) return;
        if (isClientSide) SaberAttackAnimHandler.onSwing();
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) return;

        float yawRad   = (float) Math.toRadians(player.getYRot());
        float pitchRad = (float) Math.toRadians(player.getXRot());

        Vec3 forward = player.getLookAngle();
        Vec3 right = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad));

        Vec3 origin = player.position()
            .add(0, HEIGHT_OFFSET, 0)
            .add(forward.scale(REACH))
            .add(right.scale(RIGHT_OFFSET));

        int currentCombo = FluxMeterHUD.comboIndex;
        float scale = switch (currentCombo) {
            case 7 -> 1.2f;
            case 8 -> 1.6f;
            case 9 -> 2.2f;
            default -> 1.0f;
        };

        AAALevel.addParticle(
            player.level(), false,
            SABER_SLASH.clone()
                .position(origin.x, origin.y, origin.z)
                .rotation(pitchRad, (float) Math.toRadians(player.getYRot()), 0)
                .scale(scale)
        );
    }
}