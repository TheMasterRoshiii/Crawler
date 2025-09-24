package me.master.owleaf.crawler.network;

import me.master.owleaf.crawler.client.TrapClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TrapStatePacket {
    private final boolean isTrapped;
    private final float escapeProgress;

    public TrapStatePacket(boolean isTrapped) {
        this.isTrapped = isTrapped;
        this.escapeProgress = 0.0f;
    }

    public TrapStatePacket(boolean isTrapped, float escapeProgress) {
        this.isTrapped = isTrapped;
        this.escapeProgress = escapeProgress;
    }

    public static void encode(TrapStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isTrapped);
        buffer.writeFloat(packet.escapeProgress);
    }

    public static TrapStatePacket decode(FriendlyByteBuf buffer) {
        return new TrapStatePacket(buffer.readBoolean(), buffer.readFloat());
    }

    public static void handle(TrapStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                TrapClientHandler.setTrapped(packet.isTrapped);
                TrapClientHandler.setEscapeProgress(packet.escapeProgress);
            });
        });
        context.setPacketHandled(true);
    }
}
