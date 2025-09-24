package me.master.owleaf.crawler.client.models;

import me.master.owleaf.crawler.entities.CrawlerTrapEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CrawlerTrapEntityModel extends GeoModel<CrawlerTrapEntity> {

    @Override
    public ResourceLocation getModelResource(CrawlerTrapEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("crawler", "geo/crawl.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CrawlerTrapEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("crawler", "textures/entity/crawl.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CrawlerTrapEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("crawler", "animations/crawl.animation.json");
    }
}
