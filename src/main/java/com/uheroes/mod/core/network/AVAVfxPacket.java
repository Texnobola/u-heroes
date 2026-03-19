package com.uheroes.mod.core.network;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C: tells the client to spawn an AVA VFX effect at a given world position.
 * Used for block-deflect and blaster-hit effects that are triggered server-side.
 */
public class AVAVfxPacket {

    public enum Type { BLOCK_DEFLECT, BLASTER_MUZZLE, BLASTER_HIT }

    private final Type  type;
    private final float x, y, z;
    private final float dirX, dirY, dirZ; // normalized hit direction

    public AVAVfxPacket(Type type, float x, float y, float z, float dirX, float dirY, float dirZ) {
        this.type = type;
        this.x = x; this.y = y; this.z = z;
        this.dirX = dirX; this.dirY = dirY; this.dirZ = dirZ;
    }

    public static void encode(AVAVfxPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.type);
        buf.writeFloat(pkt.x);  buf.writeFloat(pkt.y);  buf.writeFloat(pkt.z);
        buf.writeFloat(pkt.dirX); buf.writeFloat(pkt.dirY); buf.writeFloat(pkt.dirZ);
    }

    public static AVAVfxPacket decode(FriendlyByteBuf buf) {
        return new AVAVfxPacket(buf.readEnum(Type.class),
            buf.readFloat(), buf.readFloat(), buf.readFloat(),
            buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(AVAVfxPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handle(pkt)));
        ctx.get().setPacketHandled(true);
    }

    // Client-only handler in inner class to avoid classloading on server
    private static class ClientHandler {
        static void handle(AVAVfxPacket pkt) {
            var level = Minecraft.getInstance().level;
            if (level == null) return;

            float yaw   = (float) Math.atan2(pkt.dirX, pkt.dirZ);
            float pitch = (float) -Math.asin(Math.max(-1, Math.min(1, pkt.dirY)));

            switch (pkt.type) {
                case BLOCK_DEFLECT -> {
                    // Primary deflect ring
                    AAALevel.addParticle(level, false,
                        new ParticleEmitterInfo(new ResourceLocation("u_heroes", "ava_block_deflect"))
                            .position(pkt.x, pkt.y, pkt.z)
                            .rotation(pitch, yaw, 0f)
                            .scale(1.5f));
                    // Scatter rings
                    for (int i = 1; i <= 3; i++) {
                        float roll = (float) Math.toRadians(i * 45.0);
                        AAALevel.addParticle(level, false,
                            new ParticleEmitterInfo(new ResourceLocation("u_heroes", "ava_block_deflect"))
                                .position(pkt.x, pkt.y, pkt.z)
                                .rotation(pitch, yaw, roll)
                                .scale(0.8f));
                    }
                }
                case BLASTER_MUZZLE -> {
                    // Small bright ring at muzzle
                    AAALevel.addParticle(level, false,
                        new ParticleEmitterInfo(new ResourceLocation("u_heroes", "ava_block_deflect"))
                            .position(pkt.x, pkt.y, pkt.z)
                            .rotation(pitch, yaw, 0f)
                            .scale(0.6f));
                }
                case BLASTER_HIT -> {
                    // Impact burst
                    AAALevel.addParticle(level, false,
                        new ParticleEmitterInfo(new ResourceLocation("u_heroes", "ava_shield_pulse"))
                            .position(pkt.x, pkt.y, pkt.z)
                            .rotation(0, 0, 0)
                            .scale(0.3f));
                    AAALevel.addParticle(level, false,
                        new ParticleEmitterInfo(new ResourceLocation("u_heroes", "ava_block_deflect"))
                            .position(pkt.x, pkt.y, pkt.z)
                            .rotation(pitch, yaw, 0f)
                            .scale(0.9f));
                }
            }
        }
    }
}