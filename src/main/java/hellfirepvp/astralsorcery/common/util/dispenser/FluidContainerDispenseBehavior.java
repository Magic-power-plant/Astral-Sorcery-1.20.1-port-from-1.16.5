package hellfirepvp.astralsorcery.common.util.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

public class FluidContainerDispenseBehavior extends DefaultDispenseItemBehavior {

    private static final FluidContainerDispenseBehavior INSTANCE = new FluidContainerDispenseBehavior();
    private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

    private FluidContainerDispenseBehavior() {}

    public static FluidContainerDispenseBehavior getInstance() {
        return INSTANCE;
    }

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        if (FluidUtil.getFluidContained(stack).isPresent()) {
            return dumpContainer(source, stack);
        } else {
            return fillContainer(source, stack);
        }
    }

    @Nonnull
    private ItemStack fillContainer(BlockSource source, ItemStack stack) {
        Level world = source.getLevel();
        Direction dispenserFacing = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos blockpos = source.getPos().relative(dispenserFacing);

        FluidActionResult actionResult = FluidUtil.tryPickUpFluid(stack, null, world, blockpos, dispenserFacing.getOpposite());
        ItemStack resultStack = actionResult.getResult();

        if (!actionResult.isSuccess() || resultStack.isEmpty()) {
            return super.execute(source, stack);
        }

        if (stack.getCount() == 1) {
            return resultStack;
        } else if (source.getEntity() instanceof net.minecraft.world.level.block.entity.DispenserBlockEntity dispenser && dispenser.addItem(resultStack) < 0) {
            this.defaultBehavior.dispense(source, resultStack);
        }

        ItemStack stackCopy = stack.copy();
        stackCopy.shrink(1);
        return stackCopy;
    }

    @Nonnull
    private ItemStack dumpContainer(BlockSource source, @Nonnull ItemStack stack) {
        Level world = source.getLevel();
        ItemStack singleStack = stack.copy();
        singleStack.setCount(1);
        LazyOptional<IFluidHandlerItem> itemFluidHandler = FluidUtil.getFluidHandler(singleStack);
        if (!itemFluidHandler.isPresent()) {
            return super.execute(source, stack);
        }
        FluidStack drained = itemFluidHandler
                .map(handler -> handler.drain(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE))
                .orElse(FluidStack.EMPTY);
        Direction dispenserFacing = source.getBlockState().getValue(DispenserBlock.FACING);
        BlockPos pos = source.getPos().relative(dispenserFacing);
        FluidActionResult result = FluidUtil.tryPlaceFluid(null, world, InteractionHand.MAIN_HAND, pos, stack, drained);

        if (result.isSuccess()) {
            ItemStack drainedStack = result.getResult();

            if (drainedStack.getCount() == 1) {
                return drainedStack;
            } else if (!drainedStack.isEmpty() && source.getEntity() instanceof net.minecraft.world.level.block.entity.DispenserBlockEntity dispenser && dispenser.addItem(drainedStack) < 0) {
                this.defaultBehavior.dispense(source, drainedStack);
            }

            ItemStack stackCopy = drainedStack.copy();
            stackCopy.shrink(1);
            return stackCopy;
        } else {
            return this.defaultBehavior.dispense(source, stack);
        }
    }
}
