package me.master.owleaf.crawler.core.animation;

import net.minecraft.client.player.AbstractClientPlayer;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PlayerAnimatable implements GeoAnimatable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final AbstractClientPlayer player;
    private static final RawAnimation TRAP_ANIM = RawAnimation.begin().thenLoop("trap");
    private static final RawAnimation SCAPE_ANIM = RawAnimation.begin().thenPlay("scape");
    public boolean isEscaping = false;

    public PlayerAnimatable(AbstractClientPlayer player) { this.player = player; }
    public AbstractClientPlayer getPlayer() { return this.player; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "playerController", 0, state -> {
            state.getController().setAnimation(isEscaping ? SCAPE_ANIM : TRAP_ANIM);
            return PlayState.CONTINUE;
        }));
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public double getTick(Object object) {
        return 0;
    }
}