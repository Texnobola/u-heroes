package com.uheroes.mod.core.network;

import com.uheroes.mod.client.hud.ClientFluxData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet to sync Neural Flux data from server to client.
 */
public class FluxSyncPacket {
    private final int currentFlux;
    private final int maxFlux;
    private final boolean overcharged;
    
    public FluxSyncPacket(int currentFlux, int maxFlux, boolean overcharged) {
        this.currentFlux = currentFlux;
        this.maxFlux = maxFlux;
        this.overcharged = overcharged;
    }
    
    public static void encode(FluxSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.currentFlux);
        buf.writeInt(packet.maxFlux);
        buf.writeBoolean(packet.overcharged);
    }
    
    public static FluxSyncPacket decode(FriendlyByteBuf buf) {
        return new FluxSyncPacket(buf.readInt(), buf.readInt(), buf.readBoolean());
    }
    
    public static void handle(FluxSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientFluxData.update(packet.currentFlux, packet.maxFlux, packet.overcharged);
        });
        ctx.get().setPacketHandled(true);
    }
}
