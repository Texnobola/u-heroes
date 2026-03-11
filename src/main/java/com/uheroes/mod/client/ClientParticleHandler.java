package com.uheroes.mod.client;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class ClientParticleHandler {

    private static final ParticleEmitterInfo SABER_SLASH = new ParticleEmitterInfo(
        new ResourceLocation("u_heroes", "effeks/saber_slash")
    );

    public static void spawnSaberSlash(double x, double y, double z, float yRot, float scale) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        AAALevel.addParticle(
            mc.level, false,
            SABER_SLASH.clone()
                .position(x, y, z)
                .rotation(0, yRot, 0)
                .scale(scale)
        );
    }
}
