package com.uheroes.mod.heroes.nanotech.weapon;

/**
 * Modes for the Nano-Tech Laser Sword.
 */
public enum LaserSwordMode {
    LASER(0xFF00FFFF, 0),
    INFERNO_EDGE(0xFFFF4500, 5);
    
    private final int color;
    private final float bonusDamage;
    
    LaserSwordMode(int color, float bonusDamage) {
        this.color = color;
        this.bonusDamage = bonusDamage;
    }
    
    public int getColor() {
        return color;
    }
    
    public float getBonusDamage() {
        return bonusDamage;
    }
    
    public LaserSwordMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
    
    public static LaserSwordMode fromNBT(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return LASER;
        }
    }
}
