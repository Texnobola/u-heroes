package com.uheroes.mod.core.network;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S2C: jetpack VFX — charge buildup, thrust blast, cruise trail. */
public class JetpackVfxPacket {

    public enum Type { CHARGE, THRUST, CRUISE }

    private final Type  type;
    private final float x, y, z;
    private final float intensity; // 0..1

    public JetpackVfxPacket(Type type, float x, float y, float z, float intensity) {
        this.type = type; this.x = x; this.y = y; this.z = z; this.intensity = intensity;
    }

    public static void encode(JetpackVfxPacket p, FriendlyByteBuf buf) {
        buf.writeEnum(p.type);
        buf.writeFloat(p.x); buf.writeFloat(p.y); buf.writeFloat(p.z);
        buf.writeFloat(p.intensity);
    }

    public static JetpackVfxPacket decode(FriendlyByteBuf buf) {
        return new JetpackVfxPacket(buf.readEnum(Type.class),
            buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(JetpackVfxPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Level level = Minecraft.getInstance().level;
                if (level == null) return;

                // Offset slightly behind the player (back of chestplate)
                float bx = pkt.x, by = pkt.y + 1.2f, bz = pkt.z;

                switch (pkt.type) {
                    case CHARGE -> {
                        // Expanding ring that builds in size with intensity
                        AAALevel.addParticle(level, false,
                            new ParticleEmitterInfo(new ResourceLocation("u_heroes", "thruster_charge"))
                                .position(bx, by, bz)
                                .scale(0.04f + pkt.intensity * 0.08f));
                    }
                    case THRUST -> {
                        // Large blast ring + downward ring
                        AAALevel.addParticle(level, false,
                            new ParticleEmitterInfo(new ResourceLocation("u_heroes", "thruster_ring"))
                                .position(bx, by - 0.5f, bz)
                                .scale(0.15f));
                        AAALevel.addParticle(level, false,
                            new ParticleEmitterInfo(new ResourceLocation("u_heroes", "thruster_ring"))
                                .position(bx, by - 1.0f, bz)
                                .scale(0.10f));
                    }
                    case CRUISE -> {
                        // Continuous trail behind the player
                        AAALevel.addParticle(level, false,
                            new ParticleEmitterInfo(new ResourceLocation("u_heroes", "thruster_trail"))
                                .position(bx, by - 0.8f, bz)
                                .scale(0.05f));
                    }
                }
            })
        );
        ctx.get().setPacketHandled(true);
    }
}