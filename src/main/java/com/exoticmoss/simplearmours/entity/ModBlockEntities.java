package com.exoticmoss.simplearmours.entity;

import com.exoticmoss.simplearmours.SimpleArmours;
import com.exoticmoss.simplearmours.block.ModBlocks;
import com.exoticmoss.simplearmours.gravity.GravityCoreBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SimpleArmours.MODID);

    // One type shared by every tier; add each new tier's block to the valid blocks here
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GravityCoreBlockEntity>> GRAVITY_CORE = BLOCK_ENTITY_TYPES.register("gravity_core",
            () -> new BlockEntityType<>(GravityCoreBlockEntity::new, ModBlocks.SUN_CORE.get()));

    private ModBlockEntities() {
    }
}
