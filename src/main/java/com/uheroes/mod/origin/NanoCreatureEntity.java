package com.uheroes.mod.origin;

import com.uheroes.mod.UHeroesMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NanoCreatureEntity extends PathfinderMob implements GeoEntity {

    private static final RawAnimation IDLE  = RawAnimation.begin().thenLoop("animation.nano_creature.idle");
    private static final RawAnimation MOVE  = RawAnimation.begin().thenLoop("animation.nano_creature.move");
    private static final RawAnimation MERGE = RawAnimation.begin().thenPlay("animation.nano_creature.merge");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean merging = false;
    private int mergeTick   = 0;
    private static final int MERGE_DURATION = 60;

    public NanoCreatureEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new FollowMobGoal(this, 1.0, 3.0f, 7.0f));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && merging) {
            mergeTick++;
            if (mergeTick >= MERGE_DURATION) {
                // Find nearest player and grant suit
                Player nearest = this.level().getNearestPlayer(this, 3.0);
                if (nearest instanceof ServerPlayer sp) {
                    NanoCreatureHandler.grantNanoSuit(sp);
                }
                this.discard();
            }
        }

        // Auto-merge when touching a player
        if (!this.level().isClientSide && !merging) {
            Player nearest = this.level().getNearestPlayer(this, 1.5);
            if (nearest != null) {
                merging = true;
                mergeTick = 0;
            }
        }
    }

    public boolean isMerging() { return merging; }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "main", 4, state -> {
            if (merging) return state.setAndContinue(MERGE);
            if (state.isMoving()) return state.setAndContinue(MOVE);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
