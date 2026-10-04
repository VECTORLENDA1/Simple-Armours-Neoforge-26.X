package com.exoticmoss.simplearmours.client;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.exoticmoss.simplearmours.gravity.GravityCoreBlockEntity;
import com.exoticmoss.simplearmours.gravity.GravityCoreTier;
import com.exoticmoss.simplearmours.gravity.OrbitingItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Draws the items held by a core as a tilted ring spinning around it. Positions come from game time,
// so the server never has to move anything.
public class GravityCoreRenderer implements BlockEntityRenderer<GravityCoreBlockEntity, GravityCoreRenderer.State> {
    private static final float RING_TILT = 15.0F;
    private static final float ITEM_SPIN_SPEED = 4.0F;

    private final ItemModelResolver itemModelResolver;

    public GravityCoreRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GravityCoreBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        GravityCoreTier tier = blockEntity.getTier();
        state.time = blockEntity.getLevel() == null ? 0.0F : blockEntity.getLevel().getGameTime() + partialTicks;
        state.orbitRadius = tier.orbitRadius();
        state.orbitSpeed = tier.orbitSpeed();

        int seed = (int) blockEntity.getBlockPos().asLong();
        List<OrbitingItem> orbit = blockEntity.getOrbit();
        state.items = new ArrayList<>(orbit.size());
        for (int i = 0; i < orbit.size(); i++) {
            ItemStackRenderState itemState = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(itemState, orbit.get(i).stack(), ItemDisplayContext.GROUND, blockEntity.getLevel(), null, seed + i);
            state.items.add(itemState);
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        int count = state.items.size();
        for (int i = 0; i < count; i++) {
            ItemStackRenderState itemState = state.items.get(i);
            if (itemState.isEmpty()) {
                continue;
            }
            float angle = state.time * state.orbitSpeed + 360.0F * i / count;
            // Small vertical wobble so the ring feels alive rather than rigid
            float bob = Mth.sin((state.time + i * 20.0F) / 10.0F) * 0.05F;

            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(RING_TILT));
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            poseStack.translate(state.orbitRadius, bob - 0.125F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.time * ITEM_SPIN_SPEED));
            itemState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    // The ring reaches outside the block, so widen the culling box or items vanish at screen edges
    @Override
    public AABB getRenderBoundingBox(GravityCoreBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(blockEntity.getTier().orbitRadius() + 0.5);
    }

    public static class State extends BlockEntityRenderState {
        List<ItemStackRenderState> items = List.of();
        float time;
        float orbitRadius;
        float orbitSpeed;
    }
}
