package com.exoticmoss.simplearmours.gravity;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

// Each tier is a stage in a star's life. Only the data changes between tiers, the logic is shared.
public enum GravityCoreTier implements StringRepresentable {
    // name, color, orbitRadius, orbitSpeed, maxStacks, craftTime, captureRadius
    SUN("sun", 0xFFB43C, 1.2F, 2.0F, 4, 100, 2.5),
    RED_GIANT("red_giant", 0xC8281E, 1.0F, 3.5F, 8, 80, 3.0),
    NEUTRON_STAR("neutron_star", 0x8CC8FF, 0.8F, 9.0F, 12, 60, 3.5),
    BLACK_HOLE("black_hole", 0x9B30FF, 1.4F, 6.0F, 16, 40, 4.0);

    public static final Codec<GravityCoreTier> CODEC = StringRepresentable.fromEnum(GravityCoreTier::values);

    private final String name;
    private final int color;
    private final float orbitRadius;
    // Degrees per tick
    private final float orbitSpeed;
    private final int maxStacks;
    private final int craftTime;
    private final double captureRadius;

    GravityCoreTier(String name, int color, float orbitRadius, float orbitSpeed, int maxStacks, int craftTime, double captureRadius) {
        this.name = name;
        this.color = color;
        this.orbitRadius = orbitRadius;
        this.orbitSpeed = orbitSpeed;
        this.maxStacks = maxStacks;
        this.craftTime = craftTime;
        this.captureRadius = captureRadius;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int color() {
        return color;
    }

    public float orbitRadius() {
        return orbitRadius;
    }

    public float orbitSpeed() {
        return orbitSpeed;
    }

    public int maxStacks() {
        return maxStacks;
    }

    public int craftTime() {
        return craftTime;
    }

    public double captureRadius() {
        return captureRadius;
    }
}
