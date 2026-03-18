package com.uheroes.mod.heroes.nanotech.ava;

import com.uheroes.mod.core.flux.FluxCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.Optional;
import java.util.UUID;

/**
 * AVA — Autonomous Vector Assistant.
 *
 * <p>An AI companion entity that orbits the player, provides shields,
 * intercepts projectiles, and attacks threats. Powered entirely by
 * the player's Neural Flux.
 *
 * <h2>State Machine</h2>
 * <pre>
 *   PASSIVE ──(threat detected)──► ALERT ──(projectile incoming)──► INTERCEPT
 *                                     │
 *                                     └──(player attacks)──► ATTACK
 * </pre>
 *
 * <h2>Flux Costs (per tick unless noted)</h2>
 * <ul>
 *   <li>PASSIVE    — 0 Flux (free orbit)</li>
 *   <li>ALERT      — 2 Flux / second (20 Flux / 10 sec)</li>
 *   <li>INTERCEPT  — 4 Flux / second</li>
 *   <li>SHIELD_ON  — 15 Flux / second (hold key)</li>
 *   <li>ATTACK     — 8 Flux / shot (once)</li>
 * </ul>
 *
 * <h2>Linking</h2>
 * AVA is linked to a player by UUID stored in NBT key {@value #NBT_OWNER}.
 * On player respawn, a new AVA is spawned and the UUID is updated.
 * AVA cannot be killed in the traditional sense — if forcibly removed it
 * respawns after {@value #RESPAWN_COOLDOWN_TICKS} ticks.
 *
 * <h2>TODO (Visual)</h2>
 * Replace {@code Mob} base with a GeckoLib {@code GeoEntity} once the
 * model/texture is ready. Add {@code @GeoEntityMarker} and register
 * a {@code GeckoLibUtil.createInstanceCache(this)} for animations.
 */
public class AVAEntity extends Mob implements GeoEntity {
    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    // ─── Constants ───────────────────────────────────────────────────────────
    private static final String NBT_OWNER          = "AVAOwnerUUID";
    private static final String NBT_STATE          = "AVAState";
    private static final String NBT_ORBIT_ANGLE    = "AVAOrbitAngle";
    private static final String NBT_SHIELD_ACTIVE  = "AVAShieldActive";

    public static final float  ORBIT_RADIUS        = 1.8f;   // blocks
    private static final float ORBIT_SPEED         = 0.028f; // radians / tick ≈ 1 orbit / 3.7s
    private static final float LERP_T              = 0.18f;  // positional smoothing
    private static final float INTERCEPT_RADIUS    = 2.4f;   // projectile catch range
    private static final float THREAT_SCAN_RANGE   = 12.0f;  // hostile detection range
    private static final int   RESPAWN_COOLDOWN_TICKS = 600; // 30 seconds

    // Flux costs (per 20 ticks = per second)
    private static final int FLUX_ALERT_PER_SEC     = 2;
    private static final int FLUX_INTERCEPT_PER_SEC = 4;
    private static final int FLUX_SHIELD_PER_SEC    = 15;
    private static final int FLUX_ATTACK_PER_SHOT   = 8;

    // ─── State ───────────────────────────────────────────────────────────────
    public enum AVAState { PASSIVE, ALERT, INTERCEPT, ATTACK }

    @Nullable private UUID     ownerUUID;
    private AVAState  state        = AVAState.PASSIVE;
    private float     orbitAngle   = 0f;
    private boolean   shieldActive = false;
    private float     shieldRadius = 0f;
    private int       vfxTrailTimer = 0;

