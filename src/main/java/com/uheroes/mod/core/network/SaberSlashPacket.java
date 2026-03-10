package com.uheroes.mod.core.network;

import mod.chloeprime.aaaparticles.api.client.effekseer.AAALevel;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitterInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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
        return new SaberSlashPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(SaberSlashPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;
            AAALevel.addParticle(
                mc.level, false,
                new ParticleEmitterInfo(new ResourceLocation("u_heroes", "effeks/saber_slash"))
                    .position(msg.x, msg.y, msg.z)
                    .rotation(0, msg.yRot, 0)
                    .scale(msg.scale)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
