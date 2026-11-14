package hellfirepvp.astralsorcery.common.block.util.iterator;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;

public class BlockRandomProximityPositionGenerator extends BlockRandomPositionGenerator {

    @Override
    protected BlockPos genNext(Vector3 offset, double radius) {
        BlockPos next1 = super.genNext(offset, radius);
        BlockPos next2 = super.genNext(offset, radius);
        return offset.distance(next1) < offset.distance(next2) ? next1 : next2;
    }
}
