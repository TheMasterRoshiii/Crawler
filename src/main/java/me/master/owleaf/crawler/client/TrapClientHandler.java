package me.master.owleaf.crawler.client;

import me.master.owleaf.crawler.client.gui.EscapeHudOverlay;

public class TrapClientHandler {
    private static boolean isTrapped = false;
    private static float escapeProgress = 0.0f;
    private static EscapeHudOverlay hudOverlay = null;

    public static void setTrapped(boolean trapped) {
        isTrapped = trapped;
        if (!trapped) {
            escapeProgress = 0.0f;
        }
    }

    public static boolean isPlayerTrapped() {
        return isTrapped;
    }

    public static void setEscapeProgress(float progress) {
        escapeProgress = progress;
    }

    public static float getEscapeProgress() {
        return escapeProgress;
    }

    public static void onSpacePressed() {
        if (hudOverlay != null) {
            hudOverlay.onSpacePressed();
        }
    }

    public static void setHudOverlay(EscapeHudOverlay overlay) {
        hudOverlay = overlay;
    }

    public static void clearTrapState() {
        isTrapped = false;
        escapeProgress = 0.0f;
    }
}
