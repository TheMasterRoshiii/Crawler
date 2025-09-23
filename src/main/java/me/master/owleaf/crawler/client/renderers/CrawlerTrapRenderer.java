package me.master.owleaf.crawler.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.master.owleaf.crawler.blocks.CrawlerTrapBlockEntity;
import me.master.owleaf.crawler.client.models.CrawlerTrapGeoModel;
import me.master.owleaf.crawler.core.animation.SkinRenderer;
import me.master.owleaf.crawler.core.trap.CrawlerTrapEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CrawlerTrapRenderer extends GeoBlockRenderer<CrawlerTrapBlockEntity> {
    private final SkinRenderer skinRenderer;

    public CrawlerTrapRenderer(BlockEntityRendererProvider.Context context) {
        super(new CrawlerTrapGeoModel());
        this.skinRenderer = new SkinRenderer();
    }

    @Override
    public void actuallyRender(PoseStack poseStack,
                               CrawlerTrapBlockEntity animatable,
                               BakedGeoModel model,
                               RenderType renderType,
                               MultiBufferSource bufferSource,
                               VertexConsumer buffer,
                               boolean isReRender,
                               float partialTick,
                               int packedLight,
                               int packedOverlay,
                               float red,
                               float green,
                               float blue,
                               float alpha) {


        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource,
                buffer, isReRender, partialTick, packedLight, packedOverlay,
                red, green, blue, alpha);


        if (animatable.getTrapEntity().hasActiveCaptureData()) {
            renderCapturedPlayer(animatable, model, partialTick, poseStack,
                    bufferSource, packedLight);
        }
    }

    private void renderCapturedPlayer(CrawlerTrapBlockEntity blockEntity,
                                      BakedGeoModel trapModel,
                                      float partialTick,
                                      PoseStack poseStack,
                                      MultiBufferSource bufferSource,
                                      int packedLight) {

        CrawlerTrapEntity.CaptureData captureData =
                blockEntity.getTrapEntity().getActiveCaptureData();

        if (captureData != null) {
            skinRenderer.renderPlayerSkin(
                    captureData.getSkinData(),
                    trapModel,
                    poseStack,
                    bufferSource,
                    packedLight,
                    captureData.getEscapeProgress()
            ).join();
        }
    }

    @Override
    public void renderRecursively(PoseStack poseStack,
                                  CrawlerTrapBlockEntity animatable,
                                  GeoBone bone,
                                  RenderType renderType,
                                  MultiBufferSource bufferSource,
                                  VertexConsumer buffer,
                                  boolean isReRender,
                                  float partialTick,
                                  int packedLight,
                                  int packedOverlay,
                                  float red,
                                  float green,
                                  float blue,
                                  float alpha) {


        if (bone.getName().equals("player_capture_bone") &&
                animatable.getTrapEntity().hasActiveCaptureData()) {


            poseStack.pushPose();

            CrawlerTrapEntity.CaptureData captureData =
                    animatable.getTrapEntity().getActiveCaptureData();

            applyPlayerTrapEffects(poseStack, captureData.getEscapeProgress());

            super.renderRecursively(poseStack, animatable, bone, renderType,
                    bufferSource, buffer, isReRender, partialTick,
                    packedLight, packedOverlay, red, green, blue, alpha);

            poseStack.popPose();
        } else {
            // Standard bone rendering
            super.renderRecursively(poseStack, animatable, bone, renderType,
                    bufferSource, buffer, isReRender, partialTick,
                    packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

    private void applyPlayerTrapEffects(PoseStack poseStack, double escapeProgress) {
        double intensity = Math.max(0, (100 - escapeProgress) / 100.0);
        long time = System.currentTimeMillis();

        double shakeX = Math.sin(time * 0.01) * intensity * 0.05;
        double shakeY = Math.cos(time * 0.015) * intensity * 0.03;
        double shakeZ = Math.sin(time * 0.008) * intensity * 0.05;

        poseStack.translate(shakeX, shakeY, shakeZ);


        float scale = (float)(1.0 - intensity * 0.15);
        poseStack.scale(scale, Math.max(0.7f, scale), scale);


        float rotation = (float)(Math.sin(time * 0.02) * intensity * 10.0);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
    }
}
