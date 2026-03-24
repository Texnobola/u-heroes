package com.uheroes.mod.core.network;

import com.uheroes.mod.UHeroesMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel CHANNEL;
    private static int packetId = 0;

    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(UHeroesMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
        );

        CHANNEL.messageBuilder(FluxSyncPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
            .encoder(FluxSyncPacket::encode)
            .decoder(FluxSyncPacket::decode)
            .consumerMainThread(FluxSyncPacket::handle)
            .add();

        CHANNEL.messageBuilder(AVAShieldPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .encoder(AVAShieldPacket::encode)
            .decoder(AVAShieldPacket::decode)
            .consumerMainThread(AVAShieldPacket::handle)
            .add();

        CHANNEL.messageBuilder(AVAVfxPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
            .encoder(AVAVfxPacket::encode)
            .decoder(AVAVfxPacket::decode)
            .consumerMainThread(AVAVfxPacket::handle)
            .add();

        CHANNEL.messageBuilder(ScannerPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .encoder(ScannerPacket::encode)
            .decoder(ScannerPacket::decode)
            .consumerMainThread(ScannerPacket::handle)
            .add();

        CHANNEL.messageBuilder(JetpackVfxPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
            .encoder(JetpackVfxPacket::encode)
            .decoder(JetpackVfxPacket::decode)
            .consumerMainThread(JetpackVfxPacket::handle)
            .add();

        CHANNEL.messageBuilder(SeismicSlamVfxPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
            .encoder(SeismicSlamVfxPacket::encode)
            .decoder(SeismicSlamVfxPacket::decode)
            .consumerMainThread(SeismicSlamVfxPacket::handle)
            .add();

        CHANNEL.messageBuilder(ScannerDataPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
            .encoder(ScannerDataPacket::encode)
            .decoder(ScannerDataPacket::decode)
            .consumerMainThread(ScannerDataPacket::handle)
            .add();

        CHANNEL.messageBuilder(BoosterPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
            .encoder(BoosterPacket::encode)
            .decoder(BoosterPacket::decode)
            .consumerMainThread(BoosterPacket::handle)
            .add();
    }

    private static int nextId() { return packetId++; }

    public static <MSG> void sendToPlayer(MSG packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static <MSG> void sendToServer(MSG packet) {
        CHANNEL.sendToServer(packet);
    }

    public static <MSG> void sendToAllTracking(MSG packet, Entity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }
}