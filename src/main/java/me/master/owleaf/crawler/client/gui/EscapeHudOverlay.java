package me.master.owleaf.crawler.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.master.owleaf.crawler.client.TrapClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class EscapeHudOverlay implements IGuiOverlay {
    private static final ResourceLocation NORMAL = new ResourceLocation("crawler", "textures/gui/0.png");
    private static final ResourceLocation PRESSED = new ResourceLocation("crawler", "textures/gui/1.png");

    private static final int SPRITE_WIDTH = 64;
    private static final int SPRITE_HEIGHT = 20;
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 6;

    private static int pressAnimationTicks = 0;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (!TrapClientHandler.isPlayerTrapped()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int centerX = screenWidth / 2;
        int hotbarY = screenHeight - 22;
        int overlayY = hotbarY - 50;

        float progress = TrapClientHandler.getEscapeProgress();

        renderEscapeBar(guiGraphics, centerX - BAR_WIDTH / 2, overlayY - 10, progress);
        renderSpaceSprite(guiGraphics, centerX - SPRITE_WIDTH / 2, overlayY);

        if (pressAnimationTicks > 0) {
            pressAnimationTicks--;
        }
    }

    private void renderEscapeBar(GuiGraphics guiGraphics, int x, int y, float progress) {
        int filledWidth = (int) (BAR_WIDTH * (progress / 100.0f));

        guiGraphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);
        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF442222);

        if (filledWidth > 0) {
            int color = getBarColor(progress);
            guiGraphics.fill(x, y, x + filledWidth, y + BAR_HEIGHT, color);
        }
    }

    private int getBarColor(float progress) {
        if (progress < 25.0f) return 0xFFAA2222;
        if (progress < 50.0f) return 0xFFAA6622;
        if (progress < 75.0f) return 0xFFAAAA22;
        return 0xFF22AA22;
    }

    private void renderSpaceSprite(GuiGraphics guiGraphics, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();

        ResourceLocation texture = pressAnimationTicks > 0 ? PRESSED : NORMAL;
        RenderSystem.setShaderTexture(0, texture);

        guiGraphics.blit(texture, x, y, 0, 0, SPRITE_WIDTH, SPRITE_HEIGHT, SPRITE_WIDTH, SPRITE_HEIGHT);

        RenderSystem.disableBlend();
    }

    public static void onSpacePressed() {
        pressAnimationTicks = 8;
    }
}
