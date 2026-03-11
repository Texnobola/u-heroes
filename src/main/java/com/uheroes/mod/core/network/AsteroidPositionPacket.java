package com.uheroes.mod.core.network;

import com.uheroes.mod.origin.AsteroidImpactSequence;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AsteroidPositionPacket {
    private final int x;
    private final int y;
    private final int z;

    public AsteroidPositionPacket(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public static void encode(AsteroidPositionPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.x);
        buf.writeInt(packet.y);
        buf.writeInt(packet.z);
    }

    public static AsteroidPositionPacket decode(FriendlyByteBuf buf) {
        return new AsteroidPositionPacket(new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));
    }

    public static void handle(AsteroidPositionPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                AsteroidImpactSequence.craterPos = new BlockPos(packet.x, packet.y, packet.z);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}