    // ─── Constructor & Attributes ─────────────────────────────────────────────
    public AVAEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0)   // movement handled manually
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    // ─── NBT ──────────────────────────────────────────────────────────────────
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUUID != null) tag.putUUID(NBT_OWNER, ownerUUID);
        tag.putString(NBT_STATE, state.name());
        tag.putFloat(NBT_ORBIT_ANGLE, orbitAngle);
        tag.putBoolean(NBT_SHIELD_ACTIVE, shieldActive);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(NBT_OWNER)) ownerUUID = tag.getUUID(NBT_OWNER);
        try { state = AVAState.valueOf(tag.getString(NBT_STATE)); } catch (Exception ignored) {}
        orbitAngle   = tag.getFloat(NBT_ORBIT_ANGLE);
        shieldActive = tag.getBoolean(NBT_SHIELD_ACTIVE);
    }

    // ─── Tick ─────────────────────────────────────────────────────────────────
    @Override
    public void tick() {
        super.tick();

        Player owner = resolveOwner();
        if (owner == null) {
            // No owner found — wait silently (or discard after timeout)
            return;
        }

        // ── State evaluation ──────────────────────────────────────────────────
        evaluateState(owner);

        // ── Orbital position update ───────────────────────────────────────────
        updateOrbit(owner);

        // ── Per-state behaviour ───────────────────────────────────────────────
        switch (state) {
            case PASSIVE  -> tickPassive(owner);
            case ALERT    -> tickAlert(owner);
            case INTERCEPT -> tickIntercept(owner);
            case ATTACK   -> tickAttack(owner);
        }

        // ── Shield visual update ───────────────────────────────────────────────
        updateShieldRadius();

        // ── VFX (client only) ─────────────────────────────────────────────────
        if (level().isClientSide()) {
            tickVFX();
        }
    }

    // ─── State Machine ────────────────────────────────────────────────────────

    private void evaluateState(Player owner) {
        // Check flux — if too low, revert to passive
        int currentFlux = FluxCapability.getCurrent(owner);
        if (currentFlux <= 5) {
            state = AVAState.PASSIVE;
            shieldActive = false;
            return;
        }

        // Scan for threats
        boolean hasThreat = !level().getEntitiesOfClass(
            LivingEntity.class,
            getBoundingBox().inflate(THREAT_SCAN_RANGE),
            e -> e != owner && e != this && e.isAlive()
                 && !e.isAlliedTo(owner)
        ).isEmpty();

        // Scan for incoming projectiles
        boolean hasProjectile = !level().getEntitiesOfClass(
            Projectile.class,
            getBoundingBox().inflate(INTERCEPT_RADIUS),
            p -> !(p.getOwner() instanceof Player)
        ).isEmpty();

        if (hasProjectile && currentFlux >= FLUX_INTERCEPT_PER_SEC) {
            state = AVAState.INTERCEPT;
        } else if (hasThreat && currentFlux >= FLUX_ALERT_PER_SEC) {
            state = AVAState.ALERT;
        } else {
            state = AVAState.PASSIVE;
        }
    }

    // ─── State Handlers ───────────────────────────────────────────────────────

    private void tickPassive(Player owner) {
        // No Flux cost — pure orbit
    }

    private void tickAlert(Player owner) {
        // Face nearest threat
        LivingEntity nearest = getNearestThreat(owner);
        if (nearest != null) {
            Vec3 dir = nearest.position().subtract(position()).normalize();
            setYRot((float) Math.toDegrees(Math.atan2(dir.x, dir.z)));
        }
        // Flux drain: 2/sec = 1 every 10 ticks
        if (tickCount % 10 == 0) {
            FluxCapability.consume(owner, 1);
        }
    }

    private void tickIntercept(Player owner) {
        // Consume projectiles entering shield radius
        AABB catchBox = getBoundingBox().inflate(INTERCEPT_RADIUS);
        level().getEntitiesOfClass(Projectile.class, catchBox,
            p -> !(p.getOwner() instanceof Player)
        ).forEach(p -> {
            if (FluxCapability.consume(owner, 1)) {
                p.discard();
                // Trigger deflect VFX at interception point (client only via packet or direct)
                if (level().isClientSide()) {
                    Vec3 hitDir = p.position().subtract(position()).normalize();
                    AVAEffects.spawnBlockDeflect(this, hitDir);
                }
            }
        });
        // Flux drain: 4/sec = 1 every 5 ticks
        if (tickCount % 5 == 0) {
            FluxCapability.consume(owner, 1);
        }
    }

    private void tickAttack(Player owner) {
        // TODO Phase 4: fire a projectile at the nearest threat
        // Requires: AVACoreProjectile entity registration
        // For now: stub that can be filled in Phase 4
    }

    // ─── Shield ───────────────────────────────────────────────────────────────

    /**
     * Activates the shield sphere. Call from keybind handler (server-side).
     * The shield stays active as long as the owner holds the key AND has Flux.
     */
    public void setShieldActive(boolean active) {
        this.shieldActive = active;
    }

    private void updateShieldRadius() {
        float target = shieldActive ? 2.5f : 0f;
        shieldRadius = Mth.lerp(0.15f, shieldRadius, target);

        if (shieldActive) {
            Player owner = resolveOwner();
            if (owner != null) {
                // Drain 15 Flux/sec = 1 every ~1.3 ticks → 1 per tick for simplicity
                if (tickCount % 2 == 0 && !FluxCapability.consume(owner, 1)) {
                    shieldActive = false; // Out of Flux — shield drops
                }
                // Block melee inside sphere
                AABB sphereBox = getBoundingBox().inflate(shieldRadius);
                level().getEntitiesOfClass(LivingEntity.class, sphereBox,
                    e -> e != owner && e != this
                ).forEach(e -> {
                    // Push entities out of sphere
                    Vec3 push = e.position().subtract(position()).normalize().scale(0.3);
                    e.setDeltaMovement(e.getDeltaMovement().add(push));
                });
            }
        }
    }

    // ─── Orbital Movement ────────────────────────────────────────────────────

    private void updateOrbit(Player owner) {
        orbitAngle += ORBIT_SPEED;
        if (orbitAngle > Math.PI * 2) orbitAngle -= (float)(Math.PI * 2);

        double tx = owner.getX() + Math.sin(orbitAngle) * ORBIT_RADIUS;
        double ty = owner.getY() + 1.4;  // float at head height
        double tz = owner.getZ() + Math.cos(orbitAngle) * ORBIT_RADIUS;

        // Smooth lerp toward orbital target
        setPos(
            Mth.lerp(LERP_T, getX(), tx),
            Mth.lerp(LERP_T, getY(), ty),
            Mth.lerp(LERP_T, getZ(), tz)
        );
    }

    // ─── VFX (client-side) ────────────────────────────────────────────────────

    private void tickVFX() {
        vfxTrailTimer++;

        // Orbital glow trail — every 4 ticks
        if (vfxTrailTimer % 4 == 0) {
            float yaw   = (float) Math.toRadians(getYRot());
            float pitch = (float) Math.toRadians(getXRot());
            AVAEffects.spawnOrbitTrail(this, yaw, pitch);
        }

        // Shield pulse ring — every 20 ticks while shield is expanding
        if (shieldActive && shieldRadius > 0.5f && vfxTrailTimer % 20 == 0) {
            AVAEffects.spawnShieldPulse(this, shieldRadius / 2.5f);
        }
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    /**
     * Locates the owning Player entity in the current level.
     */
    @Nullable
    private Player resolveOwner() {
        if (ownerUUID == null) return null;
        return level().getPlayerByUUID(ownerUUID);
    }

    /**
     * Finds the nearest non-player hostile within threat scan range.
     */
    @Nullable
    private LivingEntity getNearestThreat(Player owner) {
        return level().getEntitiesOfClass(
            LivingEntity.class,
            getBoundingBox().inflate(THREAT_SCAN_RANGE),
            e -> e != owner && e != this && e.isAlive() && !e.isAlliedTo(owner)
        ).stream()
            .min((a, b) -> Double.compare(a.distanceToSqr(this), b.distanceToSqr(this)))
            .orElse(null);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public void setOwnerUUID(UUID uuid) { this.ownerUUID = uuid; }
    public Optional<UUID> getOwnerUUID() { return Optional.ofNullable(ownerUUID); }
    public AVAState getAVAState() { return state; }
    public boolean isShieldActive() { return shieldActive; }
    public float getShieldRadius() { return shieldRadius; }
    public float getOrbitAngle() { return orbitAngle; }

    // AVA cannot be targeted by mobs or hurt normally
    @Override public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) { return true; }
    @Override public boolean removeWhenFarAway(double dist) { return false; }
    @Override protected void registerGoals() { /* no vanilla AI — all logic above */ }

    // ─── GeckoLib ─────────────────────────────────────────────────────────────

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "ava_ctrl", 4, state -> {
            // "idle" plays while AVA exists — retract/deploy wired separately
            state.getController().setAnimation(
                RawAnimation.begin().thenLoop("animation.ava.idle"));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return animCache; }
}