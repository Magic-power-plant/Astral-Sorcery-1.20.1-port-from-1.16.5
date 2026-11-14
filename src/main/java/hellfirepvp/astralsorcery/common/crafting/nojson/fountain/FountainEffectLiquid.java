/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.fountain;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.function.RefreshFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXSpritePlane;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.CapabilitiesAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.blockentity.FountainBlockEntity;
import hellfirepvp.astralsorcery.common.util.BlockDropCaptureAssist;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FountainEffectLiquid
 * Created by HellFirePvP
 * Date: 01.11.2020 / 11:23
 */
public class FountainEffectLiquid extends FountainEffect<LiquidContext> {

    public FountainEffectLiquid() {
        super(AstralSorcery.key("effect_liquid"));
    }

    @Nonnull
    @Override
    public Block getAssociatedPrime() {
        return BlocksAS.FOUNTAIN_PRIME_LIQUID.get();
    }

    @Nonnull
    @Override
    public LiquidContext createContext(FountainBlockEntity fountain) {
        return new LiquidContext(fountain.getBlockPos());
    }

    @Override
    public void tick(FountainBlockEntity fountain, LiquidContext ctx, int operationTick, LogicalSide side, OperationSegment currentSegment) {
        if (side.isClient()) {
            tickEffects(fountain, ctx, operationTick, currentSegment);
            return;
        }

        if (currentSegment.isLaterOrEqualTo(OperationSegment.RUNNING)) {
            Level w = fountain.getLevel();
            //TODO_PORT[AS-FNT-04]: 1.16.5 reads 'fountain.getTicksExisted() % 32' here (1.16.5 FountainEffectLiquid.java:81).
            // The target TileFountain extends TileEntitySynchronized instead of TileEntityTick, so getTicksExisted() does not
            // exist yet; the tile's active-effect counter is the only monotone per-tile tick counter available and keeps the
            // same 32-tick cadence. Revert to getTicksExisted() once TileFountain extends TileEntityTick again.
            if (fountain.getTickActiveFountainEffect() % 32 == 0) {
                digCone(w, ctx);
            }

            if (ctx.tickLiquidProduction()) {
                ctx.resetLiquidProductionTick(rand);
                produceLiquid(fountain);
            }
        }
    }

    private void produceLiquid(FountainBlockEntity fountain) {
        LevelChunk ch = fountain.getLevel().getChunkAt(fountain.getBlockPos());
        ch.getCapability(CapabilitiesAS.CHUNK_FLUID).ifPresent(entry -> {
            int drain = 200 + rand.nextInt(400);
            FluidStack drained;
            // ChunkFluidEntry in this tree takes a boolean 'execute' flag instead of IFluidHandler.FluidAction
            if (!entry.isEmpty() && entry.isInitialized()) {
                drained = entry.drain(drain, false);
            } else {
                drained = new FluidStack(Fluids.WATER, drain);
            }

            int fillable = fountain.getTank().fill(drained, IFluidHandler.FluidAction.SIMULATE);
            if (fillable > 0) {
                FluidStack actual = entry.drain(fillable, true);
                fountain.getTank().fill(actual, IFluidHandler.FluidAction.EXECUTE);
            }
        });
    }

    private void digCone(Level world, LiquidContext ctx) {
        if (world instanceof ServerLevel) {
            dig((ServerLevel) world, ctx.getDigPositions());
        }
    }

