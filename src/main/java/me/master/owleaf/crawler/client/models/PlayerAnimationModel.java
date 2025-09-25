package me.master.owleaf.crawler.client.models;

import me.master.owleaf.crawler.core.CrawlerMod;
import me.master.owleaf.crawler.core.animation.PlayerAnimatable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlayerAnimationModel extends GeoModel<PlayerAnimatable> {

    private static final ResourceLocation CLASSIC_MODEL = new ResourceLocation(CrawlerMod.MODID, "geo/crawl_player_classic.geo.json");
    private static final ResourceLocation SLIM_MODEL = new ResourceLocation(CrawlerMod.MODID, "geo/crawl_player_slim.geo.json");
    private static final ResourceLocation ANIMATION_RESOURCE = new ResourceLocation(CrawlerMod.MODID, "animations/crawl_player.animation.json");

    @Override
    public ResourceLocation getModelResource(PlayerAnimatable animatable) {


        PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(animatable.getPlayer().getUUID());


        if (playerInfo != null && "slim".equals(playerInfo.getModelName())) {
            return SLIM_MODEL;
        }
        return CLASSIC_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PlayerAnimatable animatable) {
        return animatable.getPlayer().getSkinTextureLocation();
    }

    @Override
    public ResourceLocation getAnimationResource(PlayerAnimatable animatable) {
        return ANIMATION_RESOURCE;
    }
}