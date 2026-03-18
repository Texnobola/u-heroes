package com.uheroes.mod.heroes.nanotech.ava;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Forge capability holding {@link AVAData} on every player entity.
 *
 * <p>Registration pattern mirrors {@code FluxCapability} exactly — registered
 * in {@code FluxEvents.onRegisterCapabilities} and attached in
 * {@code FluxEvents.onAttachCapabilities}.
 */
public class AVACapability {

    public static final Capability<AVAData> INSTANCE =
        CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation ID =
        new ResourceLocation("u_heroes", "ava_data");

    // ─── Static helpers ───────────────────────────────────────────────────────

    public static LazyOptional<AVAData> get(Player player) {
        return player.getCapability(INSTANCE);
    }

    /** Read-only shortcut — returns empty AVAData if cap absent. */
    public static AVAData getOrEmpty(Player player) {
        return get(player).orElse(new AVAData());
    }

    // ─── Provider ─────────────────────────────────────────────────────────────

    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        private final AVAData data = new AVAData();
        private final LazyOptional<AVAData> optional = LazyOptional.of(() -> data);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == INSTANCE ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            data.saveNBT(tag);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            data.loadNBT(tag);
        }

        public void invalidateCaps() {
            optional.invalidate();
        }
    }
}