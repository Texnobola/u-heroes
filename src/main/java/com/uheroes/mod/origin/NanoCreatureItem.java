package com.uheroes.mod.origin;

import com.uheroes.mod.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class NanoCreatureItem extends Item {
    public NanoCreatureItem(Properties props) { super(props); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position().add(player.getLookAngle().scale(2.0));
            BlockPos blockPos = BlockPos.containing(pos);

            NanoCreatureEntity entity = new NanoCreatureEntity(
                ModEntities.NANO_CREATURE.get(), serverLevel);
            entity.moveTo(blockPos.getX() + 0.5, blockPos.getY(),
                blockPos.getZ() + 0.5, player.getYRot(), 0);
            serverLevel.addFreshEntity(entity);
            player.getItemInHand(hand).shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.u_heroes.nano_creature.tooltip"));
    }
}