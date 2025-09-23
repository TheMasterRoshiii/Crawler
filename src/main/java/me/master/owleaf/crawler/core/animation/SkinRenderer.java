package me.master.owleaf.crawler.core.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import java.util.concurrent.CompletableFuture;

public class SkinRenderer {

    public CompletableFuture<Void> renderPlayerSkin(String skinData,
                                                    BakedGeoModel model,
                                                    PoseStack poseStack,
                                                    MultiBufferSource bufferSource,
                                                    int packedLight,
                                                    double escapeProgress) {


        return CompletableFuture.completedFuture(null);
    }
}
