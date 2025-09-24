package me.master.owleaf.crawler.client;

import me.master.owleaf.crawler.client.gui.EscapeHudOverlay;
import me.master.owleaf.crawler.client.renderers.CrawlerTrapEntityRenderer;
import me.master.owleaf.crawler.entities.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "crawler", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CRAWLER_TRAP.get(), CrawlerTrapEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        EscapeHudOverlay overlay = new EscapeHudOverlay();
        TrapClientHandler.setHudOverlay(overlay);

        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "crawler_escape_hud", overlay);
    }
}
