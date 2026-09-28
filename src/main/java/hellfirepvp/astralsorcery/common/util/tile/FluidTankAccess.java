package hellfirepvp.astralsorcery.common.util.tile;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FluidTankAccess {

    private final Set<AccessibleTank> tanks = new HashSet<>();

    public void putTank(int tankId, IFluidTank tank, Direction... sides) {
        this.tanks.add(new AccessibleTank(tankId, tank, sides));
    }

    public void putTank(int tankId, IFluidTank tank, Predicate<Direction> accessibleSides) {
        this.tanks.add(new AccessibleTank(tankId, tank, accessibleSides));
    }

    public boolean hasCapability(Capability<?> capability, @Nullable Direction side) {
        return ForgeCapabilities.FLUID_HANDLER == capability && hasTanksForSide(side);
    }

    public LazyOptional<IFluidHandler> getCapability(@Nullable Direction side) {
        Set<AccessibleTank> available = side == null ? tanks : tanks.stream()
                .filter(tank -> tank.isAccessible(side))
                .collect(Collectors.toSet());
        return available.isEmpty() ? LazyOptional.empty() : LazyOptional.of(() -> new SidedAccess(available));
    }

    private boolean hasTanksForSide(@Nullable Direction side) {
        return side == null || tanks.stream().anyMatch(tank -> tank.isAccessible(side));
    }

    private static class SidedAccess implements IFluidHandler {

        private final Set<AccessibleTank> tanks;

        private SidedAccess(Set<AccessibleTank> tanks) {
            this.tanks = tanks;
        }

        private Optional<AccessibleTank> getTank(int id) {
            return tanks.stream().filter(tank -> tank.getId() == id).findFirst();
        }

        @Override
        public int getTanks() {
            return tanks.size();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return getTank(tank).map(accessibleTank -> accessibleTank.getTank().getFluid()).orElse(FluidStack.EMPTY);
        }

        @Override
        public int getTankCapacity(int tank) {
            return getTank(tank).map(accessibleTank -> accessibleTank.getTank().getCapacity()).orElse(0);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return getTank(tank).map(accessibleTank -> accessibleTank.getTank().isFluidValid(stack)).orElse(false);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            for (AccessibleTank tank : tanks) {
                int filled = tank.getTank().fill(resource, action);
                if (filled > 0) {
                    return filled;
                }
            }
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            for (AccessibleTank tank : tanks) {
                FluidStack drained = tank.getTank().drain(resource, action);
                if (!drained.isEmpty()) {
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            for (AccessibleTank tank : tanks) {
                FluidStack drained = tank.getTank().drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    return drained;
                }
            }
            return FluidStack.EMPTY;
        }
    }

    private static class AccessibleTank {

        private final int id;
        private final IFluidTank tank;
        private final Predicate<Direction> accessibleSides;

        private AccessibleTank(int id, IFluidTank tank, Direction... sides) {
            this(id, tank, side -> Arrays.asList(sides).contains(side));
        }

        private AccessibleTank(int id, IFluidTank tank, Predicate<Direction> accessibleSides) {
            this.id = id;
            this.tank = tank;
            this.accessibleSides = accessibleSides;
        }

        private int getId() {
            return id;
        }

        private IFluidTank getTank() {
            return tank;
        }

        private boolean isAccessible(Direction side) {
            return accessibleSides.test(side);
        }
    }
}
