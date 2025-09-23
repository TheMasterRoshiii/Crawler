package me.master.owleaf.crawler.blocks;

import me.master.owleaf.crawler.core.CrawlerMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CrawlerMod.MODID);

    public static final RegistryObject<Block> CRAWLER_TRAP = BLOCKS.register(
            "crawler_trap",
            () -> new CrawlerTrapBlock(BlockBehaviour.Properties
                    .of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 15.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion())
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
