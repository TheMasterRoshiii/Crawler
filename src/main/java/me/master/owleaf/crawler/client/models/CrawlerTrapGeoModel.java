package me.master.owleaf.crawler.client.models;

import me.master.owleaf.crawler.blocks.CrawlerTrapBlockEntity;
import me.master.owleaf.crawler.core.CrawlerMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

public class CrawlerTrapGeoModel extends DefaultedBlockGeoModel<CrawlerTrapBlockEntity> {

    public CrawlerTrapGeoModel() {
        super(ResourceLocation.fromNamespaceAndPath(CrawlerMod.MODID, "crawl"));
    }

    @Override
    public ResourceLocation getModelResource(CrawlerTrapBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CrawlerMod.MODID, "geo/crawl.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CrawlerTrapBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CrawlerMod.MODID, "textures/block/crawl.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CrawlerTrapBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CrawlerMod.MODID, "animations/crawl.animation.json");
    }
}
