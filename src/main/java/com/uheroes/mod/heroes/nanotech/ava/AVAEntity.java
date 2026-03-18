package com.uheroes.mod.heroes.nanotech.ava;

import com.uheroes.mod.core.flux.FluxCapability;
import com.uheroes.mod.heroes.nanotech.ability.BoosterHandler;
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

    private static final String NBT_OWNER       = "AVAOwnerUUID";
    private static final String NBT_ORBIT_ANGLE = "AVAOrbitAngle";
    private static final String NBT_SHIELD      = "AVAShieldActive";
    private static final String NBT_SIZE_IDX    = "AVASizeIndex";

    // Size presets — renderScale used by AVARenderer
    public static final float[] RENDER_SCALES = { 0.18f, 0.28f, 0.42f };
    public static final float[] ORBIT_RADII   = { 1.2f,  1.6f,  2.2f  };
    public static final String[] SIZE_NAMES   = { "§7Small", "§bMedium", "§6Large" };

    private static final float ORBIT_SPEED    = 0.030f;
    private static final float LERP_T         = 0.20f;
    private static final float RIDE_SPEED     = 0.28f;
    private static final float RIDE_VERT      = 0.22f;
    private static final int   FLUX_RIDE_TICK = 1;

    @Nullable private UUID ownerUUID;
    private float   orbitAngle   = 0f;
    private boolean shieldActive = false;
    private int     sizeIndex    = 0;
    private int     vfxTimer     = 0;

    // ── Target orbit angle when R is held (AVA moves toward threat) ───────────
    private boolean interceptMode = false;
    private float   targetAngle   = 0f;  // angle to interpose between player + threat

    // ─── Constructor & Attributes ─────────────────────────────────────────────

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

    // ─── Never take damage (fixes growing-on-hit bug) ─────────────────────────
    @Override public boolean hurt(DamageSource s, float a) { return false; }
    @Override public boolean isInvulnerableTo(DamageSource s) { return true; }

    // ─── Size cycling ─────────────────────────────────────────────────────────

    public static void cycleSize(Player player) {
        player.getCapability(AVACapability.INSTANCE).ifPresent(data ->
            data.getAvaUUID().ifPresent(id -> {
                if (!(player.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
                for (Entity e : sl.getEntities().getAll()) {
                    if (e instanceof AVAEntity ava && e.getUUID().equals(id)) {
                        ava.sizeIndex = (ava.sizeIndex + 1) % RENDER_SCALES.length;
                        player.displayClientMessage(
                            Component.literal("§b[AVA] §7Size: " + SIZE_NAMES[ava.sizeIndex]), true);
                        break;
                    }
                }
            }));
    }

    public float getRenderScale()  { return RENDER_SCALES[sizeIndex]; }
    public float getOrbitRadius()  { return ORBIT_RADII[sizeIndex]; }

    // ─── NBT ──────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID(NBT_OWNER, ownerUUID);
        tag.putFloat(NBT_ORBIT_ANGLE, orbitAngle);
        tag.putBoolean(NBT_SHIELD, shieldActive);
        tag.putInt(NBT_SIZE_IDX, sizeIndex);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(NBT_OWNER)) ownerUUID = tag.getUUID(NBT_OWNER);
        orbitAngle   = tag.getFloat(NBT_ORBIT_ANGLE);
        shieldActive = tag.getBoolean(NBT_SHIELD);
        sizeIndex    = Mth.clamp(tag.contains(NBT_SIZE_IDX) ? tag.getInt(NBT_SIZE_IDX) : 0,
                                  0, RENDER_SCALES.length - 1);
    }

    // ─── Main tick ────────────────────────────────────────────────────────────

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

    /** Must return LivingEntity in 1.20.1 */
    @Override @Nullable
    public LivingEntity getControllingPassenger() {
        Entity p = getFirstPassenger();
        return (p instanceof LivingEntity le) ? le : null;
    }

    private void tickRide(Player rider) {
        setYRot(rider.getYRot());
        yRotO = getYRot();

        float fw = rider.zza;
        float st = rider.xxa;
        double yawRad = Math.toRadians(rider.getYRot());
        double mx = -Math.sin(yawRad) * fw + -Math.cos(yawRad) * st;
        double mz =  Math.cos(yawRad) * fw +  -Math.sin(yawRad) * st;
        Vec3 h = new Vec3(mx, 0, mz);
        if (h.lengthSqr() > 1.0) h = h.normalize();
        h = h.scale(RIDE_SPEED);

        // Vertical: use BoosterHandler jetpack flag (Space) for up,
        // isCrouching() for down — avoids protected rider.jumping field
        double vy = 0;
        if (BoosterHandler.isJetpackActive(rider)) vy =  RIDE_VERT;
        else if (rider.isCrouching())              vy = -RIDE_VERT;

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
            // Lerp orbit angle toward the intercept angle so AVA slides smoothly
            float diff = Mth.wrapDegrees(
                (float) Math.toDegrees(targetAngle) -
                (float) Math.toDegrees(orbitAngle));
            orbitAngle += (float) Math.toRadians(diff) * 0.15f;
        } else {
            orbitAngle += ORBIT_SPEED;
            if (orbitAngle > Math.PI * 2) orbitAngle -= (float)(Math.PI * 2);
        }

        double tx = owner.getX() + Math.sin(orbitAngle) * r;
        double ty = owner.getY() + 1.5;
        double tz = owner.getZ() + Math.cos(orbitAngle) * r;

        setPos(
            Mth.lerp(LERP_T, getX(), tx),
            Mth.lerp(LERP_T, getY(), ty),
            Mth.lerp(LERP_T, getZ(), tz)
        );
    }

    // ─── Intercept mode (R key = AVA moves toward threat) ────────────────────

    /**
     * Activates intercept mode: AVA slides to the orbital position between
     * the player and the nearest threat/projectile.
     *
     * @param held true = R pressed, false = R released → resume orbit
     */
    public void setInterceptMode(boolean held, Player owner) {
        interceptMode = held;
        if (!held) return;

        // Find the direction the biggest threat is coming from
        Vec3 threatDir = findThreatDirection(owner);
        if (threatDir == null) return;

        // Compute the orbit angle that puts AVA between player and threat
        // Threat is at angle φ from player → AVA goes to angle φ (opposite side)
        targetAngle = (float) Math.atan2(threatDir.x, threatDir.z);
    }

    @Nullable
    private Vec3 findThreatDirection(Player owner) {
        Vec3 ownerPos = owner.position();

        // Priority 1: incoming projectile (closest)
        var projectiles = level().getEntitiesOfClass(Projectile.class,
            owner.getBoundingBox().inflate(16.0),
            p -> !(p.getOwner() instanceof Player));

        if (!projectiles.isEmpty()) {
            Entity closest = projectiles.stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(owner), b.distanceToSqr(owner)))
                .orElse(null);
            if (closest != null)
                return closest.position().subtract(ownerPos).normalize();
        }

        // Priority 2: nearest hostile mob
        var hostiles = level().getEntitiesOfClass(LivingEntity.class,
            owner.getBoundingBox().inflate(12.0),
            e -> e != owner && e != this && e.isAlive() && !e.isAlliedTo(owner));

        if (!hostiles.isEmpty()) {
            LivingEntity nearest = hostiles.stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(owner), b.distanceToSqr(owner)))
                .orElse(null);
            if (nearest != null)
                return nearest.position().subtract(ownerPos).normalize();
        }

        return null;
    }

    // ─── Shield / intercept ────────────────────────────────────────────────────

    private void tickIntercept(Player owner) {
        if (FluxCapability.getCurrent(owner) >= 5) {
            AABB box = getBoundingBox().inflate(1.8f);
            level().getEntitiesOfClass(Projectile.class, box,
                p -> !(p.getOwner() instanceof Player)
            ).forEach(p -> {
                if (!level().isClientSide() && FluxCapability.consume(owner, 2)) {
                    // Deflect! Show VFX facing incoming direction
                    if (level().isClientSide()) {
                        Vec3 hitDir = p.position().subtract(position()).normalize();
                        AVAEffects.spawnBlockDeflect(this, hitDir);
                    }
                    p.discard();
                }
            });
        }
        if (tickCount % 20 == 0 && !level().isClientSide()) {
            boolean threat = !level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(12f),
                e -> e != owner && e != this && e.isAlive() && !e.isAlliedTo(owner)
            ).isEmpty();
            if (threat) FluxCapability.consume(owner, 1);
        }
    }

    public void setShieldActive(boolean v) { shieldActive = v; }

    // ─── VFX ──────────────────────────────────────────────────────────────────

    private void tickVFX() {
        vfxTimer++;
        if (vfxTimer % 5 == 0)
            AVAEffects.spawnOrbitTrail(this, (float) Math.toRadians(getYRot()), 0f);
        if (shieldActive && vfxTimer % 20 == 0)
            AVAEffects.spawnShieldPulse(this, 1.0f);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public void setOwnerUUID(UUID uuid)  { this.ownerUUID = uuid; }
    public Optional<UUID> getOwnerUUID() { return Optional.ofNullable(ownerUUID); }
    public boolean isShieldActive()      { return shieldActive; }
    public float getShieldRadius()       { return shieldActive ? 2.5f : 0f; }
    public float getOrbitAngle()         { return orbitAngle; }

    @Nullable
    private Player resolveOwner() {
        return ownerUUID == null ? null : level().getPlayerByUUID(ownerUUID);
    }

    @Override public boolean removeWhenFarAway(double d) { return false; }
    @Override protected void registerGoals() { }

    // ─── GeckoLib ─────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar reg) {
        reg.add(new AnimationController<>(this, "ava_ctrl", 4, state -> {
            state.getController().setAnimation(
                RawAnimation.begin().thenLoop("animation.ava.idle"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return animCache; }
}