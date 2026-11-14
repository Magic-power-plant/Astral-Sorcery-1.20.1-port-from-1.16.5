package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.starlight.WellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

// Reference: 1.16.5 BlockWell lines 1-179.
// Missing dependencies to fully match:
// - BlockStarlightNetwork base class for starlight network block notifications.
// - CustomItemBlock marker interface.
// - PropertiesMarble.defaultMarble() harvest/tool settings.
// Implemented here from reference: custom voxel shape, catalyst insertion/removal, bucket filling, break catalyst on removal,
// comparator output, pathfinding block, and model render shape.

public class BlockWell extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            box(1, 0, 1, 15, 2, 15),
            box(3, 2, 3, 13, 4, 13),
            box(1, 4, 1, 15, 5, 15),
            box(1, 5, 1, 2, 16, 14),
            box(2, 5, 1, 15, 16, 2),
            box(14, 5, 2, 15, 16, 15),
            box(1, 5, 14, 14, 16, 15)
    );

    public BlockWell() {
        super(PropertiesMarble.defaultMarble()
                .noOcclusion());
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof WellBlockEntity well)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (world.isClientSide) {
            return !held.isEmpty() || !well.getCatalyst().isEmpty() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!held.isEmpty()) {
            if (well.getCatalyst().isEmpty() && well.canAcceptCatalyst(held)) {
                if (!world.isEmptyBlock(pos.above())) {
                    return InteractionResult.PASS;
                }
                ItemStack inserted = held.copy();
                inserted.setCount(1);
                well.setCatalyst(inserted);
                world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                return InteractionResult.CONSUME;
            }

            boolean filled = well.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                    .map(handler -> {
                        var result = FluidUtil.tryFillContainerAndStow(held, handler, new InvWrapper(player.getInventory()), FluidType.BUCKET_VOLUME, player, true);
                        if (result.isSuccess()) {
                            player.setItemInHand(hand, result.getResult());
                            world.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1F, 1F);
                            well.setChanged();
                            return true;
                        }
                        return false;
                    })
                    .orElse(false);
            if (filled) {
                return InteractionResult.CONSUME;
            }
        }

        if (!well.getCatalyst().isEmpty()) {
            ItemStack extracted = well.getCatalyst().copy();
            well.setCatalyst(ItemStack.EMPTY);
            if (!player.addItem(extracted)) {
                player.drop(extracted, false);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!world.isClientSide && blockEntity instanceof WellBlockEntity well && !well.getCatalyst().isEmpty()) {
                well.breakCatalyst();
            }
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof WellBlockEntity well) {
            int fluidPart = Mth.ceil(well.getTank().getPercentageFilled() * 8F);
            return well.getCatalyst().isEmpty() ? fluidPart : fluidPart + 7;
        }
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WellBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.WELL.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof WellBlockEntity well) {
                well.tick();
            }
        } : null;
    }
}
