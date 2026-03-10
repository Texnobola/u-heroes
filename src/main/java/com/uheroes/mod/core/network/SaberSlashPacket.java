package com.uheroes.mod.core.network;

import com.uheroes.mod.client.ClientParticleHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaberSlashPacket {
    private final double x, y, z;
    private final float yRot, scale;

    public SaberSlashPacket(double x, double y, double z, float yRot, float scale) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yRot = yRot;
        this.scale = scale;
    }

    public static void encode(SaberSlashPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.yRot);
        buf.writeFloat(msg.scale);
    }

    public static SaberSlashPacket decode(FriendlyByteBuf buf) {
        return new SaberSlashPacket(
            buf.readDouble(), buf.readDouble(), buf.readDouble(),
            buf.readFloat(), buf.readFloat()
        );
    }

    public static void handle(SaberSlashPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ClientParticleHandler.spawnSaberSlash(msg.x, msg.y, msg.z, msg.yRot, msg.scale)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}
