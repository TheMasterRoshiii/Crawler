package me.master.owleaf.crawler.events;

import me.master.owleaf.crawler.blocks.CrawlerTrapBlockEntity;
import me.master.owleaf.crawler.core.CrawlerMod;
import me.master.owleaf.crawler.network.EscapeInputPacket;
import me.master.owleaf.crawler.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrapEventHandler {

    private static final Map<UUID, Long> lastInputTime = new ConcurrentHashMap<>();
    private static final long INPUT_COOLDOWN = 50L; // 50ms cooldown between inputs

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            lastInputTime.remove(serverPlayer.getUUID());
            findAndDeactivatePlayerTraps(serverPlayer);

            CrawlerMod.LOGGER.debug("Player {} logged out, cleared trap sessions",
                    serverPlayer.getName().getString());
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        // Check if player is in a trap and apply movement restrictions
        BlockPos nearestTrap = findNearestCrawlerTrap(serverPlayer);
        if (nearestTrap != null) {
            BlockEntity blockEntity = serverPlayer.level().getBlockEntity(nearestTrap);
            if (blockEntity instanceof CrawlerTrapBlockEntity trapEntity) {
                if (trapEntity.getTrapEntity().hasActiveCapture(serverPlayer.getUUID())) {
                    restrictPlayerMovement(serverPlayer, nearestTrap);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity instanceof CrawlerTrapBlockEntity trapEntity) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                boolean handled = trapEntity.handlePlayerInteraction(serverPlayer);
                if (handled) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity instanceof CrawlerTrapBlockEntity trapEntity) {
            trapEntity.getTrapEntity().shutdown();
            CrawlerMod.LOGGER.debug("Crawler trap at {} was broken, released all captures",
                    event.getPos());
        }
    }

    private void findAndDeactivatePlayerTraps(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();


        for (int x = -5; x <= 5; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockEntity blockEntity = player.level().getBlockEntity(checkPos);

                    if (blockEntity instanceof CrawlerTrapBlockEntity trapEntity) {
                        if (trapEntity.getTrapEntity().hasActiveCapture(player.getUUID())) {
                            trapEntity.getTrapEntity().releasePlayer(player.getUUID());
                            CrawlerMod.LOGGER.debug("Released player {} from trap at {}",
                                    player.getName().getString(), checkPos);
                        }
                    }
                }
            }
        }
    }

    private BlockPos findNearestCrawlerTrap(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();


        for (int x = -4; x <= 4; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockEntity blockEntity = player.level().getBlockEntity(checkPos);

                    if (blockEntity instanceof CrawlerTrapBlockEntity) {
                        return checkPos;
                    }
                }
            }
        }
        return null;
    }

    private void restrictPlayerMovement(ServerPlayer player, BlockPos trapPos) {
        double centerX = trapPos.getX() + 0.5;
        double centerZ = trapPos.getZ() + 0.5;
        double maxDistance = 1.2;

        double deltaX = player.getX() - centerX;
        double deltaZ = player.getZ() - centerZ;
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (distance > maxDistance) {
            double pullFactor = 0.8;
            double newX = centerX + (deltaX / distance) * maxDistance * pullFactor;
            double newZ = centerZ + (deltaZ / distance) * maxDistance * pullFactor;

            player.teleportTo(newX, player.getY(), newZ);
            player.setDeltaMovement(player.getDeltaMovement().multiply(0.1, 0.5, 0.1));
            player.hurtMarked = true;
        }
    }


    @Mod.EventBusSubscriber(modid = CrawlerMod.MODID, value = Dist.CLIENT)
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

                    CrawlerMod.LOGGER.debug("Space key pressed, sent escape input for player {}",
                            player.getName().getString());
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


    @Mod.EventBusSubscriber(modid = CrawlerMod.MODID, value = Dist.DEDICATED_SERVER)
    public static class ServerEvents {

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;


            if (event.getServer().getTickCount() % 1200 == 0) {
                lastInputTime.clear();
                CrawlerMod.LOGGER.debug("Cleared input timing cache");
            }
        }
    }
}
