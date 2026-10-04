package com.exoticmoss.simplearmours.gravity;

import com.exoticmoss.simplearmours.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class GravityCoreBlock extends BaseEntityBlock {
    public static final MapCodec<GravityCoreBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GravityCoreTier.CODEC.fieldOf("tier").forGetter(GravityCoreBlock::getTier),
            propertiesCodec()
    ).apply(instance, GravityCoreBlock::new));

    private static final VoxelShape SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);

    private final GravityCoreTier tier;

    public GravityCoreBlock(GravityCoreTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public GravityCoreTier getTier() {
        return tier;
    }

    @Override
    protected MapCodec<GravityCoreBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // Until recipes exist, an empty-hand click sends everything in orbit back to whoever threw it.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof GravityCoreBlockEntity core) {
            core.releaseAll(serverLevel);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravityCoreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel ? createTickerHelper(type, ModBlockEntities.GRAVITY_CORE.get(), GravityCoreBlockEntity::serverTick) : null;
    }
}
