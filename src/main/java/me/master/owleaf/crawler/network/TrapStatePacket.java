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
    private final double trapX;
    private final double trapY;
    private final double trapZ;

    public TrapStatePacket(boolean isTrapped, float escapeProgress, double trapX, double trapY, double trapZ) {
        this.isTrapped = isTrapped;
        this.escapeProgress = escapeProgress;
        this.trapX = trapX;
        this.trapY = trapY;
        this.trapZ = trapZ;
    }

    public static void encode(TrapStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isTrapped);
        buffer.writeFloat(packet.escapeProgress);
        buffer.writeDouble(packet.trapX);
        buffer.writeDouble(packet.trapY);
        buffer.writeDouble(packet.trapZ);
    }

    public static TrapStatePacket decode(FriendlyByteBuf buffer) {
        return new TrapStatePacket(buffer.readBoolean(), buffer.readFloat(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(TrapStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TrapClientHandler.handlePacket(packet));
        });
        context.setPacketHandled(true);
    }

    public boolean isTrapped() {
        return isTrapped;
    }

    public float getEscapeProgress() {
        return escapeProgress;
    }

    public double getTrapX() {
        return trapX;
    }

    public double getTrapY() {
        return trapY;
    }

    public double getTrapZ() {
        return trapZ;
    }
}