package me.master.owleaf.crawler.core.trap;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class CrawlerTrapEntity implements GeoAnimatable {
    private final AnimatableInstanceCache cache;
    private final ScheduledExecutorService scheduler;
    private final Map<UUID, CaptureSession> activeSessions;

    private static final RawAnimation TRAP_ANIMATION =
            RawAnimation.begin().thenPlay("trap");
    private static final RawAnimation ESCAPE_ANIMATION =
            RawAnimation.begin().thenPlay("escape");
    private static final RawAnimation IDLE_ANIMATION =
            RawAnimation.begin().thenLoop("idle");

    public enum TrapState {
        IDLE, ACTIVATING, TRAPPING, RELEASING, KILLING
    }

    private volatile TrapState currentState = TrapState.IDLE;

    public CrawlerTrapEntity() {
        this.cache = GeckoLibUtil.createInstanceCache(this);
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "CrawlerTrap-Worker");
            t.setDaemon(true);
            return t;
        });
        this.activeSessions = new ConcurrentHashMap<>();
    }

    public synchronized boolean capturePlayer(ServerPlayer player) {
        if (currentState != TrapState.IDLE) return false;

        UUID playerId = player.getUUID();
        String skinData = extractPlayerSkin(player);

        CaptureSession session = new CaptureSession(playerId, skinData,
                System.currentTimeMillis());
        activeSessions.put(playerId, session);

        currentState = TrapState.ACTIVATING;

        restrictPlayerMovement(player);

        return true;
    }

    public synchronized boolean processSpaceInput(UUID playerId) {
        CaptureSession session = activeSessions.get(playerId);
        if (session == null || currentState != TrapState.TRAPPING) return false;

        session.addEscapeInput();
        return true;
    }

    public double getEscapeProgress(UUID playerId) {
        CaptureSession session = activeSessions.get(playerId);
        return session != null ? session.getEscapeProgress() : 0.0;
    }

    public boolean hasActiveCapture(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }

    public boolean hasActiveCaptureData() {
        return !activeSessions.isEmpty();
    }

    public CaptureData getActiveCaptureData() {
        return activeSessions.values().stream()
                .findFirst()
                .map(session -> new CaptureData(session.getSkinData(),
                        session.getEscapeProgress()))
                .orElse(null);
    }

    public synchronized void releasePlayer(UUID playerId) {
        activeSessions.remove(playerId);
        if (activeSessions.isEmpty()) {
            currentState = TrapState.IDLE;
        }
    }

    private String extractPlayerSkin(ServerPlayer player) {
        try {
            GameProfile profile = player.getGameProfile();
            Collection<Property> textures = profile.getProperties().get("textures");

            if (!textures.isEmpty()) {
                Property textureProperty = textures.iterator().next();
                return textureProperty.getValue();
            }
        } catch (Exception e) {
            // Log error but continue
        }
        return "default_steve";
    }

    private void restrictPlayerMovement(ServerPlayer player) {
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
    }

    public void updateState(TrapState newState) {
        this.currentState = newState;
    }

    public TrapState getCurrentState() {
        return currentState;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller",
                this::mainAnimationController));
    }

    private PlayState mainAnimationController(AnimationState<CrawlerTrapEntity> state) {
        return switch (currentState) {
            case ACTIVATING, TRAPPING -> state.setAndContinue(TRAP_ANIMATION);
            case RELEASING -> state.setAndContinue(ESCAPE_ANIMATION);
            case IDLE -> state.setAndContinue(IDLE_ANIMATION);
            default -> PlayState.STOP;
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void shutdown() {
        scheduler.shutdown();
        activeSessions.clear();
    }


    public static class CaptureSession {
        private final UUID playerId;
        private final String skinData;
        private final long startTime;
        private final AtomicInteger escapeInputs;

        public CaptureSession(UUID playerId, String skinData, long startTime) {
            this.playerId = playerId;
            this.skinData = skinData;
            this.startTime = startTime;
            this.escapeInputs = new AtomicInteger(0);
        }

        public void addEscapeInput() {
            escapeInputs.incrementAndGet();
        }

        public double getEscapeProgress() {
            return Math.min(100.0, (escapeInputs.get() / 20.0) * 100.0);
        }

        public UUID getPlayerId() { return playerId; }
        public String getSkinData() { return skinData; }
        public long getStartTime() { return startTime; }
    }

    public static class CaptureData {
        private final String skinData;
        private final double escapeProgress;

        public CaptureData(String skinData, double escapeProgress) {
            this.skinData = skinData;
            this.escapeProgress = escapeProgress;
        }

        public String getSkinData() { return skinData; }
        public double getEscapeProgress() { return escapeProgress; }
    }
}
