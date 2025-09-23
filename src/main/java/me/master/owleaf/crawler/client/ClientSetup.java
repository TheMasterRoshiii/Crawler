package me.master.owleaf.crawler.client;

import me.master.owleaf.crawler.client.renderers.CrawlerTrapRenderer;
import me.master.owleaf.crawler.entities.ModBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(
                    ModBlockEntities.CRAWLER_TRAP.get(),
                    CrawlerTrapRenderer::new
            );
        });
    }
}
