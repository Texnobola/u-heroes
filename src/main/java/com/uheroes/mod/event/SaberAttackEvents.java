package com.uheroes.mod.event;

import com.uheroes.mod.UHeroesMod;
import com.uheroes.mod.client.hud.FluxMeterHUD;
import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.core.network.SaberSlashPacket;
import com.uheroes.mod.heroes.nanotech.weapon.LaserSwordItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = UHeroesMod.MOD_ID)
public class SaberAttackEvents {
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        
        if (player.level().isClientSide()) {
            return;
        }
        
        if (!(player.getMainHandItem().getItem() instanceof LaserSwordItem)) {
            return;
        }
        
        int currentCombo = FluxMeterHUD.comboIndex;
        
        float scale = switch (currentCombo) {
            case 7 -> 1.2f;
            case 8 -> 1.6f;
            case 9 -> 2.2f;
            default -> 1.0f;
        };
        
        ModNetwork.sendToPlayer(
            new SaberSlashPacket(
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                player.getYRot(),
                scale
            ),
            (ServerPlayer) player
        );
    }
}
