package com.uheroes.mod.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent CLIENT → SERVER for booster ability triggers and jetpack state changes.
 */
public class BoosterPacket {

    public enum Action { DASH, POWER_PUNCH, JETPACK_ON, JETPACK_OFF }

    private final Action action;

    public BoosterPacket(Action action) { this.action = action; }

    public static void encode(BoosterPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.action);
    }

    public static BoosterPacket decode(FriendlyByteBuf buf) {
        return new BoosterPacket(buf.readEnum(Action.class));
    }

    public static void handle(BoosterPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            switch (pkt.action) {
                case DASH        -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.triggerDash(player);
                case POWER_PUNCH -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.triggerPowerPunch(player);
                case JETPACK_ON  -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.setJetpackActive(player.getUUID(), true);
                case JETPACK_OFF -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.setJetpackActive(player.getUUID(), false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}