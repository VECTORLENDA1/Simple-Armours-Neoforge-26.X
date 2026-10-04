package com.exoticmoss.simplearmours.Item;

import com.exoticmoss.simplearmours.SimpleArmours;

import com.exoticmoss.simplearmours.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SimpleArmours.MODID);

    public static final DeferredItem<BlockItem> SUN_CORE = ITEMS.registerSimpleBlockItem(ModBlocks.SUN_CORE);

    private ModItems() {
    }
}
