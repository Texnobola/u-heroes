package com.uheroes.mod.core.network;

import com.uheroes.mod.heroes.nanotech.ability.ScannerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** C2S: player pressed or released the scanner key. */
public class ScannerPacket {

    private final boolean active;

    public ScannerPacket(boolean active) { this.active = active; }

    public static void encode(ScannerPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.active);
    }

    public static ScannerPacket decode(FriendlyByteBuf buf) {
        return new ScannerPacket(buf.readBoolean());
    }

    public static void handle(ScannerPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (pkt.active) {
                ScannerHandler.tickScan(player);
                // Build and send results back to client
                sendScanResults(player);
            } else {
                ScannerHandler.clearScan(player);
                // Send empty list to clear the HUD
                com.uheroes.mod.core.network.ModNetwork.sendToPlayer(
                    new com.uheroes.mod.core.network.ScannerDataPacket(
                        java.util.Collections.emptyList()), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void sendScanResults(ServerPlayer player) {
        java.util.Set<Integer> marked = ScannerHandler.getMarked(player);
        if (marked.isEmpty()) return;

        java.util.List<com.uheroes.mod.core.network.ScannerDataPacket.TargetInfo> list =
            new java.util.ArrayList<>();

        for (int id : marked) {
            net.minecraft.world.entity.Entity e = player.level().getEntity(id);
            if (!(e instanceof net.minecraft.world.entity.LivingEntity le)) continue;
            float hp   = le.getHealth() / le.getMaxHealth();
            float dist = (float) player.distanceTo(le);
            String name = le.getDisplayName().getString();
            list.add(new com.uheroes.mod.core.network.ScannerDataPacket.TargetInfo(
                id, hp, dist, name));
        }
        com.uheroes.mod.core.network.ModNetwork.sendToPlayer(
            new com.uheroes.mod.core.network.ScannerDataPacket(list), player);
    }
}