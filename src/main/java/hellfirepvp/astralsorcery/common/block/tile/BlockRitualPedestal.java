package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockStarlightNetwork;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.VoxelUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Port of 1.16.5 {@code BlockRitualPedestal}.
 * <p>
 * 1.16.5 also implemented ObserverLib's {@code BlockStructureObserver}; the structure-observer subsystem is not part of
 * the embedded ObserverLib subset (see TODO_PORT[AS-MCH-04] on the structure matching side).
 */
public class BlockRitualPedestal extends BlockStarlightNetwork implements CustomItemBlock {

    private final VoxelShape shape;

    public BlockRitualPedestal() {
        super(PropertiesMarble.defaultMarble()
                .noOcclusion());

        this.shape = createShape();
    }

    protected VoxelShape createShape() {
        VoxelShape m1 = Block.box(0, 0, 0, 16, 2, 16);
        VoxelShape m2 = Block.box(11, 2, 6, 15, 6, 10);
        VoxelShape m3 = Block.box(12, 2, 12, 14, 7, 14);
        VoxelShape m4 = Block.box(2, 2, 12, 4, 7, 14);
        VoxelShape m5 = Block.box(2, 2, 12, 4, 7, 14);
        VoxelShape m6 = Block.box(12, 2, 2, 14, 7, 4);
        VoxelShape m7 = Block.box(2, 2, 2, 4, 7, 4);
        VoxelShape m8 = Block.box(6, 2, 6, 10, 10, 10);
        VoxelShape m9 = Block.box(2, 10, 2, 14, 12, 14);
        VoxelShape m10 = Block.box(6, 2, 11, 10, 6, 15);
        VoxelShape m11 = Block.box(6, 2, 1, 10, 6, 5);
        VoxelShape m12 = Block.box(3, 12, 11, 5, 14, 13);
        VoxelShape m13 = Block.box(1, 2, 6, 5, 6, 10);
        VoxelShape m14 = Block.box(3, 12, 3, 5, 14, 5);
        VoxelShape m15 = Block.box(11, 12, 3, 13, 14, 5);
        VoxelShape m16 = Block.box(11, 12, 11, 13, 14, 13);
        VoxelShape m17 = Block.box(11, 2, 11, 15, 8, 15);
        VoxelShape m18 = Block.box(11, 2, 1, 15, 8, 5);
        VoxelShape m19 = Block.box(1, 2, 11, 5, 8, 15);
        VoxelShape m20 = Block.box(1, 2, 1, 5, 8, 5);

        return VoxelUtils.combineAll(BooleanOp.OR, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10,
                m11, m12, m13, m14, m15, m16, m17, m18, m19, m20);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rtr) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        TileRitualPedestal pedestal = MiscUtils.getTileAt(world, pos, TileRitualPedestal.class, true);
        if (pedestal == null) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        ItemStack in = pedestal.getCurrentCrystal();
        if (player.isShiftKeyDown()) {
            pedestal.tryPlaceCrystalInPedestal(ItemStack.EMPTY);
            if (player.getItemInHand(hand).isEmpty()) {
                player.setItemInHand(hand, in);
            } else {
                player.getInventory().placeItemBackInInventory(in);
            }
        } else {
            player.setItemInHand(hand, pedestal.tryPlaceCrystalInPedestal(heldItem));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);

        TileRitualPedestal te = MiscUtils.getTileAt(world, pos, TileRitualPedestal.class, true);
        if (te != null && !world.isClientSide()) {
            BlockPos toCheck = pos.above();
            BlockState other = world.getBlockState(toCheck);
            if (Block.isFaceFull(other.getCollisionShape(world, pos), Direction.DOWN)) {
                ItemUtils.dropItem(world, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, te.getCurrentCrystal());
                te.tryPlaceCrystalInPedestal(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (placer instanceof Player) {
            TileRitualPedestal pedestal = MiscUtils.getTileAt(world, pos, TileRitualPedestal.class, true);
            if (pedestal != null) {
                pedestal.setOwner(placer.getUUID());
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
        return new TileRitualPedestal(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityTypesAS.RITUAL_PEDESTAL.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof TileRitualPedestal pedestal) {
                pedestal.tick();
            }
        } : null;
    }
}
