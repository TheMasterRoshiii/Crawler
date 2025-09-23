package me.master.owleaf.crawler.network;

import me.master.owleaf.crawler.core.CrawlerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CrawlerMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static int nextId() {
        return id++;
    }

    public static void register() {
        INSTANCE.messageBuilder(EscapeInputPacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(EscapeInputPacket::decode)
                .encoder(EscapeInputPacket::encode)
                .consumerMainThread(EscapeInputPacket::handle)
                .add();

        INSTANCE.messageBuilder(TrapSyncPacket.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TrapSyncPacket::decode)
                .encoder(TrapSyncPacket::encode)
                .consumerMainThread(TrapSyncPacket::handle)
                .add();
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAllPlayers(Object packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
}
