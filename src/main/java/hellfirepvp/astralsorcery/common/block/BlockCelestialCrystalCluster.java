package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.BlockCrystalContainer;
import hellfirepvp.astralsorcery.common.block.base.BlockStarlightRecipient;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.crystal.CollectorCrystalType;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.blockentity.CelestialCrystalsBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Port of 1.16.5 {@code BlockCelestialCrystalCluster}.
 */
public class BlockCelestialCrystalCluster extends BlockCrystalContainer implements BlockStarlightRecipient, CustomItemBlock {

    private static final VoxelShape GROWTH_STAGE_0 = Block.box(4, 0, 5, 12, 8, 11);
    private static final VoxelShape GROWTH_STAGE_1 = Block.box(4, 0, 5, 12, 10, 11);
    private static final VoxelShape GROWTH_STAGE_2 = Block.box(2, 0, 4, 12, 12, 14);
    private static final VoxelShape GROWTH_STAGE_3 = Block.box(2, 0, 2, 14, 14, 14);
    private static final VoxelShape GROWTH_STAGE_4 = Block.box(2, 0, 2, 14, 16, 14);

    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 4);

    public BlockCelestialCrystalCluster() {
        super(Properties.of()
                .mapColor(CollectorCrystalType.CELESTIAL_CRYSTAL.getMaterialColor())
                .strength(3F, 3F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.GLASS)
                .lightLevel((state) -> 8)
                .noOcclusion()
                .offsetType(OffsetType.XZ)
                //1.20.1 rejects an offset block with a collision shape unless its shape is dynamic (as 1.21.1 does)
                .dynamicShape());
        registerDefaultState(this.getStateDefinition().any().setValue(STAGE, 0));
    }

    @Override
    public void receiveStarlight(Level world, Random rand, BlockPos pos, IWeakConstellation starlightType, double amount) {
        CelestialCrystalsBlockEntity crystals = MiscUtils.getBlockEntityAt(world, pos, CelestialCrystalsBlockEntity.class, false);
        if (crystals != null) {
            crystals.grow((int) (CelestialCrystalsBlockEntity.TICK_GROWTH_CHANCE / amount));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(world, pos);
        VoxelShape shape;
        switch (state.getValue(STAGE)) {
            case 4:
                shape = GROWTH_STAGE_4;
                break;
            case 3:
                shape = GROWTH_STAGE_3;
                break;
            case 2:
                shape = GROWTH_STAGE_2;
                break;
            case 1:
                shape = GROWTH_STAGE_1;
                break;
            case 0:
            default:
                shape = GROWTH_STAGE_0;
        }
        return shape.move(offset.x, offset.y, offset.z);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction placedAgainst, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        if (!this.canSurvive(state, world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return canSupportRigidBlock(world, pos.below());
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            super.onRemove(state, world, pos, newState, isMoving);

            if (!world.isClientSide()) {
                PktPlayEffect effect = new PktPlayEffect(PktPlayEffect.Type.SMALL_CRYSTAL_BREAK)
                        .addData(buf -> ByteBufUtils.writeVector(buf,
                                new Vector3(pos).add(state.getOffset(world, pos)).add(0.5, 0.4, 0.5)));
                PacketChannel.sendToAllAround(effect, world, Vec3.atCenterOf(pos), 32);
            }
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CelestialCrystalsBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.CELESTIAL_CRYSTAL_CLUSTER.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof CelestialCrystalsBlockEntity crystals) {
                crystals.tick();
            }
        } : null;
    }
}
