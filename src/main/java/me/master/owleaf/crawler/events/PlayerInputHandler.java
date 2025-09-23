package me.master.owleaf.crawler.events;

import me.master.owleaf.crawler.core.CrawlerMod;
import me.master.owleaf.crawler.network.EscapeInputPacket;
import me.master.owleaf.crawler.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = CrawlerMod.MODID, value = Dist.CLIENT)
public class PlayerInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getKey() == GLFW.GLFW_KEY_SPACE &&
                event.getAction() == GLFW.GLFW_PRESS) {

            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PacketHandler.sendToServer(new EscapeInputPacket(player.getUUID()));
            }
        }
    }
}
