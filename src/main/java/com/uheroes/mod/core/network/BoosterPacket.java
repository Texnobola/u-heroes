package com.uheroes.mod.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent CLIENT → SERVER when the player presses a booster keybind.
 *
 * <p>The server validates Flux availability and applies the movement/damage
 * impulse authoritatively.
 */
public class BoosterPacket {

    public enum Action { DASH, POWER_PUNCH }

    private final Action action;

    public BoosterPacket(Action action) { this.action = action; }

    // ─── Codec ────────────────────────────────────────────────────────────────

    public static void encode(BoosterPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.action);
    }

    public static BoosterPacket decode(FriendlyByteBuf buf) {
        return new BoosterPacket(buf.readEnum(Action.class));
    }

    // ─── Handler ──────────────────────────────────────────────────────────────

    public static void handle(BoosterPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            switch (pkt.action) {
                case DASH        -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.triggerDash(player);
                case POWER_PUNCH -> com.uheroes.mod.heroes.nanotech.ability.BoosterHandler.triggerPowerPunch(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}