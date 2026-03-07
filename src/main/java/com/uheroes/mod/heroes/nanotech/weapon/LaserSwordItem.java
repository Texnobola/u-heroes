package com.uheroes.mod.heroes.nanotech.weapon;

import com.uheroes.mod.core.flux.FluxCapability;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * Nano-Tech Laser Sword with switchable modes.
 */
public class LaserSwordItem extends SwordItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final String NBT_MODE = "SwordMode";
    private static final int MODE_SWITCH_COST = 5;
    
    public LaserSwordItem(Properties properties) {
        super(NanoTechToolTier.NANO_TECH, 10, -2.2f, properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player.isCrouching()) {
            if (FluxCapability.consume(player, MODE_SWITCH_COST)) {
                LaserSwordMode currentMode = getMode(stack);
                LaserSwordMode newMode = currentMode.next();
                setMode(stack, newMode);
                
                String modeKey = newMode == LaserSwordMode.LASER ? 
                    "tooltip.u_heroes.laser_mode" : "tooltip.u_heroes.inferno_edge_mode";
                player.displayClientMessage(Component.translatable("status.u_heroes.mode_switched",
                    Component.translatable(modeKey)), true);
                
                return InteractionResultHolder.success(stack);
            } else {
                player.displayClientMessage(Component.translatable("status.u_heroes.insufficient_flux"), true);
                return InteractionResultHolder.fail(stack);
            }
        }
        
        return InteractionResultHolder.pass(stack);
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        LaserSwordMode mode = getMode(stack);
        
        if (attacker.level() instanceof ServerLevel serverLevel) {
            if (mode == LaserSwordMode.LASER) {
                // Cyan sparks + Weakness
                for (int i = 0; i < 10; i++) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                        1, 0.3, 0.3, 0.3, 0.1);
                }
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
            } else {
                // Fire + flame particles + bonus damage
                target.setSecondsOnFire(5);
                target.hurt(attacker.damageSources().onFire(), mode.getBonusDamage());
                
                for (int i = 0; i < 15; i++) {
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                        target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                        1, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
        
        return super.hurtEnemy(stack, target, attacker);
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        LaserSwordMode mode = getMode(stack);
        String modeKey = mode == LaserSwordMode.LASER ? 
            "tooltip.u_heroes.laser_mode" : "tooltip.u_heroes.inferno_edge_mode";
        
        tooltip.add(Component.translatable("tooltip.u_heroes.mode_label", 
            Component.translatable(modeKey)));
        
        if (mode == LaserSwordMode.LASER) {
            tooltip.add(Component.translatable("tooltip.u_heroes.laser_desc"));
        } else {
            tooltip.add(Component.translatable("tooltip.u_heroes.inferno_desc"));
        }
        
        tooltip.add(Component.translatable("tooltip.u_heroes.mode_switch_hint", MODE_SWITCH_COST));
    }
    
    public static LaserSwordMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(NBT_MODE)) {
            return LaserSwordMode.fromNBT(tag.getString(NBT_MODE));
        }
        return LaserSwordMode.LASER;
    }
    
    public static void setMode(ItemStack stack, LaserSwordMode mode) {
        stack.getOrCreateTag().putString(NBT_MODE, mode.name());
    }
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // TODO: add animations in Phase 3.3 / 4.1
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private LaserSwordRenderer renderer;
            
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new LaserSwordRenderer();
                }
                return renderer;
            }
        });
    }
}
