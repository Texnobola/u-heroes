package com.uheroes.mod.core.network;

import com.uheroes.mod.client.ScreenShakeHandler;
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

/** S2C: triggers seismic slam shockwave VFX + screen shake on all nearby clients. */
public class SeismicSlamVfxPacket {

    private final float x, y, z;

    public SeismicSlamVfxPacket(float x, float y, float z) {
        this.x = x; this.y = y; this.z = z;
    }

    public static void encode(SeismicSlamVfxPacket pkt, FriendlyByteBuf buf) {
        buf.writeFloat(pkt.x); buf.writeFloat(pkt.y); buf.writeFloat(pkt.z);
    }

    public static SeismicSlamVfxPacket decode(FriendlyByteBuf buf) {
        return new SeismicSlamVfxPacket(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(SeismicSlamVfxPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Level level = Minecraft.getInstance().level;
                if (level == null) return;

                // Shockwave ring — large scale, flat on the ground
                AAALevel.addParticle(level, false,
                    new ParticleEmitterInfo(new ResourceLocation("u_heroes", "seismic_shockwave"))
                        .position(pkt.x, pkt.y, pkt.z)
                        .rotation(0f, 0f, 0f)
                        .scale(0.5f));

                // Second smaller inner ring
                AAALevel.addParticle(level, false,
                    new ParticleEmitterInfo(new ResourceLocation("u_heroes", "seismic_shockwave"))
                        .position(pkt.x, pkt.y + 0.1f, pkt.z)
                        .rotation(0f, 0f, 0f)
                        .scale(0.25f));

                // Screen shake
                ScreenShakeHandler.shakeIntensity = 2.5f;
                ScreenShakeHandler.shakeTicks = 12;
                ScreenShakeHandler.totalShakeTicks = 12;
            })
        );
        ctx.get().setPacketHandled(true);
    }
}