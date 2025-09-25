package me.master.owleaf.crawler.client.models;

import me.master.owleaf.crawler.entities.CrawlerTrapEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.model.GeoModel;

public class CrawlerTrapEntityModel extends GeoModel<CrawlerTrapEntity> {

    @Override
    public ResourceLocation getModelResource(CrawlerTrapEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("crawler", "geo/crawl.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CrawlerTrapEntity animatable) {
        // USA LA SKIN DEL JUGADOR SIEMPRE
        if (animatable.hasTrappedPlayer()) {
            Player trappedPlayer = getTrappedPlayer(animatable);
            if (trappedPlayer != null) {
                return getPlayerSkin(trappedPlayer);
            }
        }

        // Fallback
        return ResourceLocation.fromNamespaceAndPath("crawler", "textures/entity/crawl.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CrawlerTrapEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath("crawler", "animations/crawl.animation.json");
    }

    private Player getTrappedPlayer(CrawlerTrapEntity trap) {
        return trap.level().players().stream()
                .filter(player -> trap.isTrappingPlayer(player.getUUID()))
                .findFirst()
                .orElse(null);
    }

    private ResourceLocation getPlayerSkin(Player player) {
        if (player instanceof AbstractClientPlayer clientPlayer) {
            return clientPlayer.getSkinTextureLocation();
        } else {
            return DefaultPlayerSkin.getDefaultSkin(player.getUUID());
        }
    }
}
