package com.uheroes.mod.core.flux;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provider for Neural Flux capability.
 */
public class FluxProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    private final NeuralFlux flux = new NeuralFlux();
    private final LazyOptional<NeuralFlux> optional = LazyOptional.of(() -> flux);
    
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == FluxCapability.INSTANCE ? optional.cast() : LazyOptional.empty();
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        flux.saveNBT(tag);
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag tag) {
        flux.loadNBT(tag);
    }
    
    public void invalidateCaps() {
        optional.invalidate();
    }
}
