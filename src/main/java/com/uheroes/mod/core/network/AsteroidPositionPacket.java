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

    public AsteroidPositionPacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static AsteroidPositionPacket decode(FriendlyByteBuf buf) {
        return new AsteroidPositionPacket(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                AsteroidImpactSequence.craterPos = new BlockPos(x, y, z);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
