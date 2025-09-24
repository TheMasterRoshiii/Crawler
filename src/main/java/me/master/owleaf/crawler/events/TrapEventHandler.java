package me.master.owleaf.crawler.events;

import me.master.owleaf.crawler.entities.CrawlerTrapEntity;
import me.master.owleaf.crawler.core.CrawlerMod;
import me.master.owleaf.crawler.network.EscapeInputPacket;
import me.master.owleaf.crawler.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrapEventHandler {

    private static final Map<UUID, Long> lastInputTime = new ConcurrentHashMap<>();
    private static final long INPUT_COOLDOWN = 50L;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            lastInputTime.remove(serverPlayer.getUUID());
            findAndDeactivatePlayerTraps(serverPlayer);
        }
    }

    private void findAndDeactivatePlayerTraps(ServerPlayer player) {
        List<CrawlerTrapEntity> nearbyTraps = player.serverLevel().getEntitiesOfClass(
                CrawlerTrapEntity.class, player.getBoundingBox().inflate(10.0));

        for (CrawlerTrapEntity trap : nearbyTraps) {
            if (trap.hasTrappedPlayer()) {
                trap.discard();
            }
        }
    }

    @Mod.EventBusSubscriber(modid = "crawler", value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onKeyInput(InputEvent.Key event) {
            if (event.getKey() == GLFW.GLFW_KEY_SPACE &&
                    event.getAction() == GLFW.GLFW_PRESS) {

                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    UUID playerId = player.getUUID();
                    long currentTime = System.currentTimeMillis();

                    Long lastInput = lastInputTime.get(playerId);
                    if (lastInput != null && (currentTime - lastInput) < INPUT_COOLDOWN) {
                        return;
                    }

                    lastInputTime.put(playerId, currentTime);
                    PacketHandler.sendToServer(new EscapeInputPacket(playerId));
                }
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            if (player.tickCount % 100 == 0) {
                long currentTime = System.currentTimeMillis();
                lastInputTime.entrySet().removeIf(entry ->
                        (currentTime - entry.getValue()) > 5000);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = "crawler", value = Dist.DEDICATED_SERVER)
    public static class ServerEvents {

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            if (event.getServer().getTickCount() % 1200 == 0) {
                lastInputTime.clear();
            }
        }
    }
}
