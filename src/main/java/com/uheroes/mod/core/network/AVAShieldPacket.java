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
 * Sent CLIENT → SERVER on AVA shield key press/release.
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

                // Push state to the live AVA entity using UUID overload directly
                ava.getAvaUUID().ifPresent(id -> {
                    if (player.level() instanceof ServerLevel level) {
                        Entity entity = level.getEntities().get(id); // UUID overload — no lambda
                        if (entity instanceof AVAEntity avaEntity) {
                            avaEntity.setShieldActive(pkt.held);
                        }
                    }
                });
            });
        });
        ctx.get().setPacketHandled(true);
    }
}