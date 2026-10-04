package com.exoticmoss.simplearmours.gravity;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;

// A stack held in a core's orbit, plus the player who threw it (empty for droppers and other machines).
public record OrbitingItem(ItemStack stack, Optional<UUID> owner) {
    public static final Codec<OrbitingItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(OrbitingItem::stack),
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(OrbitingItem::owner)
    ).apply(instance, OrbitingItem::new));
}
