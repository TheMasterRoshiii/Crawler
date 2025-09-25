package me.master.owleaf.crawler.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import me.master.owleaf.crawler.entities.CrawlerTrapEntity;
import me.master.owleaf.crawler.client.models.CrawlerTrapEntityModel;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CrawlerTrapEntityRenderer extends GeoEntityRenderer<CrawlerTrapEntity> {

    public CrawlerTrapEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CrawlerTrapEntityModel());

    }

    @Override
    public void render(CrawlerTrapEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {

        if (!entity.shouldRender()) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(2.0f, 2.0f, 2.0f);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }

    @Override
    protected float getDeathMaxRotation(CrawlerTrapEntity entity) {
        return 0.0f;
    }
}
