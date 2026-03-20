package com.uheroes.mod.core.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

/**
 * S2C: server sends the current scan results to the client.
 * Each entry: entityId, healthPct (0–1), distanceBlocks, name
 */
public class ScannerDataPacket {

    public static class TargetInfo {
        public final int    entityId;
        public final float  healthPct;
        public final float  distance;
        public final String name;

        public TargetInfo(int entityId, float healthPct, float distance, String name) {
            this.entityId  = entityId;
            this.healthPct = healthPct;
            this.distance  = distance;
            this.name      = name;
        }
    }

    private final List<TargetInfo> targets;

    public ScannerDataPacket(List<TargetInfo> targets) { this.targets = targets; }

    public static void encode(ScannerDataPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.targets.size());
        for (TargetInfo t : pkt.targets) {
            buf.writeInt(t.entityId);
            buf.writeFloat(t.healthPct);
            buf.writeFloat(t.distance);
            buf.writeUtf(t.name, 64);
        }
    }

    public static ScannerDataPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<TargetInfo> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int    id    = buf.readInt();
            float  hp    = buf.readFloat();
            float  dist  = buf.readFloat();
            String name  = buf.readUtf(64);
            list.add(new TargetInfo(id, hp, dist, name));
        }
        return new ScannerDataPacket(list);
    }

    public static void handle(ScannerDataPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.uheroes.mod.client.hud.ScannerHUD.updateTargets(pkt.targets)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}