package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.infusion.InfuserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

// Reference: 1.16.5 BlockInfuser lines 1-125.
// Implemented here: custom VoxelShape, item input insertion/extraction, wand-triggered infusion start,
// pickup sound, comparator output, pathfinding block, and model render shape.
// Missing dependencies to fully match:
// - BlockInventory base class for shared inventory block behavior.
// - CustomItemBlock interface.
// - PropertiesMarble.defaultMarble() harvest/tool settings.

public class BlockInfuser extends BaseEntityBlock {

    private static final VoxelShape INFUSER = box(0, 0, 0, 16, 12, 16);

    public BlockInfuser() {
        super(PropertiesMarble.defaultMarble()
                .noOcclusion());
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof InfuserBlockEntity infuser)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);
        ItemStack stored = infuser.getItemInput();
        if (held.is(ItemsAS.WAND.get())) {
            return !stored.isEmpty() && infuser.startInfusion(player) ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
        }

        if (!held.isEmpty()) {
            if (infuser.getActiveRecipe() != null) {
                return InteractionResult.CONSUME;
            }
            if (!stored.isEmpty()) {
                ItemStack removed = infuser.setItemInput(ItemStack.EMPTY);
                if (!player.addItem(removed)) {
                    player.drop(removed, false);
                }
            }
            if (!world.isEmptyBlock(pos.above())) {
                return InteractionResult.PASS;
            }
            ItemStack inserted = held.copy();
            inserted.setCount(1);
            infuser.setItemInput(inserted);
            world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.CONSUME;
        }

        if (!stored.isEmpty() && infuser.getActiveRecipe() == null) {
            ItemStack removed = infuser.setItemInput(ItemStack.EMPTY);
            if (!player.addItem(removed)) {
                player.drop(removed, false);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return INFUSER;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof InfuserBlockEntity infuser) {
            return infuser.getItemInput().isEmpty() ? 0 : 15;
        }
        return 0;
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
        return new InfuserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.INFUSER.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof InfuserBlockEntity infuser) {
                infuser.tick();
            }
        } : null;
    }
}
