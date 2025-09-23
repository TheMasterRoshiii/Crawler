package me.master.owleaf.crawler.blocks;

import me.master.owleaf.crawler.core.trap.CrawlerTrapEntity;
import me.master.owleaf.crawler.entities.ModBlockEntities;
import me.master.owleaf.crawler.network.PacketHandler;
import me.master.owleaf.crawler.network.TrapSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CrawlerTrapBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final CrawlerTrapEntity trapEntity;
    private final ScheduledExecutorService scheduler;

    private boolean isActive = false;
    private int tickCounter = 0;

    public CrawlerTrapBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRAWLER_TRAP.get(), pos, state);

        this.trapEntity = new ConcreteCrawlerTrapEntity();

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CrawlerTrap-" + pos.toShortString());
            t.setDaemon(true);
            return t;
        });
    }

    private class ConcreteCrawlerTrapEntity extends CrawlerTrapEntity {

        public void inicializarComportamientoTrampa() {
        }

        public boolean validarCondicionesTrampa() {
            return level != null && !level.isClientSide && !isRemoved();
        }

        public void alActivarse() {
        }

        public void alDesactivarse() {
        }

        public double getTick(Object blockEntity) {
            return RenderUtils.getCurrentTick();
        }
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;

        if (tickCounter % 10 == 0 && !isActive) {
            detectarJugadoresCercanos();
        }

        if (isActive) {
            actualizarEstadoTrampa();
        }
    }

    private void detectarJugadoresCercanos() {
        AABB areaDeteccion = new AABB(worldPosition)
                .inflate(3.0, 2.0, 3.0);

        List<ServerPlayer> jugadoresCercanos = level.getEntitiesOfClass(
                ServerPlayer.class, areaDeteccion);

        jugadoresCercanos.stream()
                .filter(this::esObjetivoValido)
                .findFirst()
                .ifPresent(this::activarTrampa);
    }

    private boolean esObjetivoValido(ServerPlayer jugador) {
        return !jugador.isCreative() &&
                !jugador.isSpectator() &&
                jugador.isAlive() &&
                jugador.getY() >= worldPosition.getY() - 1 &&
                jugador.getY() <= worldPosition.getY() + 3;
    }

    private void activarTrampa(ServerPlayer jugador) {
        if (trapEntity.capturePlayer(jugador)) {
            isActive = true;

            restringirJugador(jugador);
            trapEntity.updateState(CrawlerTrapEntity.TrapState.TRAPPING);

            sincronizarConCliente();

            iniciarSecuenciaEscape(jugador);
        }
    }

    private void restringirJugador(ServerPlayer jugador) {
        double centroX = worldPosition.getX() + 0.5;
        double centroY = worldPosition.getY() + 1.5;
        double centroZ = worldPosition.getZ() + 0.5;

        jugador.teleportTo(centroX, centroY, centroZ);
        jugador.setDeltaMovement(Vec3.ZERO);
        jugador.hurtMarked = true;

        PacketHandler.sendToPlayer(
                new TrapSyncPacket(worldPosition, true, jugador.getUUID()),
                jugador);
    }

    private void iniciarSecuenciaEscape(ServerPlayer jugador) {
        scheduler.schedule(() -> {
            if (isActive && trapEntity.hasActiveCapture(jugador.getUUID())) {
                double progresoEscape = trapEntity.getEscapeProgress(jugador.getUUID());

                if (progresoEscape >= 100.0) {
                    liberarJugador(jugador);
                } else {
                    eliminarJugador(jugador);
                }
            }
        }, 10, TimeUnit.SECONDS);
    }

    private void liberarJugador(ServerPlayer jugador) {
        isActive = false;
        trapEntity.releasePlayer(jugador.getUUID());
        trapEntity.updateState(CrawlerTrapEntity.TrapState.RELEASING);

        jugador.teleportTo(jugador.getX(), jugador.getY() + 2, jugador.getZ());

        PacketHandler.sendToPlayer(
                new TrapSyncPacket(worldPosition, false, jugador.getUUID()),
                jugador);

        sincronizarConCliente();

        scheduler.schedule(() -> {
            trapEntity.updateState(CrawlerTrapEntity.TrapState.IDLE);
        }, 2, TimeUnit.SECONDS);
    }

    private void eliminarJugador(ServerPlayer jugador) {
        isActive = false;
        trapEntity.releasePlayer(jugador.getUUID());
        trapEntity.updateState(CrawlerTrapEntity.TrapState.KILLING);

        jugador.hurt(jugador.damageSources().generic(), Float.MAX_VALUE);

        PacketHandler.sendToPlayer(
                new TrapSyncPacket(worldPosition, false, jugador.getUUID()),
                jugador);

        sincronizarConCliente();

        scheduler.schedule(() -> {
            trapEntity.updateState(CrawlerTrapEntity.TrapState.IDLE);
        }, 2, TimeUnit.SECONDS);
    }

    private void actualizarEstadoTrampa() {
    }

    public boolean handlePlayerInteraction(ServerPlayer jugador) {
        if (isActive) {
            if (trapEntity.hasActiveCapture(jugador.getUUID())) {
                double progreso = trapEntity.getEscapeProgress(jugador.getUUID());
                jugador.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Progreso de escape: " + String.format("%.1f", progreso) + "%"
                ));
                return true;
            }
        } else {
            jugador.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Trampa Crawler - Estado: Inactiva"
            ));

            if (esObjetivoValido(jugador)) {
                activarTrampa(jugador);
                return true;
            }
        }

        return false;
    }

    public boolean procesarInputEscape(Player jugador) {
        if (!isActive) return false;

        boolean resultado = trapEntity.processSpaceInput(jugador.getUUID());
        if (resultado) {
            sincronizarConCliente();
        }
        return resultado;
    }

    private void sincronizarConCliente() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
        }
    }

    public CrawlerTrapEntity getTrapEntity() {
        return trapEntity;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        trapEntity.registerControllers(controllers);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("isActive", isActive);
        tag.putInt("tickCounter", tickCounter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        isActive = tag.getBoolean("isActive");
        tickCounter = tag.getInt("tickCounter");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        trapEntity.shutdown();
    }
}
