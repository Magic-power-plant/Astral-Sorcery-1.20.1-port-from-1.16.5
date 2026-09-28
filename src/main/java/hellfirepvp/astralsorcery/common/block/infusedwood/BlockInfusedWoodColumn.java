/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.infusedwood;

import hellfirepvp.astralsorcery.common.block.base.template.BlockInfusedWoodTemplate;
import hellfirepvp.astralsorcery.common.util.VoxelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockInfusedWoodColumn
 * Created by HellFirePvP
 * Date: 20.07.2019 / 20:09
 */
public class BlockInfusedWoodColumn extends BlockInfusedWoodTemplate implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<PillarType> PILLAR_TYPE = EnumProperty.create("pillartype", PillarType.class);

    private final VoxelShape middleShape, bottomShape, topShape;

    public BlockInfusedWoodColumn() {
        this.registerDefaultState(this.getStateDefinition().any().setValue(PILLAR_TYPE, PillarType.MIDDLE).setValue(WATERLOGGED, false));
        this.middleShape = createPillarShape();
        this.topShape    = createPillarTopShape();
        this.bottomShape = createPillarBottomShape();
    }

    protected VoxelShape createPillarShape() {
        return Block.box(4, 0, 4, 12, 16, 12);
    }

    protected VoxelShape createPillarTopShape() {
        VoxelShape column = Block.box(4, 0, 4, 12, 14, 12);
        VoxelShape top = Block.box(2, 14, 2, 14, 16, 14);

        return VoxelUtils.combineAll(BooleanOp.OR,
                column, top);
    }

    protected VoxelShape createPillarBottomShape() {
        VoxelShape column = Block.box(4, 2, 4, 12, 16, 12);
        VoxelShape bottom = Block.box(2, 0, 2, 14, 2, 14);

        return VoxelUtils.combineAll(BooleanOp.OR,
                column, bottom);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PILLAR_TYPE, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        switch (state.getValue(PILLAR_TYPE)) {
            case TOP:
                return this.topShape;
            case BOTTOM:
                return this.bottomShape;
            default:
            case MIDDLE:
                return this.middleShape;
        }
    }

    @Override
    public BlockState updateShape(BlockState thisState, Direction otherBlockFacing, BlockState otherBlockState, LevelAccessor world, BlockPos thisPos, BlockPos otherBlockPos) {
        if (thisState.getValue(WATERLOGGED)) {
            // 1.16.5: world.getPendingFluidTicks().scheduleTick(pos, WATER, WATER.getTickRate(world))
            world.scheduleTick(thisPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return this.getThisState(world, thisPos).setValue(WATERLOGGED, thisState.getValue(WATERLOGGED));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos blockpos = ctx.getClickedPos();
        Level world = ctx.getLevel();
        FluidState ifluidstate = world.getFluidState(blockpos);
        return this.getThisState(world, blockpos).setValue(WATERLOGGED, ifluidstate.getType() == Fluids.WATER);
    }

    private BlockState getThisState(BlockGetter world, BlockPos pos) {
        boolean hasUp   = world.getBlockState(pos.above()).getBlock() instanceof BlockInfusedWoodColumn;
        boolean hasDown = world.getBlockState(pos.below()).getBlock() instanceof BlockInfusedWoodColumn;
        if (hasUp) {
            if (hasDown) {
                return this.defaultBlockState().setValue(PILLAR_TYPE, PillarType.MIDDLE);
            }
            return this.defaultBlockState().setValue(PILLAR_TYPE, PillarType.BOTTOM);
        } else if (hasDown) {
            return this.defaultBlockState().setValue(PILLAR_TYPE, PillarType.TOP);
        }
        return this.defaultBlockState().setValue(PILLAR_TYPE, PillarType.MIDDLE);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockPathTypes getBlockPathType(BlockState state, BlockGetter world, BlockPos pos, @Nullable Mob entity) {
        return BlockPathTypes.BLOCKED;
    }


    public static enum PillarType implements StringRepresentable {

        TOP,
        MIDDLE,
        BOTTOM;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return this.getSerializedName();
        }
    }
}
