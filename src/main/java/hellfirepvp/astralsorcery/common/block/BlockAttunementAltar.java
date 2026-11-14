package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.base.LargeBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.AttunementAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Port of 1.16.5 BlockAttunementAltar (ContainerBlock + TESR).
 */
public class BlockAttunementAltar extends BaseEntityBlock implements CustomItemBlock, LargeBlock {

    private static final AABB PLACEMENT_BOX = new AABB(-1, 0, -1, 1, 1, 1);
    private static final VoxelShape ATTUNEMENT_ALTAR = box(-2, 0, -2, 18, 6, 18);
    private static final VoxelShape ATTUNEMENT_ALTAR_COLLISION = box(0, 0, 0, 16, 6, 16);

    public BlockAttunementAltar() {
        // 1.16.5 used harvestLevel(1)/harvestTool(PICKAXE). In 1.20.1 that lives entirely in the data tags:
        // data/minecraft/tags/blocks/mineable/pickaxe.json and .../needs_stone_tool.json both list this block
        // (requiresCorrectToolForDrops comes from PropertiesMarble).
        super(PropertiesMarble.defaultMarble()
                .lightLevel((state) -> 4));
    }

    @Override
    public AABB getBlockSpace() {
        return PLACEMENT_BOX;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.canPlaceAt(context) ? this.defaultBlockState() : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ATTUNEMENT_ALTAR;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return ATTUNEMENT_ALTAR_COLLISION;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    // 1.16.5 ContainerBlock rendered INVISIBLE and drew the altar through its TESR.
    // VERIFY_PORT[AS-MCH-01]: the target has a blockstate/model (astralsorcery:block/attunement_altar -> base/nothing,
    // particle texture only), so MODEL draws nothing but supplies break particles; RenderAttunementAltar is still a stub,
    // so verify in game that the altar becomes visible once the TESR (and its obj model) is ported.
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AttunementAltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // TileAttunementAltar#tick branches on level.isClientSide() itself, mirroring the 1.16.5 ITickableTileEntity.
        return type == BlockEntityTypesAS.ATTUNEMENT_ALTAR.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof AttunementAltarBlockEntity altar) {
                altar.tick();
            }
        } : null;
    }
}
