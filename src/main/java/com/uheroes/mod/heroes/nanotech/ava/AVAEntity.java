package com.uheroes.mod.heroes.nanotech.ava;

import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.core.network.AVAVfxPacket;
import com.uheroes.mod.core.network.ModNetwork;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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

    private static final EntityDataAccessor<Boolean> SHIELD_ACTIVE =
        SynchedEntityData.defineId(AVAEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SIZE_INDEX =
        SynchedEntityData.defineId(AVAEntity.class, EntityDataSerializers.INT);

    private static final String NBT_OWNER       = "AVAOwnerUUID";
    private static final String NBT_ORBIT_ANGLE = "AVAOrbitAngle";

    public static final float[]  RENDER_SCALES = { 0.18f, 0.28f, 0.42f };
    public static final float[]  ORBIT_RADII   = { 1.2f,  1.6f,  2.2f  };
    public static final String[] SIZE_NAMES    = { "§7Small", "§bMedium", "§6Large" };

    private static final float ORBIT_SPEED    = 0.030f;
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

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SHIELD_ACTIVE, false);
        entityData.define(SIZE_INDEX, 0);
    }

    public boolean isShieldActive()        { return entityData.get(SHIELD_ACTIVE); }
    public void setShieldActive(boolean v) { entityData.set(SHIELD_ACTIVE, v); }
    public int   getSizeIndex()            { return entityData.get(SIZE_INDEX); }
    public float getRenderScale()          { return RENDER_SCALES[getSizeIndex()]; }
    public float getOrbitRadius()          { return ORBIT_RADII[getSizeIndex()]; }
    public float getShieldRadius()         { return isShieldActive() ? 2.5f : 0f; }

    @Override public boolean hurt(DamageSource s, float a)          { return false; }
    @Override public boolean isInvulnerableTo(DamageSource s)       { return true; }

    // ─── Size cycling (server-side) ───────────────────────────────────────────

    public static void cycleSize(Player player) {
        if (!(player.level() instanceof ServerLevel sl)) return;
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
        if (isVehicle()) {
            if (getFirstPassenger() instanceof Player rider) tickRide(rider);
            return;
        }
        Player owner = resolveOwner();
        if (owner == null) return;
        updateOrbit(owner);
        tickIntercept(owner);
        if (!level().isClientSide()) tickBlaster(owner);
        // VFX: CLIENT-SIDE ONLY — AAALevel.addParticle is a client API
        if (level().isClientSide()) tickVFX();
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

        double dx = tx - getX();
        double dy = ty - getY();
        double dz = tz - getZ();
        setDeltaMovement(dx * 0.4, dy * 0.4, dz * 0.4);
        move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());

        if (dx * dx + dz * dz > 0.0001) {
            setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
        }
    }

    // ─── Intercept mode ───────────────────────────────────────────────────────

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
                p -> !(p.getOwner() instanceof Player)
            ).forEach(p -> {
                Vec3 hitDir = p.position().subtract(position()).normalize();
                if (!level().isClientSide() && FluxCapability.consume(owner, 2)) {
                    // Face the incoming threat before deflect VFX fires
                    faceTarget(p.position());
                    p.discard();
                    // SERVER: send deflect VFX packet to all nearby clients
                    Vec3 pos = getEyePosition();
                    ModNetwork.sendToAllTracking(
                        new AVAVfxPacket(AVAVfxPacket.Type.BLOCK_DEFLECT,
                            (float)pos.x, (float)pos.y, (float)pos.z,
                            (float)hitDir.x, (float)hitDir.y, (float)hitDir.z),
                        this);
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

    // ─── VFX (CLIENT-SIDE ONLY) ───────────────────────────────────────────────

    private boolean prevShieldActive = false;

    private void tickVFX() {
        // This method is ONLY called when level().isClientSide() is true
        vfxTimer++;

        // Orbit trail — every 3 ticks while not riding
        if (!isVehicle() && vfxTimer % 3 == 0) {
            AVAEffects.spawnOrbitTrail(this, (float)Math.toRadians(getYRot()), 0f);
        }

        // Shield pulse
        boolean shieldNow = isShieldActive();
        if (shieldNow && !prevShieldActive) {
            AVAEffects.spawnShieldPulse(this, 1.8f);
        } else if (shieldNow && vfxTimer % 15 == 0) {
            AVAEffects.spawnShieldPulse(this, 1.0f);
        }
        prevShieldActive = shieldNow;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public void setOwnerUUID(UUID uuid)  { this.ownerUUID = uuid; }
    public Optional<UUID> getOwnerUUID() { return Optional.ofNullable(ownerUUID); }
    public float getOrbitAngle()         { return orbitAngle; }

    @Nullable private Player resolveOwner() {
        return ownerUUID == null ? null : level().getPlayerByUUID(ownerUUID);
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }

    // ─── Blaster (SERVER-SIDE ONLY) ───────────────────────────────────────────

    @Nullable

    private void fireBlaster(LivingEntity target, Player owner) {
        Vec3 from = getEyePosition();
        Vec3 to   = target.getEyePosition();
        Vec3 dir  = to.subtract(from).normalize();
        double dist = from.distanceTo(to);

        // Snap AVA to face the target cleanly — no more "qiyshiq" shooting
        faceTarget(to);

        // SERVER: send blaster muzzle VFX to all clients
        ModNetwork.sendToAllTracking(
            new AVAVfxPacket(AVAVfxPacket.Type.BLASTER_MUZZLE,
                (float)from.x, (float)from.y, (float)from.z,
                (float)dir.x,  (float)dir.y,  (float)dir.z),
            this);

        // Instant hit
        target.hurt(owner.damageSources().playerAttack(owner), 8.0f);

        // SERVER: send blaster impact VFX to all clients
        Vec3 impactPos = from.add(dir.scale(dist));
        ModNetwork.sendToAllTracking(
            new AVAVfxPacket(AVAVfxPacket.Type.BLASTER_HIT,
                (float)impactPos.x, (float)impactPos.y, (float)impactPos.z,
                (float)dir.x,       (float)dir.y,       (float)dir.z),
            this);

        // Blaster sound
        if (level() instanceof ServerLevel sl) {
            sl.playSound(null, blockPosition(),
                net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER,
                net.minecraft.sounds.SoundSource.NEUTRAL, 0.12f, 3.8f);
        }
    }

    private int blasterCooldown = 0;

    private void tickBlaster(Player owner) {
        if (blasterCooldown > 0) { blasterCooldown--; return; }
        if (!isShieldActive()) return;

        LivingEntity target = findNearestHostile(owner);
        if (target != null && target.distanceTo(this) < 14.0) {
            fireBlaster(target, owner);
            blasterCooldown = 25;
        }
    }

    /** Instantly snap yaw/pitch to face a world position — looks crisp, not "qiyshiq". */
    private void faceTarget(Vec3 target) {
        Vec3 diff  = target.subtract(getEyePosition());
        float yaw  = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        float pitch = (float) Math.toDegrees(-Math.asin(
            Math.max(-1, Math.min(1, diff.normalize().y))
        ));
        setYRot(yaw);
        setXRot(pitch);
        yRotO  = yaw;
        xRotO  = pitch;
    }

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
    @javax.annotation.Nullable
    private LivingEntity findNearestHostile(Player owner) {
        java.util.Set<Integer> marked =
            com.uheroes.mod.heroes.nanotech.ability.ScannerHandler.getMarked(owner);

        return level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(14.0),
            e -> e != owner && e != this && e.isAlive() && !e.isAlliedTo(owner)
        ).stream().min((a, b) -> {
            boolean aM = marked.contains(a.getId());
            boolean bM = marked.contains(b.getId());
            if (aM != bM) return aM ? -1 : 1;
            return Double.compare(a.distanceToSqr(this), b.distanceToSqr(this));
        }).orElse(null);
    }
}