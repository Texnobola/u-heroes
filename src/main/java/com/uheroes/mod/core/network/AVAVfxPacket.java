package com.uheroes.mod.core.network;

import com.uheroes.mod.heroes.nanotech.ava.AVAEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * S2C: tells the client to spawn an AVA VFX effect at a given world position.
 */
public class AVAVfxPacket {

    public enum Type { BLOCK_DEFLECT, BLASTER_MUZZLE, BLASTER_HIT }

    private final Type  type;
    private final float x, y, z;
    private final float dirX, dirY, dirZ;

    public AVAVfxPacket(Type type, float x, float y, float z,
                        float dirX, float dirY, float dirZ) {
        this.type = type;
        this.x = x; this.y = y; this.z = z;
        this.dirX = dirX; this.dirY = dirY; this.dirZ = dirZ;
    }

    public static void encode(AVAVfxPacket pkt, FriendlyByteBuf buf) {
        buf.writeEnum(pkt.type);
        buf.writeFloat(pkt.x);    buf.writeFloat(pkt.y);    buf.writeFloat(pkt.z);
        buf.writeFloat(pkt.dirX); buf.writeFloat(pkt.dirY); buf.writeFloat(pkt.dirZ);
    }

    public static AVAVfxPacket decode(FriendlyByteBuf buf) {
        return new AVAVfxPacket(buf.readEnum(Type.class),
            buf.readFloat(), buf.readFloat(), buf.readFloat(),
            buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(AVAVfxPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Level level = Minecraft.getInstance().level;
                if (level == null) return;

                Vec3 pos = new Vec3(pkt.x, pkt.y, pkt.z);
                Vec3 dir = new Vec3(pkt.dirX, pkt.dirY, pkt.dirZ);

                switch (pkt.type) {
                    case BLOCK_DEFLECT ->
                        AVAEffects.spawnDeflect(level, pos, dir, 0.08f);

                    case BLASTER_MUZZLE ->
                        AVAEffects.spawnBlaster(level, pos, dir);

                    case BLASTER_HIT -> {
                        AVAEffects.spawnDeflect(level, pos, dir, 0.10f);
                    }
                }
            })
        );
        ctx.get().setPacketHandled(true);
    }
}