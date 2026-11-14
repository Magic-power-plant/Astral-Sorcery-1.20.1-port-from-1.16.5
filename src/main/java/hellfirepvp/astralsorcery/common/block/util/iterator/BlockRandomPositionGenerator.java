package hellfirepvp.astralsorcery.common.block.util.iterator;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class BlockRandomPositionGenerator extends BlockPositionGenerator {

    @Override
    protected BlockPos genNext(Vector3 offset, double radius) {
        if (radius <= 0) {
            return offset.toBlockPos();
        }
        return offset.clone().add(Vector3.random().multiply(radius)).toBlockPos();
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {}

    @Override
    public void readFromNBT(CompoundTag nbt) {}
}
