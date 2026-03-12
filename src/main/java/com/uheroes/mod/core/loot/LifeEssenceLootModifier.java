package com.uheroes.mod.core.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.uheroes.mod.init.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class LifeEssenceLootModifier extends LootModifier {

    public static final Supplier<Codec<LifeEssenceLootModifier>> CODEC = Suppliers.memoize(() ->
        RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, LifeEssenceLootModifier::new))
    );

    private static final float DROP_CHANCE = 0.02f; // 2%

    public LifeEssenceLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Only apply when breaking leaves blocks
        var blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (blockState == null) return generatedLoot;
        if (!(blockState.getBlock() instanceof LeavesBlock)) return generatedLoot;

        // 2% chance to drop life essence
        if (context.getRandom().nextFloat() < DROP_CHANCE) {
            generatedLoot.add(new ItemStack(ModItems.LIFE_ESSENCE.get(), 1));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}