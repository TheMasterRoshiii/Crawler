package me.master.owleaf.crawler.mixins;

import me.master.owleaf.crawler.client.TrapClientHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin extends Input {

    @Shadow @Final private Options options;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(boolean slowDown, float discountFactor, CallbackInfo ci) {
        if (TrapClientHandler.isPlayerTrapped()) {
            boolean spacePressed = this.options.keyJump.isDown();

            if (spacePressed) {
                TrapClientHandler.onSpacePressed();
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickEnd(boolean slowDown, float discountFactor, CallbackInfo ci) {
        if (TrapClientHandler.isPlayerTrapped()) {
            this.up = false;
            this.down = false;
            this.left = false;
            this.right = false;
            this.jumping = false;
            this.shiftKeyDown = false;

            this.forwardImpulse = 0.0f;
            this.leftImpulse = 0.0f;
        }
    }
}
