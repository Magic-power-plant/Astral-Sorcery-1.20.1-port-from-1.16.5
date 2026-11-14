package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.properties.PropertiesGlass;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.SpectralRelayBlockEntity;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
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

public class BlockSpectralRelay extends BaseEntityBlock {

    private static final VoxelShape RELAY = box(2, 0, 2, 14, 2, 14);

    public BlockSpectralRelay() {
        super(PropertiesGlass.coatedGlass()
                .lightLevel(state -> 4)
                .noOcclusion());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SpectralRelayBlockEntity relay)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) {
            return !held.isEmpty() || !relay.getInventory().getStackInSlot(0).isEmpty() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        ItemStack stored = relay.getInventory().getStackInSlot(0);
        if (!held.isEmpty()) {
            if (!stored.isEmpty()) {
                ItemStack removed = relay.getInventory().extractItem(0, 1, false);
                if (!player.addItem(removed)) {
                    player.drop(removed, false);
                }
                SpectralRelayBlockEntity.cascadeRelayProximityUpdates(level, pos);
            }
            if (!level.isEmptyBlock(pos.above()) || !relay.getInventory().isItemValid(0, held)) {
                return InteractionResult.PASS;
            }
            ItemStack inserted = ItemUtils.copyStackWithSize(held, 1);
            relay.getInventory().setStackInSlot(0, inserted);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            relay.updateAltarLinkState();
            SpectralRelayBlockEntity.cascadeRelayProximityUpdates(level, pos);
            relay.markForUpdate();
            return InteractionResult.CONSUME;
        }

        if (!stored.isEmpty()) {
            ItemStack removed = relay.getInventory().extractItem(0, 1, false);
            if (!player.addItem(removed)) {
                player.drop(removed, false);
            }
            SpectralRelayBlockEntity.cascadeRelayProximityUpdates(level, pos);
            relay.markForUpdate();
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!level.isClientSide && blockEntity instanceof SpectralRelayBlockEntity relay) {
                ItemStack stored = relay.getInventory().getStackInSlot(0);
                if (!stored.isEmpty()) {
                    ItemUtils.dropItemNaturally(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stored.copy());
                    relay.getInventory().setStackInSlot(0, ItemStack.EMPTY);
                }
                SpectralRelayBlockEntity.cascadeRelayProximityUpdates(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SpectralRelayBlockEntity relay) {
            return relay.getInventory().getStackInSlot(0).isEmpty() ? 0 : 15;
        }
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return RELAY;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpectralRelayBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.SPECTRAL_RELAY.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof SpectralRelayBlockEntity relay) {
                relay.tick();
            }
        } : null;
    }
}
