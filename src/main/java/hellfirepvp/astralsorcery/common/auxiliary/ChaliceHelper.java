package hellfirepvp.astralsorcery.common.auxiliary;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.TileChalice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ChaliceHelper {

    @Nonnull
    public static List<BlockPos> findNearbyChalices(Level level, BlockPos origin, int distance) {
        List<BlockPos> positions = new ArrayList<>();
        int radius = Math.max(0, Math.min(16, distance));
        Vec3 originVector = Vec3.atCenterOf(origin).add(0, 1, 0);
        BlockPos min = origin.offset(-radius, -radius, -radius);
        BlockPos max = origin.offset(radius, radius, radius);
        for (BlockPos at : BlockPos.betweenClosed(min, max)) {
            if (at.equals(origin) || at.distSqr(origin) > radius * radius) {
                continue;
            }
            if (!level.getBlockState(at).is(BlocksAS.CHALICE.get())) {
                continue;
            }
            if (level.hasNeighborSignal(at) || level.getBlockState(at.below()).is(BlocksAS.FOUNTAIN.get())) {
                continue;
            }
            if (isLineClear(level, originVector, Vec3.atCenterOf(at).add(0, 1, 0))) {
                positions.add(at.immutable());
            }
        }
        return positions;
    }

    @Nonnull
    public static List<TileChalice> findNearbyChalicesContaining(Level level, BlockPos origin, FluidStack expected, int distance) {
        List<TileChalice> out = new ArrayList<>();
        for (BlockPos chalicePos : findNearbyChalices(level, origin, distance)) {
            if (level.getBlockEntity(chalicePos) instanceof TileChalice chalice && chalice.getTank().drain(expected, IFluidHandler.FluidAction.SIMULATE).getAmount() >= expected.getAmount()) {
                out.add(chalice);
            }
        }
        return out;
    }

    @Nonnull
    public static Optional<List<TileChalice>> findNearbyChalicesCombined(Level level, BlockPos origin, FluidStack expected, int distance) {
        FluidStack required = expected.copy();
        List<TileChalice> out = new ArrayList<>();
        for (BlockPos chalicePos : findNearbyChalices(level, origin, distance)) {
            if (level.getBlockEntity(chalicePos) instanceof TileChalice chalice) {
                FluidStack drained = chalice.getTank().drain(required, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    required.shrink(drained.getAmount());
                    out.add(chalice);
                    if (required.isEmpty()) {
                        return Optional.of(out);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static boolean doChalicesContainCombined(Level level, Collection<BlockPos> chalicePositions, FluidStack expected) {
        FluidStack required = expected.copy();
        for (BlockPos pos : chalicePositions) {
            if (level.getBlockEntity(pos) instanceof TileChalice chalice) {
                FluidStack drained = chalice.getTank().drain(required, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty()) {
                    required.shrink(drained.getAmount());
                    if (required.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return required.isEmpty();
    }

    public static boolean isLineClear(Level level, Vec3 from, Vec3 to) {
        Vec3 step = to.subtract(from);
        double distance = step.length();
        if (distance <= 0) {
            return true;
        }
        step = step.normalize().scale(0.1);
        Vec3 current = from;
        BlockPos start = BlockPos.containing(from);
        BlockPos target = BlockPos.containing(to);
        BlockPos last = null;
        for (double traveled = 0.1; traveled <= distance; traveled += 0.1) {
            current = current.add(step);
            BlockPos at = BlockPos.containing(current);
            if (at.equals(last)) {
                continue;
            }
            last = at;
            if (at.equals(start) || at.equals(target)) {
                continue;
            }
            BlockState state = level.getBlockState(at);
            if (state.isAir()) {
                continue;
            }
            if (!state.getFluidState().isEmpty()) {
                return false;
            }
            if (!(state.getBlock() instanceof AbstractGlassBlock)) {
                return false;
            }
        }
        return true;
    }
}
