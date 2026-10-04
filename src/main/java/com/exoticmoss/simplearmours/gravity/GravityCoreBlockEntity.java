package com.exoticmoss.simplearmours.gravity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.exoticmoss.simplearmours.entity.ModBlockEntities;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

// Items are not simulated while orbiting: they are pulled out of the world and stored here, and the
// orbit is purely visual (see GravityCoreRenderer). That keeps "gravity" from ever touching entities or blocks.
public class GravityCoreBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<List<OrbitingItem>> ORBIT_CODEC = OrbitingItem.CODEC.listOf();

    private final List<OrbitingItem> orbit = new ArrayList<>();
    // Items this core just spat out, so it does not swallow them again straight away. Not saved.
    private final Set<UUID> ejected = new HashSet<>();

    public GravityCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAVITY_CORE.get(), pos, state);
    }

    public GravityCoreTier getTier() {
        return getBlockState().getBlock() instanceof GravityCoreBlock core ? core.getTier() : GravityCoreTier.SUN;
    }

    public List<OrbitingItem> getOrbit() {
        return Collections.unmodifiableList(orbit);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GravityCoreBlockEntity core) {
        core.captureNearbyItems((ServerLevel) level);
    }

    private void captureNearbyItems(ServerLevel level) {
        GravityCoreTier tier = getTier();
        AABB area = new AABB(worldPosition).inflate(tier.captureRadius());
        List<ItemEntity> nearby = level.getEntitiesOfClass(ItemEntity.class, area, Entity::isAlive);

        // Forget ejected items once they have left the area or been picked up
        Set<UUID> nearbyIds = nearby.stream().map(Entity::getUUID).collect(Collectors.toSet());
        ejected.retainAll(nearbyIds);

        boolean captured = false;
        for (ItemEntity item : nearby) {
            if (orbit.size() >= tier.maxStacks()) {
                break;
            }
            if (ejected.contains(item.getUUID())) {
                continue;
            }
            UUID owner = item.getOwner() instanceof Player player ? player.getUUID() : null;
            orbit.add(new OrbitingItem(item.getItem().copy(), Optional.ofNullable(owner)));
            item.discard();
            captured = true;
        }

        if (captured) {
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.6F);
            markUpdated();
        }
    }

    public void releaseAll(ServerLevel level) {
        if (orbit.isEmpty()) {
            return;
        }
        for (OrbitingItem entry : orbit) {
            returnToOwner(level, entry.stack().copy(), entry.owner().orElse(null));
        }
        orbit.clear();
        markUpdated();
    }

    // Sends a stack straight into the owner's inventory. Whatever does not fit is dropped at their feet;
    // with no owner online (or a machine as the source) it pops out above the core instead.
    private void returnToOwner(ServerLevel level, ItemStack stack, @Nullable UUID ownerId) {
        Player owner = ownerId == null ? null : level.getPlayerByUUID(ownerId);
        if (owner != null) {
            owner.getInventory().add(stack);
            level.playSound(null, owner.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.4F, 1.4F);
            if (stack.isEmpty()) {
                return;
            }
        }

        ItemEntity drop;
        if (owner != null) {
            drop = new ItemEntity(level, owner.getX(), owner.getY(), owner.getZ(), stack, 0.0, 0.0, 0.0);
            drop.setNoPickUpDelay();
        } else {
            drop = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, stack, 0.0, 0.25, 0.0);
            drop.setDefaultPickUpDelay();
        }
        ejected.add(drop.getUUID());
        level.addFreshEntity(drop);
    }

    // Breaking the core gives everything back instead of scattering it
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            releaseAll(serverLevel);
        }
    }

    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        orbit.clear();
        input.read("Orbit", ORBIT_CODEC).ifPresent(orbit::addAll);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Orbit", ORBIT_CODEC, orbit);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            output.store("Orbit", ORBIT_CODEC, orbit);
            return output.buildResult();
        }
    }
}
