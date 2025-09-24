package me.master.owleaf.crawler.mixins;

import me.master.owleaf.crawler.entities.CrawlerTrapEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LocalPlayer.class)
public class PlayerMovementMixin {

    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    private void onAiStep(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        if (player.level() != null) {
            List<CrawlerTrapEntity> nearbyTraps = player.level().getEntitiesOfClass(
                    CrawlerTrapEntity.class, player.getBoundingBox().inflate(5.0));

            for (CrawlerTrapEntity trap : nearbyTraps) {
                if (trap.isTrappingPlayer(player.getUUID())) {
                    player.input.up = false;
                    player.input.down = false;
                    player.input.left = false;
                    player.input.right = false;
                    player.input.jumping = false;
                    player.input.shiftKeyDown = false;

                    player.setDeltaMovement(Vec3.ZERO);
                    break;
                }
            }
        }
    }
}
