package com.uheroes.mod.core.network;

import com.uheroes.mod.heroes.nanotech.ava.AVACapability;
import com.uheroes.mod.heroes.nanotech.ava.AVAEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C→S: sent when the AVA shield key (R) is pressed or released.
 *
 * <p>On press: AVA enters intercept mode — slides to the orbit angle
 * that interposes her between the player and the nearest threat.
 *
 * <p>On release: AVA resumes normal orbit.
 */
public class AVAShieldPacket {

    private final boolean held;

    public AVAShieldPacket(boolean held) { this.held = held; }

    public static void encode(AVAShieldPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.held);
    }

    public static AVAShieldPacket decode(FriendlyByteBuf buf) {
        return new AVAShieldPacket(buf.readBoolean());
    }

    public static void handle(AVAShieldPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(AVACapability.INSTANCE).ifPresent(ava -> {
                ava.setShieldHeld(pkt.held);

                ava.getAvaUUID().ifPresent(id -> {
                    if (!(player.level() instanceof ServerLevel level)) return;
                    Entity entity = level.getEntities().get(id);
                    if (entity instanceof AVAEntity avaEntity) {
                        avaEntity.setShieldActive(pkt.held);
                        // R press → AVA moves to face the threat direction
                        avaEntity.setInterceptMode(pkt.held, player);
                    }
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }
}