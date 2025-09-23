package me.master.owleaf.crawler.entities;

import me.master.owleaf.crawler.blocks.CrawlerTrapBlockEntity;
import me.master.owleaf.crawler.blocks.ModBlocks;
import me.master.owleaf.crawler.core.CrawlerMod;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CrawlerMod.MODID);

    public static final RegistryObject<BlockEntityType<CrawlerTrapBlockEntity>> CRAWLER_TRAP =
            BLOCK_ENTITIES.register("crawler_trap", () ->
                    BlockEntityType.Builder.of(CrawlerTrapBlockEntity::new,
                            ModBlocks.CRAWLER_TRAP.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
