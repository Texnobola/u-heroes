package com.uheroes.mod.heroes.nanotech.ava;

import com.uheroes.mod.event.AVAEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * AVA in item form.
 * Hold right-click  → shield block (UseAnim.BLOCK + isShield=true)
 * Shift+right-click → deploys AVA as entity and consumes this item
 */
public class AVAShieldItem extends Item {

    public AVAShieldItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && player instanceof ServerPlayer sp
                    && level instanceof ServerLevel sl) {
                player.getCapability(AVACapability.INSTANCE).ifPresent(ava -> {
                    if (!ava.hasAva()) {
                        AVAEvents.spawnAVA(player, sl, ava);
                        if (!player.getAbilities().instabuild) stack.shrink(1);
                    } else {
                        player.displayClientMessage(
                            Component.translatable("status.u_heroes.ava_online"), true);
                    }
                });
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BLOCK; }
    @Override public int getUseDuration(ItemStack stack) { return 72000; }
    public boolean isShield(ItemStack stack, @Nullable net.minecraft.world.entity.LivingEntity entity) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.u_heroes.ava_item_deploy"));
        tooltip.add(Component.translatable("tooltip.u_heroes.ava_item_block"));
    }
}