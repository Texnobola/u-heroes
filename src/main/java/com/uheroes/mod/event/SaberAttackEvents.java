package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.animation.SaberAttackAnimHandler;
import com.uheroes.mod.client.hud.FluxMeterHUD;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import com.uheroes.mod.init.ModSounds;
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

    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            Player player = event.getEntity();
            if (player.getMainHandItem().getItem() instanceof LaserSwordItem) {
                player.level().playSound(null,
                    player.blockPosition(),
                    ModSounds.LASER_SWORD_HIT.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0f, 1.0f);
            }
            spawnSlash(player, false);
        }
    }

    @Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
            Player player = event.getEntity();
            if (player.getMainHandItem().getItem() instanceof LaserSwordItem) {
                player.level().playSound(player,
                    player.blockPosition(),
                    ModSounds.LASER_SWORD_SWING.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.8f, 1.0f);
            }
            spawnSlash(event.getEntity(), true);
        }
    }

    static void spawnSlash(Player player, boolean isClientSide) {
        if (player.level().isClientSide() != isClientSide) return;
        if (isClientSide) SaberAttackAnimHandler.onSwing();
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) return;

        Vec3 look    = player.getLookAngle();
        Vec3 eyePos  = player.getEyePosition();

        // Sword tip: 1.2 blocks forward from eye, 0.45 right, 0.1 down
        // Use look + right vector so it always tracks where the sword points
        float yawRad = (float) Math.toRadians(player.getYRot());
        // right = perpendicular to look on the horizontal plane
        Vec3 right = new Vec3(
             Math.cos(yawRad),
             0,
             Math.sin(yawRad)
        ).normalize();

        Vec3 swordTip = eyePos
            .add(look.scale(1.2))           // 1.2 blocks ahead along look
            .add(right.scale(0.45))         // 0.45 right (sword hand side)
            .subtract(0, 0.1, 0);           // slightly lower than eye

        // Rotation: yaw from player facing, pitch from look angle
        float yaw   = (float) Math.toRadians(player.getYRot());
        float pitch = (float) Math.toRadians(player.getXRot());

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
                .position(swordTip.x, swordTip.y, swordTip.z)
                .rotation(pitch, yaw, 0)
                .scale(scale)
        );
    }
}