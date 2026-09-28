package hellfirepvp.astralsorcery.common.util.block.iterator;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public abstract class BlockPositionGenerator {

    private BiPredicate<BlockPos, Double> posFilter = (pos, radius) -> true;

    public final BlockPositionGenerator andFilter(Predicate<BlockPos> filter) {
        return this.andFilter((pos, radius) -> filter.test(pos));
    }

    public final BlockPositionGenerator andFilter(BiPredicate<BlockPos, Double> filter) {
        this.posFilter = this.posFilter.and(filter);
        return this;
    }

    public final BlockPositionGenerator copyFilterFrom(BlockPositionGenerator other) {
        other.andFilter(this.posFilter);
        return this;
    }

    public final BlockPos generateNextPosition(Vector3 offset, double selectionRadius) {
        BlockPos next;
        do {
            next = genNext(offset, selectionRadius);
        } while (!posFilter.test(next, selectionRadius));
        return next;
    }

    protected abstract BlockPos genNext(Vector3 offset, double radius);

    public abstract void writeToNBT(CompoundTag nbt);

    public abstract void readFromNBT(CompoundTag nbt);
}