    private void dig(ServerLevel world, List<BlockPos> positions) {
        BlockDropCaptureAssist.startCapturing();
        try {
            positions.forEach(pos -> {
                MiscUtils.executeWithChunk(world, pos, () -> {
                    BlockState state = world.getBlockState(pos);
                    // 1.16.5 BlockState#isAir(IBlockReader, BlockPos) collapsed into the stateless isAir() in 1.20.1
                    if (!state.isAir() &&
                            world.getBlockEntity(pos) == null &&
                            state.getDestroySpeed(world, pos) >= 0 &&
                            !BlockUtils.isFluidBlock(state)) {
                        BlockUtils.breakBlockWithoutPlayer(world, pos, state, ItemStack.EMPTY, true, true, false);
                    }
                });
            });
        } finally {
            BlockDropCaptureAssist.getCapturedStacksAndStop();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickEffects(FountainBlockEntity fountain, LiquidContext ctx, int operationTick, OperationSegment currentSegment) {
        if (currentSegment.isLaterOrEqualTo(OperationSegment.STARTUP)) {
            FXSpritePlane sprite = (FXSpritePlane) ctx.fountainSprite;
            if (sprite == null) {
                sprite = EffectHelper.of(EffectTemplatesAS.TEXTURE_SPRITE)
                        .spawn(new Vector3(fountain).add(0.5, 0.5, 0.5))
                        .setAxis(Vector3.RotAxis.Y_AXIS)
                        .setNoRotation(45)
                        .setSprite(SpritesAS.SPR_FOUNTAIN_LIQUID)
                        .setAlphaMultiplier(1F)
                        .alpha((fx, alphaIn, pTicks) -> this.getSegmentPercent(OperationSegment.STARTUP, fountain.getTickActiveFountainEffect()))
                        .setScaleMultiplier(5.5F)
                        //AS-FNT-05 (resolved): the note here claimed TileFountain did not track a currentEffect
                        // yet. It does - common/tile/TileFountain.java:60 declares it and getCurrentEffect() has
                        // six call sites - so 1.16.5's identity check is restored
                        // (1.16.5 FountainEffectLiquid.java:149). Without it the sprite kept refreshing after this
                        // effect ended or was swapped for another one.
                        .refresh(RefreshFunction.blockEntityExistsAnd(fountain, (blockEntity, fx) -> blockEntity.getCurrentEffect() == this));
            } else if (sprite.isRemoved() || sprite.canRemove()) {
                EffectHelper.refresh(sprite, EffectTemplatesAS.TEXTURE_SPRITE);
            }
            ctx.fountainSprite = sprite;
        }

        BlockPos fountainPos = fountain.getBlockPos();
        float segmentPercent = getSegmentPercent(currentSegment, operationTick);
        switch (currentSegment) {
            case STARTUP:
                playFountainVortexParticles(fountainPos, segmentPercent);
                playFountainArcs(fountainPos, segmentPercent);
                break;
            case PREPARATION:
                playFountainArcs(fountainPos, 1F - segmentPercent);
                playFountainVortexParticles(fountainPos, 1F - segmentPercent);
                playDigPreparation(fountainPos, segmentPercent);
                break;
            case RUNNING:
                playFountainVortexParticles(fountainPos, 0.2F);
                playFountainArcs(fountainPos, 0.6F);
                playDigParticles(fountainPos);
                //TODO_PORT[AS-FNT-04]: 1.16.5 reads 'fountain.getTicksExisted() % 40' here (1.16.5 FountainEffectLiquid.java:172).
                // Same substitution as above: TileFountain has no getTicksExisted() in the target tree yet.
                if (fountain.getTickActiveFountainEffect() % 40 == 0) {
                    playDigLightbeam(fountainPos);
                }
                break;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playDigPreparation(Vec3i pos, float chance) {
        Vector3 at = new Vector3(pos).add(0.5, 0.5, 0.5);
        for (int i = 0; i < 12; i++) {
            if (rand.nextFloat() >= chance) {
                continue;
            }
            Vector3 particlePos = new Vector3(
                    pos.getX() - 0.4 + rand.nextFloat() * 1.8,
                    pos.getY()       - rand.nextFloat() * 3,
                    pos.getZ() - 0.4 + rand.nextFloat() * 1.8
            );
            Vector3 motion = particlePos.clone().vectorFromHereTo(at).normalize().divide(30);

            EntityVisualFX fx = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(particlePos)
                    .setMotion(motion)
                    .setAlphaMultiplier(1F)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.2F + rand.nextFloat() * 0.1F)
                    .setMaxAge(20 + rand.nextInt(40));
            if (rand.nextBoolean()) {
                fx.color(VFXColorFunction.WHITE);
            } else {
                fx.color(VFXColorFunction.constant(ColorsAS.DEFAULT_GENERIC_PARTICLE));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playDigParticles(Vec3i pos) {
        for (int i = 0; i < 2; i++) {
            Vector3 at = new Vector3(pos).add(
                    0.3 + rand.nextFloat() * 0.4,
                    -rand.nextFloat() * 1.7,
                    0.3 + rand.nextFloat() * 0.4);
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .setScaleMultiplier(0.25F)
                    .setAlphaMultiplier(1F)
                    .setMotion(new Vector3(0, -rand.nextFloat() * 0.008F, 0))
                    .color(VFXColorFunction.random());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playDigLightbeam(Vec3i pos) {
        Vector3 from = new Vector3(pos).add(0.5, 1.5, 0.5);
        MiscUtils.applyRandomOffset(from, rand, 0.1F);
        Vector3 to = from.clone().setY(0);

        float size = 6 + rand.nextFloat() * 2;
        EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                .spawn(from)
                .setup(to, size, size);
    }

    @Override
    public void transition(FountainBlockEntity fountain, LiquidContext ctx, LogicalSide side, OperationSegment prevSegment, OperationSegment nextSegment) {
        if (side.isServer()) {
            if (nextSegment == OperationSegment.RUNNING) {
                digCone(fountain.getLevel(), ctx);
            }
        } else {
            if (nextSegment == OperationSegment.RUNNING) {
                markDigProcess(fountain.getBlockPos());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void markDigProcess(BlockPos pos) {
        for (int yy = 0; yy <= pos.getY(); yy++) {
            for (int i = 0; i < 4; i++) {
                Vector3 at = new Vector3(pos).setY(yy).add(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .setAlphaMultiplier(1F)
                        .alpha(VFXAlphaFunction.FADE_OUT)
                        .setScaleMultiplier(0.2F + rand.nextFloat() * 0.1F)
                        .setMaxAge(20 + rand.nextInt(40));
            }
        }

        Vector3 from = new Vector3(pos).add(0.5, 0.5, 0.5);
        Vector3 to = from.clone().setY(0);
        EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                .spawn(from)
                .setup(to, 1.5, 1.5)
                .setAlphaMultiplier(1F)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.constant(ColorsAS.EFFECT_BLUE_LIGHT));
    }

    @Override
    public void onReplace(FountainBlockEntity fountain, LiquidContext ctx, @Nullable FountainEffect<?> newEffect, LogicalSide side) {
        if (side.isClient()) {
            removeSprite(ctx);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void removeSprite(LiquidContext ctx) {
        FXSpritePlane sprite = (FXSpritePlane) ctx.fountainSprite;
        if (sprite != null) {
            sprite.requestRemoval();
        }
    }
}
