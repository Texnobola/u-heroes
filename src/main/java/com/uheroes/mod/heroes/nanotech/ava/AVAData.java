package com.uheroes.mod.heroes.nanotech.ava;

import net.minecraft.nbt.CompoundTag;

import java.util.Optional;
import java.util.UUID;

/**
 * Player capability that persists the AVA entity link across sessions,
 * deaths, and dimension changes.
 *
 * <p>Stored alongside {@code NeuralFlux} in the player's capability set.
 * Saved/loaded via NBT, copied on death exactly like NeuralFlux.
 */
public class AVAData {

    // NBT keys
    public static final String NBT_AVA_UUID       = "AVALinkedUUID";
    public static final String NBT_RESPAWN_COOL   = "AVARespawnCooldown";
    public static final String NBT_SHIELD_ACTIVE  = "AVAShieldKeyHeld";

    /** Ticks a player must wait before AVA respawns after forced removal. */
    public static final int RESPAWN_COOLDOWN = 600; // 30 s

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<UUID> avaUUID     = Optional.empty();
    private int            respawnCool = 0;
    private boolean        shieldHeld  = false;

    // ─── UUID link ────────────────────────────────────────────────────────────

    public Optional<UUID> getAvaUUID()         { return avaUUID; }
    public void           setAvaUUID(UUID id)  { avaUUID = Optional.ofNullable(id); }
    public void           clearAvaUUID()       { avaUUID = Optional.empty(); }
    public boolean        hasAva()             { return avaUUID.isPresent(); }

    // ─── Respawn cooldown ────────────────────────────────────────────────────

    public void startRespawnCooldown()  { respawnCool = RESPAWN_COOLDOWN; }
    public boolean isOnCooldown()       { return respawnCool > 0; }
    public void tickCooldown()          { if (respawnCool > 0) respawnCool--; }
    public int  getRespawnCool()        { return respawnCool; }

    // ─── Shield key state ────────────────────────────────────────────────────

    public boolean isShieldHeld()           { return shieldHeld; }
    public void    setShieldHeld(boolean v) { shieldHeld = v; }

    // ─── NBT ─────────────────────────────────────────────────────────────────

    public void saveNBT(CompoundTag tag) {
        avaUUID.ifPresent(id -> tag.putUUID(NBT_AVA_UUID, id));
        tag.putInt(NBT_RESPAWN_COOL, respawnCool);
        tag.putBoolean(NBT_SHIELD_ACTIVE, shieldHeld);
    }

    public void loadNBT(CompoundTag tag) {
        avaUUID     = tag.hasUUID(NBT_AVA_UUID) ? Optional.of(tag.getUUID(NBT_AVA_UUID)) : Optional.empty();
        respawnCool = tag.contains(NBT_RESPAWN_COOL) ? tag.getInt(NBT_RESPAWN_COOL) : 0;
        shieldHeld  = tag.contains(NBT_SHIELD_ACTIVE) && tag.getBoolean(NBT_SHIELD_ACTIVE);
    }

    public void copyFrom(AVAData src) {
        this.avaUUID     = src.avaUUID;
        this.respawnCool = src.respawnCool;
        this.shieldHeld  = src.shieldHeld;
    }
}