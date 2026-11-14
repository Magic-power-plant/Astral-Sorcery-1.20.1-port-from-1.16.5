package hellfirepvp.astralsorcery.common.block.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockPredicate {

    boolean test(Level world, BlockPos pos, BlockState state);
}
