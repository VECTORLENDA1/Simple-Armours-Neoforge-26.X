package com.exoticmoss.simplearmours.block;

import com.exoticmoss.simplearmours.SimpleArmours;
import com.exoticmoss.simplearmours.gravity.GravityCoreBlock;
import com.exoticmoss.simplearmours.gravity.GravityCoreTier;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SimpleArmours.MODID);

    public static final DeferredBlock<GravityCoreBlock> SUN_CORE = BLOCKS.registerBlock("sun_core",
            properties -> new GravityCoreBlock(GravityCoreTier.SUN, properties),
            properties -> properties
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.0F, 6.0F)
                    .lightLevel(state -> 15)
                    .sound(SoundType.AMETHYST)
                    .noOcclusion());

    private ModBlocks() {
    }
}
