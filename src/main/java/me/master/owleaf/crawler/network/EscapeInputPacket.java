package me.master.owleaf.crawler.network;

import me.master.owleaf.crawler.blocks.CrawlerTrapBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

public class EscapeInputPacket {
    private final UUID playerId;
    private final long timestamp;

    public EscapeInputPacket(UUID playerId) {
        this.playerId = playerId;
        this.timestamp = System.currentTimeMillis();
    }

    public EscapeInputPacket(UUID playerId, long timestamp) {
        this.playerId = playerId;
        this.timestamp = timestamp;
    }

    public static void encode(EscapeInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerId);
        buffer.writeLong(packet.timestamp);
    }

    public static EscapeInputPacket decode(FriendlyByteBuf buffer) {
        return new EscapeInputPacket(
                buffer.readUUID(),
                buffer.readLong()
        );
    }

    public static void handle(EscapeInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getUUID().equals(packet.playerId)) {

                long latency = System.currentTimeMillis() - packet.timestamp;
                if (latency > 500) return;

                BlockPos trapPos = findNearestCrawlerTrap(player);
                if (trapPos != null) {
                    BlockEntity blockEntity = player.serverLevel().getBlockEntity(trapPos);
                    if (blockEntity instanceof CrawlerTrapBlockEntity trapEntity) {
                        trapEntity.procesarInputEscape(player);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static BlockPos findNearestCrawlerTrap(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();

        for (int x = -4; x <= 4; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockEntity be = player.serverLevel().getBlockEntity(checkPos);
                    if (be instanceof CrawlerTrapBlockEntity) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }
}
