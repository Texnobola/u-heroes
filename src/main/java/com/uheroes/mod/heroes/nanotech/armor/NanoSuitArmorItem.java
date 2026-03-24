package com.uheroes.mod.heroes.nanotech.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import top.theillusivec4.caelus.api.CaelusApi;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.uheroes.mod.client.renderer.NanoSuitArmorRenderer;

import java.util.List;
import java.util.function.Consumer;

/**
 * Nano-Tech Suit armor piece with GeckoLib animations.
 */
public class NanoSuitArmorItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    public NanoSuitArmorItem(Type type, Properties properties) {
        super(new NanoSuitMaterial(), type, properties);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.u_heroes.nano_suit_tag"));
        tooltip.add(Component.translatable("tooltip.u_heroes.flux_powered"));
        
        switch (this.getType()) {
            case HELMET -> tooltip.add(Component.translatable("tooltip.u_heroes.helmet_feature"));
            case CHESTPLATE -> tooltip.add(Component.translatable("tooltip.u_heroes.chestplate_feature"));
            case LEGGINGS -> tooltip.add(Component.translatable("tooltip.u_heroes.leggings_feature"));
            case BOOTS -> tooltip.add(Component.translatable("tooltip.u_heroes.boots_feature"));
        }
        
        tooltip.add(Component.translatable("tooltip.u_heroes.full_set_hint"));
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
            private NanoSuitArmorRenderer renderer;
            
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (renderer == null) {
                    renderer = new NanoSuitArmorRenderer();
                }
                renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                renderer.setBaseModel(original);
                return renderer;
            }
        });
    }
}