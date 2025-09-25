package me.master.owleaf.crawler.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.master.owleaf.crawler.client.TrapClientHandler;
import me.master.owleaf.crawler.client.renderers.PlayerAnimationRenderer;
import me.master.owleaf.crawler.core.CrawlerMod;
import me.master.owleaf.crawler.core.animation.PlayerAnimatable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CrawlerMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    private static final PlayerAnimationRenderer playerAnimationRenderer = new PlayerAnimationRenderer();

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (event.getEntity() != Minecraft.getInstance().player) return;

        PlayerAnimatable animatable = TrapClientHandler.getAnimatable();
        if (TrapClientHandler.isPlayerTrapped() && animatable != null) {
            event.setCanceled(true);

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            double playerX = Mth.lerp(event.getPartialTick(), event.getEntity().xo, event.getEntity().getX());
            double playerY = Mth.lerp(event.getPartialTick(), event.getEntity().yo, event.getEntity().getY());
            double playerZ = Mth.lerp(event.getPartialTick(), event.getEntity().zo, event.getEntity().getZ());

            double trapX = TrapClientHandler.getTrapX();
            double trapY = TrapClientHandler.getTrapY();
            double trapZ = TrapClientHandler.getTrapZ();


            poseStack.translate(trapX - playerX, trapY - playerY, trapZ - playerZ);

            event.getEntity().setInvisible(true);

            RenderType renderType = playerAnimationRenderer.getRenderType(animatable,
                    playerAnimationRenderer.getTextureLocation(animatable),
                    event.getMultiBufferSource(),
                    event.getPartialTick());

            VertexConsumer vertexConsumer = event.getMultiBufferSource().getBuffer(renderType);

            playerAnimationRenderer.render(
                    poseStack,
                    animatable,
                    event.getMultiBufferSource(),
                    renderType,
                    vertexConsumer,
                    event.getPackedLight()
            );

            event.getEntity().setInvisible(false);

            poseStack.popPose();
        }
    }
}