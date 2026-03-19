package com.uheroes.mod.heroes.nanotech.ava;

import com.uheroes.mod.core.network.AVAVfxPacket;
import com.uheroes.mod.core.network.ModNetwork;
import com.uheroes.mod.init.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * AVA's energy blaster bolt.
 * Fast, no-gravity, semi-transparent cyan pulse shot at enemies.
 * Damage: 8.0, range: 20 blocks, despawn after 40 ticks if no hit.
 */
public class AVABlasterEntity extends Projectile {

    private static final float DAMAGE   = 8.0f;
    private static final int   LIFETIME = 40; // ticks before despawn

    @Nullable private LivingEntity shooter;

    public AVABlasterEntity(EntityType<? extends AVABlasterEntity> type, Level level) {
        super(type, level);
    }

    public AVABlasterEntity(Level level, AVAEntity ava, LivingEntity target) {
        super(ModEntities.AVA_BLASTER.get(), level);
        this.shooter = target; // store target for reference (owner is ava)
        setOwner(ava);
        setPos(ava.getX(), ava.getEyeY(), ava.getZ());
        setNoGravity(true);

        Vec3 dir = target.getEyePosition().subtract(position()).normalize();
        setDeltaMovement(dir.scale(1.8)); // fast bolt
    }

    @Override
    protected void defineSynchedData() { }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state,
                                   net.minecraft.core.BlockPos pos) { }

    @Override
    public void tick() {
        super.tick();

        // Despawn after lifetime
        if (tickCount > LIFETIME) { discard(); return; }

        // Apply movement each tick
        move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());

        // Hit detection
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this,
            e -> e != getOwner() && e.isAlive() && e instanceof LivingEntity);

        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        if (level().isClientSide) return;

        Vec3 pos = hit.getLocation();
        Vec3 dir = getDeltaMovement().normalize();

        // Send VFX to nearby clients
        ModNetwork.sendToAllTracking(
            new AVAVfxPacket(AVAVfxPacket.Type.BLASTER_HIT,
                (float) pos.x, (float) pos.y, (float) pos.z,
                (float) dir.x, (float) dir.y, (float) dir.z),
            this);

        // Damage entity hit
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity target) {
            if (getOwner() instanceof AVAEntity ava) {
                target.hurt(level().damageSources().generic(), DAMAGE);
                target.setDeltaMovement(target.getDeltaMovement().add(dir.scale(0.5)));
                target.hurtMarked = true;
            }
        }

        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) { discard(); }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) { }
    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) { }
}