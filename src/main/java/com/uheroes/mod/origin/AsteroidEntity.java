package com.uheroes.mod.origin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AsteroidEntity extends Entity implements GeoEntity {

    private static final EntityDataAccessor<Float> TARGET_X =
        SynchedEntityData.defineId(AsteroidEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Y =
        SynchedEntityData.defineId(AsteroidEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Z =
        SynchedEntityData.defineId(AsteroidEntity.class, EntityDataSerializers.FLOAT);

    public static final int IMPACT_TICK  = 70;
    public static final int DESPAWN_TICK = 200;

    private double startX, startY, startZ;
    private boolean hasImpacted = false;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AsteroidEntity(EntityType<? extends AsteroidEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void initFlight(Vec3 target) {
        this.startX = this.getX();
        this.startY = this.getY();
        this.startZ = this.getZ();
        this.entityData.set(TARGET_X, (float) target.x);
        this.entityData.set(TARGET_Y, (float) target.y);
        this.entityData.set(TARGET_Z, (float) target.z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TARGET_X, 0.0f);
        this.entityData.define(TARGET_Y, 0.0f);
        this.entityData.define(TARGET_Z, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();

        float tx = entityData.get(TARGET_X);
        float ty = entityData.get(TARGET_Y);
        float tz = entityData.get(TARGET_Z);

        if (tickCount <= IMPACT_TICK) {
            double t = (double) tickCount / IMPACT_TICK;
            this.setPos(
                startX + (tx - startX) * t,
                startY + (ty - startY) * t,
                startZ + (tz - startZ) * t
            );
        }

        if (!level().isClientSide && tickCount == IMPACT_TICK && !hasImpacted) {
            hasImpacted = true;
            BlockPos impactPos = new BlockPos(Mth.floor(tx), Mth.floor(ty), Mth.floor(tz));
            AsteroidCraterGenerator.impactAt((ServerLevel) level(), impactPos);
        }

        if (!level().isClientSide && tickCount >= DESPAWN_TICK) {
            this.discard();
        }
    }

    // GeckoLib
    private static final RawAnimation SPIN = RawAnimation.begin().thenLoop("animation.asteroid.spin");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "spin", 0,
            state -> state.setAndContinue(SPIN)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // Entity#addAdditionalSaveData is protected in Entity — must stay protected
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("StartX", startX);
        tag.putDouble("StartY", startY);
        tag.putDouble("StartZ", startZ);
        tag.putFloat("TargetX", entityData.get(TARGET_X));
        tag.putFloat("TargetY", entityData.get(TARGET_Y));
        tag.putFloat("TargetZ", entityData.get(TARGET_Z));
        tag.putBoolean("HasImpacted", hasImpacted);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        startX = tag.getDouble("StartX");
        startY = tag.getDouble("StartY");
        startZ = tag.getDouble("StartZ");
        entityData.set(TARGET_X, tag.getFloat("TargetX"));
        entityData.set(TARGET_Y, tag.getFloat("TargetY"));
        entityData.set(TARGET_Z, tag.getFloat("TargetZ"));
        hasImpacted = tag.getBoolean("HasImpacted");
    }
}