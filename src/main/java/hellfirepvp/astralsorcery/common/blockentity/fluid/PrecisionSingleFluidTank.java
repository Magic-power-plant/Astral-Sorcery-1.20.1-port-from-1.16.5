package hellfirepvp.astralsorcery.common.blockentity.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class PrecisionSingleFluidTank implements IFluidTank {

    private double amount = 0;
    private int maxCapacity;
    private Fluid fluid = Fluids.EMPTY;
    private Runnable onUpdate = () -> {};
    private boolean allowInput = true;
    private boolean allowOutput = true;

    private PrecisionSingleFluidTank() {}

    public PrecisionSingleFluidTank(int maxCapacity) {
        this.maxCapacity = Math.max(0, maxCapacity);
    }

    public void addUpdateFunction(Runnable onUpdate) {
        Runnable previous = this.onUpdate;
        this.onUpdate = () -> {
            previous.run();
            onUpdate.run();
        };
    }

    public void setAllowInput(boolean allowInput) {
        this.allowInput = allowInput;
    }

    public void setAllowOutput(boolean allowOutput) {
        this.allowOutput = allowOutput;
    }

    public double getMaxAddable(double toAdd) {
        return Math.min(toAdd, maxCapacity - amount);
    }

    public int getMaxDrainable(double toDrain) {
        return Mth.floor(Math.min(toDrain, amount));
    }

    public double addAmount(double amount) {
        if (this.fluid == Fluids.EMPTY) {
            return amount;
        }
        double addable = getMaxAddable(amount);
        this.amount += addable;
        if (Math.abs(addable) > 0) {
            this.onUpdate.run();
        }
        return amount - addable;
    }

    @Nonnull
    public FluidStack drain(double amount) {
        if (this.fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        int drainable = getMaxDrainable(amount);
        this.amount -= drainable;
        Fluid drainedFluid = this.fluid;
        if (this.amount <= 0) {
            setFluid(Fluids.EMPTY);
        }
        if (drainable != 0) {
            this.onUpdate.run();
        }
        return new FluidStack(drainedFluid, drainable);
    }

    public void setFluid(@Nonnull Fluid fluid) {
        boolean update = fluid != this.fluid;
        if (update) {
            this.amount = 0;
        }
        this.fluid = fluid;
        if (update) {
            this.onUpdate.run();
        }
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, getFluidAmount());
    }

    @Override
    public int getFluidAmount() {
        return Mth.floor(amount);
    }

    @Override
    public int getCapacity() {
        return maxCapacity;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return true;
    }

    public boolean canFill() {
        return allowInput && amount < maxCapacity;
    }

    public boolean canDrain() {
        return allowOutput && amount > 0 && fluid != Fluids.EMPTY;
    }

    public boolean canFillFluidType(FluidStack fluidStack) {
        return canFill() && (fluid == Fluids.EMPTY || fluidStack.getFluid().equals(fluid));
    }

    public boolean canDrainFluidType(FluidStack fluidStack) {
        return canDrain() && fluidStack.getFluid().equals(fluid);
    }

    public float getPercentageFilled() {
        return maxCapacity <= 0 ? 0 : (float) (amount / maxCapacity);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || !canFillFluidType(resource)) {
            return 0;
        }
        int addable = Mth.floor(getMaxAddable(resource.getAmount()));
        if (action.execute()) {
            if (addable > 0 && fluid == Fluids.EMPTY) {
                setFluid(resource.getFluid());
            }
            addable -= addAmount(addable);
        }
        return addable;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || !canDrainFluidType(resource)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (!canDrain()) {
            return FluidStack.EMPTY;
        }
        int drainable = getMaxDrainable(maxDrain);
        if (action.execute()) {
            return drain(drainable);
        }
        return new FluidStack(fluid, drainable);
    }

    public CompoundTag writeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("amount", amount);
        tag.putInt("capacity", maxCapacity);
        tag.putBoolean("allowInput", allowInput);
        tag.putBoolean("allowOutput", allowOutput);
        ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);
        tag.putString("fluid", fluidId == null ? "minecraft:empty" : fluidId.toString());
        return tag;
    }

    public void readNBT(CompoundTag tag) {
        amount = tag.getDouble("amount");
        maxCapacity = tag.getInt("capacity");
        allowInput = tag.getBoolean("allowInput");
        allowOutput = tag.getBoolean("allowOutput");
        Fluid loadedFluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.tryParse(tag.getString("fluid")));
        fluid = loadedFluid == null ? Fluids.EMPTY : loadedFluid;
    }

    public static PrecisionSingleFluidTank deserialize(CompoundTag tag) {
        PrecisionSingleFluidTank tank = new PrecisionSingleFluidTank();
        tank.readNBT(tag);
        return tank;
    }
}
