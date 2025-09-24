package me.master.owleaf.crawler.entities;

import me.master.owleaf.crawler.network.PacketHandler;
import me.master.owleaf.crawler.network.TrapStatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlerTrapEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Byte> TRAP_STATE = SynchedEntityData.defineId(CrawlerTrapEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> IS_VISIBLE = SynchedEntityData.defineId(CrawlerTrapEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> ESCAPE_PROGRESS = SynchedEntityData.defineId(CrawlerTrapEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private ServerPlayer trappedPlayer = null;
    private double originalMaxHealth = 0;
    private final AtomicInteger escapeInputs = new AtomicInteger(0);
    public int trapStartTick = 0;
    private int ticksSinceLastDetection = 0;
    private int ticksSinceLastDamage = 0;

    private static final int DETECTION_INTERVAL = 5;
    private static final int DAMAGE_INTERVAL = 20;
    private static final int ESCAPE_COOLDOWN = 300;
    private static final double DETECTION_RANGE = 6.0;
    private static final int REQUIRED_ESCAPE_INPUTS = 40;
    private static final float DAMAGE_AMOUNT = 3.0f;

    public enum TrapState {
        IDLE(0), ACTIVATING(1), TRAPPING(2), ESCAPING(3), COOLDOWN(4);

        private final byte id;
        TrapState(int id) { this.id = (byte) id; }

        public static TrapState fromId(byte id) {
            for (TrapState state : values()) {
                if (state.id == id) return state;
            }
            return IDLE;
        }
    }

    public CrawlerTrapEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TRAP_STATE, TrapState.IDLE.id);
        this.entityData.define(IS_VISIBLE, false);
        this.entityData.define(ESCAPE_PROGRESS, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            serverTick();
        }
    }

    private void serverTick() {
        TrapState currentState = getCurrentState();

        switch (currentState) {
            case IDLE -> {
                if (++ticksSinceLastDetection >= DETECTION_INTERVAL) {
                    ticksSinceLastDetection = 0;
                    detectNearbyPlayers();
                }
            }
            case ACTIVATING -> {
                if (tickCount - trapStartTick > 5) {
                    setTrapState(TrapState.TRAPPING);
                    if (trappedPlayer != null) {
                        trappedPlayer.hurt(trappedPlayer.damageSources().generic(), DAMAGE_AMOUNT);
                    }
                }
            }
            case TRAPPING -> {
                if (trappedPlayer != null) {
                    if (++ticksSinceLastDamage >= DAMAGE_INTERVAL) {
                        ticksSinceLastDamage = 0;
                        dealTrapDamage();
                    }

                    forcePlayerPosition();
                    updateEscapeProgress();

                    if (!trappedPlayer.isAlive()) {
                        disappear();
                    }
                }
            }
            case ESCAPING -> {
                if (tickCount - trapStartTick > 55) {
                    disappear();
                }
            }
            case COOLDOWN -> {
                if (tickCount - trapStartTick > ESCAPE_COOLDOWN) {
                    reset();
                }
            }
        }
    }

    private void detectNearbyPlayers() {
        AABB detectionBox = getBoundingBox().inflate(DETECTION_RANGE);
        List<ServerPlayer> nearbyPlayers = level().getEntitiesOfClass(ServerPlayer.class, detectionBox);

        for (ServerPlayer player : nearbyPlayers) {
            if (isValidTarget(player)) {
                activateTrap(player);
                break;
            }
        }
    }

    private boolean isValidTarget(ServerPlayer player) {
        return !player.isCreative() &&
                !player.isSpectator() &&
                player.isAlive() &&
                distanceToSqr(player) <= DETECTION_RANGE * DETECTION_RANGE;
    }

    private void activateTrap(ServerPlayer player) {
        trappedPlayer = player;
        trapStartTick = tickCount;
        escapeInputs.set(0);

        storeOriginalHealth(player);
        setTrapState(TrapState.ACTIVATING);
        setVisible(true);
        setEscapeProgress(0.0f);

        snapPlayerToTrap(player);

        PacketHandler.sendToPlayer(new TrapStatePacket(true, 0.0f), player);
    }

    private void storeOriginalHealth(ServerPlayer player) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            originalMaxHealth = maxHealthAttr.getValue();
        }
    }

    private void dealTrapDamage() {
        if (trappedPlayer == null) return;

        AttributeInstance maxHealthAttr = trappedPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            double currentMaxHealth = maxHealthAttr.getValue();
            if (currentMaxHealth > 4.0) {
                maxHealthAttr.setBaseValue(Math.max(4.0, currentMaxHealth - 2.0));

                if (trappedPlayer.getHealth() > maxHealthAttr.getValue()) {
                    trappedPlayer.setHealth((float) maxHealthAttr.getValue());
                }
            } else {
                trappedPlayer.hurt(trappedPlayer.damageSources().generic(), Float.MAX_VALUE);
            }
        }
    }

    private void forcePlayerPosition() {
        if (trappedPlayer == null) return;

        trappedPlayer.teleportTo(getX(), getY() + 0.5, getZ());
        trappedPlayer.setDeltaMovement(Vec3.ZERO);
        trappedPlayer.hurtMarked = true;
        trappedPlayer.getAbilities().flying = false;
        trappedPlayer.getAbilities().mayfly = false;
        trappedPlayer.onUpdateAbilities();

        trappedPlayer.invulnerableTime = 0;
    }

    private void snapPlayerToTrap(ServerPlayer player) {
        player.teleportTo(getX(), getY() + 0.5, getZ());
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;

        player.hurt(player.damageSources().generic(), DAMAGE_AMOUNT);
    }

    private void updateEscapeProgress() {
        if (trappedPlayer == null) return;

        int currentInputs = escapeInputs.get();
        float progress = Math.min(100.0f, (currentInputs / (float) REQUIRED_ESCAPE_INPUTS) * 100.0f);
        setEscapeProgress(progress);
    }

    public boolean processEscapeInput(UUID playerId) {
        if (trappedPlayer == null || !trappedPlayer.getUUID().equals(playerId) || getCurrentState() != TrapState.TRAPPING) {
            return false;
        }

        int currentInputs = escapeInputs.incrementAndGet();
        float progress = Math.min(100.0f, (currentInputs / (float) REQUIRED_ESCAPE_INPUTS) * 100.0f);
        setEscapeProgress(progress);

        PacketHandler.sendToPlayer(new TrapStatePacket(true, progress), trappedPlayer);

        if (currentInputs >= REQUIRED_ESCAPE_INPUTS) {
            escapePlayer();
        }

        return true;
    }

    private void escapePlayer() {
        if (trappedPlayer != null) {
            trappedPlayer.teleportTo(getX(), getY() + 1.0, getZ());

            PacketHandler.sendToPlayer(new TrapStatePacket(false, 0.0f), trappedPlayer);

            setTrapState(TrapState.ESCAPING);
            setVisible(true);
            trapStartTick = tickCount;
        }
    }

    private void disappear() {
        if (trappedPlayer != null) {
            PacketHandler.sendToPlayer(new TrapStatePacket(false, 0.0f), trappedPlayer);
        }

        setVisible(false);
        this.discard();
    }

    private void reset() {
        if (trappedPlayer != null) {
            PacketHandler.sendToPlayer(new TrapStatePacket(false, 0.0f), trappedPlayer);
        }

        trappedPlayer = null;
        originalMaxHealth = 0;
        escapeInputs.set(0);
        trapStartTick = 0;
        ticksSinceLastDetection = 0;
        ticksSinceLastDamage = 0;

        setTrapState(TrapState.IDLE);
        setVisible(false);
        setEscapeProgress(0.0f);
    }

    public boolean isTrappingPlayer(UUID playerId) {
        return trappedPlayer != null &&
                trappedPlayer.getUUID().equals(playerId) &&
                getCurrentState() == TrapState.TRAPPING;
    }

    private void setTrapState(TrapState state) {
        entityData.set(TRAP_STATE, state.id);
    }

    private void setVisible(boolean visible) {
        entityData.set(IS_VISIBLE, visible);
    }

    private void setEscapeProgress(float progress) {
        entityData.set(ESCAPE_PROGRESS, progress);
    }

    public TrapState getCurrentState() {
        return TrapState.fromId(entityData.get(TRAP_STATE));
    }

    public boolean shouldRender() {
        return entityData.get(IS_VISIBLE);
    }

    public float getEscapeProgress() {
        return entityData.get(ESCAPE_PROGRESS);
    }

    public boolean hasTrappedPlayer() {
        return trappedPlayer != null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationState<CrawlerTrapEntity> state) {
        if (!shouldRender()) {
            return PlayState.STOP;
        }

        return switch (getCurrentState()) {
            case ACTIVATING -> state.setAndContinue(RawAnimation.begin().thenPlay("trap"));
            case TRAPPING -> state.setAndContinue(RawAnimation.begin().thenLoop("trap"));
            case ESCAPING -> state.setAndContinue(RawAnimation.begin().thenPlay("scape"));
            default -> PlayState.STOP;
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        originalMaxHealth = tag.getDouble("originalMaxHealth");
        trapStartTick = tag.getInt("trapStartTick");
        escapeInputs.set(tag.getInt("escapeInputs"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("originalMaxHealth", originalMaxHealth);
        tag.putInt("trapStartTick", trapStartTick);
        tag.putInt("escapeInputs", escapeInputs.get());
    }
}
