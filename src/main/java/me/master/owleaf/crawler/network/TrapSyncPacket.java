package me.master.owleaf.crawler.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TrapSyncPacket {
    private final BlockPos blockPos;
    private final boolean isActive;
    private final UUID playerId;

    public TrapSyncPacket(BlockPos blockPos, boolean isActive, UUID playerId) {
        this.blockPos = blockPos;
        this.isActive = isActive;
        this.playerId = playerId;
    }

    public static void encode(TrapSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeBoolean(packet.isActive);
        buffer.writeUUID(packet.playerId);
    }

    public static TrapSyncPacket decode(FriendlyByteBuf buffer) {
        return new TrapSyncPacket(
                buffer.readBlockPos(),
                buffer.readBoolean(),
                buffer.readUUID()
        );
    }

    public static void handle(TrapSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            handleClientSide(packet);
        });
        context.setPacketHandled(true);
    }

    private static void handleClientSide(TrapSyncPacket packet) {

    }

    public BlockPos getBlockPos() { return blockPos; }
    public boolean isActive() { return isActive; }
    public UUID getPlayerId() { return playerId; }
}
