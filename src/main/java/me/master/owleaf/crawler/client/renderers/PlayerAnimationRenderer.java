package me.master.owleaf.crawler.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.master.owleaf.crawler.client.models.PlayerAnimationModel;
import me.master.owleaf.crawler.core.animation.PlayerAnimatable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

public class PlayerAnimationRenderer extends GeoObjectRenderer<PlayerAnimatable> {
    public PlayerAnimationRenderer() {
        super(new PlayerAnimationModel());
    }
}