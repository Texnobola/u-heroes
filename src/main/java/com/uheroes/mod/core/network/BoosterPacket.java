package com.uheroes.mod.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BoosterPacket {

    public enum Action { DASH, POWER_PUNCH, JETPACK_ON, JETPACK_OFF, AVA_RESIZE, SEISMIC_SLAM }

    private final Action action;
    public BoosterPacket(Action action) { this.action = action; }

    public static void encode(BoosterPacket pkt, FriendlyByteBuf buf) { buf.writeEnum(pkt.action); }
    public static BoosterPacket decode(FriendlyByteBuf buf) { return new BoosterPacket(buf.readEnum(Action.class)); }

    public static void handle(BoosterPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            var bh = com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.class;
            switch (pkt.action) {
                case DASH        -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.triggerDash(player);
                case POWER_PUNCH -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.triggerPowerPunch(player);
                case JETPACK_ON  -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.setJetpackActive(player.getUUID(), true);
                case JETPACK_OFF -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.setJetpackActive(player.getUUID(), false);
                case AVA_RESIZE   -> com.uheroes.mod.heroes.nanotech.ava.AVAEntity.cycleSize(player);
                case SEISMIC_SLAM -> com.uheroes.mod.heroes.nanotech.ability.SeismicSlamHandler.triggerSlam(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}