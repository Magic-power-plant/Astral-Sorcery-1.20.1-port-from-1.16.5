package hellfirepvp.astralsorcery.common.structure.match.util;

import hellfirepvp.astralsorcery.common.structure.match.structure.MatchableStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class StructureUtil {

    private StructureUtil() {}

    public static Optional<Integer> getLowestMismatchingSlice(MatchableStructure structure, BlockGetter world, BlockPos offset) {
        for (int y = structure.getMinimumOffset().getY(); y <= structure.getMaximumOffset().getY(); y++) {
            if (!structure.matchesSlice(world, offset, y)) {
                return Optional.of(y);
            }
        }
        return Optional.empty();
    }

    public static Set<BlockPos> getMismatches(MatchableStructure structure, BlockGetter world, BlockPos offset) {
        Set<BlockPos> mismatches = new HashSet<>();
        structure.getContents().keySet().forEach(pos -> {
            if (!structure.matchesSingleBlock(world, offset, pos)) {
                mismatches.add(offset.offset(pos));
            }
        });
        return mismatches;
    }
}
