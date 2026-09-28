package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.properties.PropertiesMisc;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileChalice;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

public class BlockChalice extends BaseEntityBlock {

    private static final VoxelShape CHALICE = box(2, 0, 2, 14, 14, 14);

    public BlockChalice() {
        super(PropertiesMisc.defaultGoldMachinery()
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TileChalice chalice)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return FluidUtil.getFluidHandler(held).isPresent() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return chalice.getCapability(ForgeCapabilities.FLUID_HANDLER, null)
                .map(handler -> {
                    FluidStack contained = FluidUtil.getFluidContained(held).orElse(FluidStack.EMPTY);
                    if (contained.isEmpty()) {
                        var result = FluidUtil.tryFillContainerAndStow(held, handler, new InvWrapper(player.getInventory()), FluidType.BUCKET_VOLUME, player, true);
                        if (result.isSuccess()) {
                            player.setItemInHand(hand, result.getResult());
                            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1F, 1F);
                            chalice.setChanged();
                            return InteractionResult.CONSUME;
                        }
                    } else {
                        var result = FluidUtil.tryEmptyContainerAndStow(held, handler, new InvWrapper(player.getInventory()), FluidType.BUCKET_VOLUME, player, true);
                        if (result.isSuccess()) {
                            player.setItemInHand(hand, result.getResult());
                            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1F, 1F);
                            chalice.setChanged();
                            return InteractionResult.CONSUME;
                        }
                    }
                    return InteractionResult.PASS;
                })
                .orElse(InteractionResult.PASS);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileChalice chalice) {
            return Mth.ceil(chalice.getTank().getPercentageFilled() * 15F);
        }
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CHALICE;
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
        return new TileChalice(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityTypesAS.CHALICE.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof TileChalice chalice) {
                chalice.tick();
            }
        } : null;
    }
}
