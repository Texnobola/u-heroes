package com.uheroes.mod.core.network;

import com.uheroes.mod.origin.AsteroidImpactSequence;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TriggerImpactSequencePacket {

    public TriggerImpactSequencePacket() {}

    public static void encode(TriggerImpactSequencePacket packet, FriendlyByteBuf buf) {
        // Empty packet
    }

    public static TriggerImpactSequencePacket decode(FriendlyByteBuf buf) {
        return new TriggerImpactSequencePacket();
    }

    public static void handle(TriggerImpactSequencePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                AsteroidImpactSequence.start();
            });
        });
        ctx.get().setPacketHandled(true);
    }
}