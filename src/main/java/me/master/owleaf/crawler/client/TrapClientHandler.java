package me.master.owleaf.crawler.client;

import me.master.owleaf.crawler.client.gui.EscapeHudOverlay;
import me.master.owleaf.crawler.core.animation.PlayerAnimatable;
import me.master.owleaf.crawler.network.TrapStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class TrapClientHandler {
    private static boolean isTrapped = false;
    private static float escapeProgress = 0.0f;
    private static EscapeHudOverlay hudOverlay = null;
    private static PlayerAnimatable animatable = null;
    private static double trapX, trapY, trapZ;

    public static void handlePacket(TrapStatePacket packet) {
        isTrapped = packet.isTrapped();
        escapeProgress = packet.getEscapeProgress();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (isTrapped) {
            trapX = packet.getTrapX();
            trapY = packet.getTrapY();
            trapZ = packet.getTrapZ();
            if (animatable == null) animatable = new PlayerAnimatable(player);
            animatable.isEscaping = false;
        } else {
            if (animatable != null) {
                animatable.isEscaping = true;
                new Thread(() -> {
                    try {
                        Thread.sleep(2750);
                        Minecraft.getInstance().execute(() -> {
                            animatable = null;
                            isTrapped = false;
                        });
                    } catch (InterruptedException e) {}
                }).start();
            } else {
                isTrapped = false;
            }
        }
    }

    public static boolean isPlayerTrapped() { return isTrapped; }
    public static PlayerAnimatable getAnimatable() { return animatable; }
    public static void setEscapeProgress(float progress) { escapeProgress = progress; }
    public static float getEscapeProgress() { return escapeProgress; }

    public static double getTrapX() { return trapX; }
    public static double getTrapY() { return trapY; }
    public static double getTrapZ() { return trapZ; }

    public static void onSpacePressed() {
        if (hudOverlay != null) hudOverlay.onSpacePressed();
    }
    public static void setHudOverlay(EscapeHudOverlay overlay) { hudOverlay = overlay; }
    public static void clearTrapState() {
        isTrapped = false;
        escapeProgress = 0.0f;
        animatable = null;
    }
}