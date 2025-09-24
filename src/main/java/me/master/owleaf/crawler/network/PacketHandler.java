package me.master.owleaf.crawler.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("crawler", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void init() {
        INSTANCE.messageBuilder(EscapeInputPacket.class, packetId++)
                .encoder(EscapeInputPacket::encode)
                .decoder(EscapeInputPacket::decode)
                .consumerMainThread(EscapeInputPacket::handle)
                .add();

        INSTANCE.messageBuilder(TrapSyncPacket.class, packetId++)
                .encoder(TrapSyncPacket::encode)
                .decoder(TrapSyncPacket::decode)
                .consumerMainThread(TrapSyncPacket::handle)
                .add();

        INSTANCE.messageBuilder(TrapStatePacket.class, packetId++)
                .encoder(TrapStatePacket::encode)
                .decoder(TrapStatePacket::decode)
                .consumerMainThread(TrapStatePacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
