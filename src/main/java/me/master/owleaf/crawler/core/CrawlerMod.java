package me.master.owleaf.crawler.core;

import me.master.owleaf.crawler.blocks.ModBlocks;
import me.master.owleaf.crawler.client.ClientSetup;
import me.master.owleaf.crawler.entities.ModBlockEntities;
import me.master.owleaf.crawler.events.TrapEventHandler;
import me.master.owleaf.crawler.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CrawlerMod.MODID)
public class CrawlerMod {
    public static final String MODID = "crawler";
    public static final Logger LOGGER = LogManager.getLogger();

    public CrawlerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        forgeEventBus.register(new TrapEventHandler());

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(ClientSetup::onClientSetup);
        });

        LOGGER.info("Crawler trap mod initialized - prepare for capture!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
            LOGGER.info("Crawler mod common setup completed");
        });
    }
}
