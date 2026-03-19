package com.uheroes.mod.heroes.nanotech.ava;

import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.core.network.AVAVfxPacket;
import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.heroes.nanotech.ava.AVABlasterEntity;
import com.uheroes.mod.heroes.nanotech.ability.BoosterHandler;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class AVAEntity extends Mob implements GeoEntity {

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    // ── Synced data (sent server→client every update interval) ────────────────
    private static final EntityDataAccessor<Boolean> SHIELD_ACTIVE =
        SynchedEntityData.defineId(AVAEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SIZE_INDEX =
        SynchedEntityData.defineId(AVAEntity.class, EntityDataSerializers.INT);

    private static final String NBT_OWNER       = "AVAOwnerUUID";
    private static final String NBT_ORBIT_ANGLE = "AVAOrbitAngle";

    public static final float[] RENDER_SCALES = { 0.18f, 0.28f, 0.42f };
    public static final float[] ORBIT_RADII   = { 1.2f,  1.6f,  2.2f  };
    public static final String[] SIZE_NAMES   = { "§7Small", "§bMedium", "§6Large" };

    private static final float ORBIT_SPEED    = 0.030f;
    private static final float LERP_T         = 0.18f;
    private static final float RIDE_SPEED     = 0.28f;
    private static final float RIDE_VERT      = 0.22f;
    private static final int   FLUX_RIDE_TICK = 1;

    @Nullable private UUID ownerUUID;
    private float orbitAngle = 0f;
    private int   vfxTimer   = 0;

    private boolean interceptMode = false;
    private float   targetAngle   = 0f;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public AVAEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setInvulnerable(true);
        noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    // ── Synched data definition ───────────────────────────────────────────────

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SHIELD_ACTIVE, false);
        entityData.define(SIZE_INDEX, 0);
    }

    // ── Getters/setters for synced fields ─────────────────────────────────────

    public boolean isShieldActive()    { return entityData.get(SHIELD_ACTIVE); }
    public void setShieldActive(boolean v) { entityData.set(SHIELD_ACTIVE, v); }

    public int  getSizeIndex()         { return entityData.get(SIZE_INDEX); }
    public float getRenderScale()      { return RENDER_SCALES[getSizeIndex()]; }
    public float getOrbitRadius()      { return ORBIT_RADII[getSizeIndex()]; }
    public float getShieldRadius()     { return isShieldActive() ? 2.5f : 0f; }

    // ─── Hurt override — fixes growing bug ────────────────────────────────────
    @Override public boolean hurt(DamageSource s, float a) { return false; }
    @Override public boolean isInvulnerableTo(DamageSource s) { return true; }

    // ─── Size cycling ─────────────────────────────────────────────────────────

    public static void cycleSize(Player player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
        player.getCapability(AVACapability.INSTANCE).ifPresent(data ->
            data.getAvaUUID().ifPresent(id -> {
                for (Entity e : sl.getEntities().getAll()) {
                    if (e instanceof AVAEntity ava && e.getUUID().equals(id)) {
                        int next = (ava.getSizeIndex() + 1) % RENDER_SCALES.length;
                        ava.entityData.set(SIZE_INDEX, next);
                        player.displayClientMessage(
                            Component.literal("§b[AVA] §7Size: " + SIZE_NAMES[next]), true);
                        break;
                    }
                }
            }));
    }

    // ─── NBT ──────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID(NBT_OWNER, ownerUUID);
        tag.putFloat(NBT_ORBIT_ANGLE, orbitAngle);
        tag.putBoolean("AVAShieldActive", isShieldActive());
        tag.putInt("AVASizeIndex", getSizeIndex());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(NBT_OWNER)) ownerUUID = tag.getUUID(NBT_OWNER);
        orbitAngle = tag.getFloat(NBT_ORBIT_ANGLE);
        setShieldActive(tag.getBoolean("AVAShieldActive"));
        entityData.set(SIZE_INDEX,
            Mth.clamp(tag.contains("AVASizeIndex") ? tag.getInt("AVASizeIndex") : 0,
                      0, RENDER_SCALES.length - 1));
    }

    // ─── Tick ─────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();

        // VFX always ticks client-side regardless of mode
        if (level().isClientSide()) tickVFX();

        if (isVehicle()) {
            if (getFirstPassenger() instanceof Player rider) tickRide(rider);
            return;
        }
        Player owner = resolveOwner();
        if (owner == null) return;
        updateOrbit(owner);
        tickIntercept(owner);
        if (!level().isClientSide()) tickBlasterAttack(owner);
    }

    // ─── Riding ───────────────────────────────────────────────────────────────

    @Override public boolean canAddPassenger(Entity p) {
        return getPassengers().isEmpty() && p instanceof Player;
    }
    @Override protected boolean canRide(Entity e) { return false; }

    @Override
    public void positionRider(Entity passenger, MoveFunction fn) {
        fn.accept(passenger, getX(), getY() + getBbHeight() + 0.15, getZ());
    }

    @Override @Nullable
    public LivingEntity getControllingPassenger() {
        Entity p = getFirstPassenger();
        return (p instanceof LivingEntity le) ? le : null;
    }

    private void tickRide(Player rider) {
        setYRot(rider.getYRot());
        yRotO = getYRot();
        float fw = rider.zza, st = rider.xxa;
        double yawRad = Math.toRadians(rider.getYRot());
        double mx = -Math.sin(yawRad) * fw + -Math.cos(yawRad) * st;
        double mz =  Math.cos(yawRad) * fw +  -Math.sin(yawRad) * st;
        Vec3 h = new Vec3(mx, 0, mz);
        if (h.lengthSqr() > 1.0) h = h.normalize();
        h = h.scale(RIDE_SPEED);
        double vy = BoosterHandler.isJetpackActive(rider) ? RIDE_VERT
                  : rider.isCrouching()                   ? -RIDE_VERT : 0;
        if (!level().isClientSide() && (h.lengthSqr() > 0.001 || Math.abs(vy) > 0.001))
            FluxCapability.consume(rider, FLUX_RIDE_TICK);
        setDeltaMovement(h.x, vy, h.z);
        fallDistance = 0;
        resetFallDistance();
    }

    // ─── Orbit ────────────────────────────────────────────────────────────────

    private void updateOrbit(Player owner) {
        float r = getOrbitRadius();

        if (interceptMode) {
            float diff = targetAngle - orbitAngle;
            while (diff >  Math.PI) diff -= (float)(2 * Math.PI);
            while (diff < -Math.PI) diff += (float)(2 * Math.PI);
            orbitAngle += diff * 0.10f;
        } else {
            orbitAngle += ORBIT_SPEED;
            if (orbitAngle > Math.PI * 2) orbitAngle -= (float)(2 * Math.PI);
        }

        double tx = owner.getX() + Math.sin(orbitAngle) * r;
        double ty = owner.getY() + 1.6;
        double tz = owner.getZ() + Math.cos(orbitAngle) * r;

        // Smooth movement via velocity, not teleport — avoids jitter
        double dx = tx - getX();
        double dy = ty - getY();
        double dz = tz - getZ();
        setDeltaMovement(dx * 0.4, dy * 0.4, dz * 0.4);
        move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());

        // Face orbit direction
        if (dx * dx + dz * dz > 0.0001) {
            setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
        }
    }

    // ─── R key: intercept mode ────────────────────────────────────────────────

    public void setInterceptMode(boolean held, Player owner) {
        interceptMode = held;
        if (!held) return;
        Vec3 dir = findThreatDirection(owner);
        if (dir != null) targetAngle = (float) Math.atan2(dir.x, dir.z);
    }

    @Nullable private Vec3 findThreatDirection(Player owner) {
        var projs = level().getEntitiesOfClass(Projectile.class,
            owner.getBoundingBox().inflate(16.0), p -> !(p.getOwner() instanceof Player));
        if (!projs.isEmpty()) {
            return projs.stream().min((a,b)->Double.compare(a.distanceToSqr(owner),b.distanceToSqr(owner)))
                .map(p -> p.position().subtract(owner.position()).normalize()).orElse(null);
        }
        var mobs = level().getEntitiesOfClass(LivingEntity.class,
            owner.getBoundingBox().inflate(12.0),
            e -> e != owner && e != this && e.isAlive() && !e.isAlliedTo(owner));
        if (!mobs.isEmpty()) {
            return mobs.stream().min((a,b)->Double.compare(a.distanceToSqr(owner),b.distanceToSqr(owner)))
                .map(e -> e.position().subtract(owner.position()).normalize()).orElse(null);
        }
        return null;
    }

    // ─── Intercept / shield ───────────────────────────────────────────────────

    private void tickIntercept(Player owner) {
        if (FluxCapability.getCurrent(owner) >= 5) {
            level().getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(1.8f),
                p -> !(p.getOwner() instanceof Player) && !(p instanceof AVABlasterEntity)
            ).forEach(p -> {
                if (!level().isClientSide() && FluxCapability.consume(owner, 2)) {
                    // Send VFX to all nearby clients BEFORE discarding
                    Vec3 hitDir = p.getDeltaMovement().normalize();
                    Vec3 pos = getEyePosition();
                    ModNetwork.sendToAllTracking(
                        new AVAVfxPacket(AVAVfxPacket.Type.BLOCK_DEFLECT,
                            (float) pos.x, (float) pos.y, (float) pos.z,
                            (float) hitDir.x, (float) hitDir.y, (float) hitDir.z),
                        this);
                    p.discard();
                }
            });
        }
        if (tickCount % 20 == 0 && !level().isClientSide()) {
            boolean threat = !level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(12f),
                e -> e != owner && e != this && e.isAlive() && !e.isAlliedTo(owner)).isEmpty();
            if (threat) FluxCapability.consume(owner, 1);
        }
    }

    // ─── Blaster Attack ───────────────────────────────────────────────────────

    private static final int BLASTER_COOLDOWN = 60; // ticks between shots (3s)
    private int blasterTimer = 0;

    private void tickBlasterAttack(Player owner) {
        blasterTimer++;
        if (blasterTimer < BLASTER_COOLDOWN) return;

        // Find nearest threat in 16-block range
        LivingEntity target = level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(16.0),
            e -> e != owner && e != this && e.isAlive() && !e.isAlliedTo(owner)
        ).stream()
            .min((a, b) -> Double.compare(a.distanceToSqr(this), b.distanceToSqr(this)))
            .orElse(null);

        if (target == null) return;
        blasterTimer = 0;

        // Spawn blaster bolt
        AVABlasterEntity bolt = new AVABlasterEntity(level(), this, target);
        level().addFreshEntity(bolt);

        // Muzzle flash VFX
        Vec3 muzzle = position().add(0, getBbHeight() * 0.5, 0);
        Vec3 dir = target.getEyePosition().subtract(muzzle).normalize();
        ModNetwork.sendToAllTracking(
            new AVAVfxPacket(AVAVfxPacket.Type.BLASTER_MUZZLE,
                (float) muzzle.x, (float) muzzle.y, (float) muzzle.z,
                (float) dir.x, (float) dir.y, (float) dir.z),
            this);
    }

    // ─── VFX ──────────────────────────────────────────────────────────────────

    private void tickVFX() {
        vfxTimer++;
        if (vfxTimer % 5 == 0) AVAEffects.spawnOrbitTrail(this, (float)Math.toRadians(getYRot()), 0f);
        if (isShieldActive() && vfxTimer % 20 == 0) AVAEffects.spawnShieldPulse(this, 1.0f);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public void setOwnerUUID(UUID uuid)  { this.ownerUUID = uuid; }
    public Optional<UUID> getOwnerUUID() { return Optional.ofNullable(ownerUUID); }
    public float getOrbitAngle()         { return orbitAngle; }

    @Nullable private Player resolveOwner() {
        return ownerUUID == null ? null : level().getPlayerByUUID(ownerUUID);
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override

    @Override protected void registerGoals() { }

    // ─── GeckoLib ─────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar reg) {
        reg.add(new AnimationController<>(this, "ava_ctrl", 4, state -> {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("animation.ava.idle"));
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animCache; }
}