package hellfirepvp.astralsorcery.common.structure.match.structure;

import hellfirepvp.astralsorcery.common.structure.match.block.MatchableState;
import hellfirepvp.astralsorcery.common.structure.match.tile.MatchableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface MatchableStructure extends Structure {

    default boolean matches(@Nonnull BlockGetter reader, @Nonnull BlockPos center) {
        for (Map.Entry<BlockPos, ? extends MatchableState> entry : getContents().entrySet()) {
            if (!matchesSingleBlock(reader, center, entry.getKey())) {
                return false;
            }
        }
        return true;
    }

    default boolean matchesSlice(@Nonnull BlockGetter reader, @Nonnull BlockPos center, int yOffset) {
        if (getMinimumOffset().getY() > yOffset || getMaximumOffset().getY() < yOffset) {
            return true;
        }
        for (BlockPos pos : getContents().keySet()) {
            if (pos.getY() == yOffset && !matchesSingleBlock(reader, center, pos)) {
                return false;
            }
        }
        return true;
    }

    default boolean matchesSingleBlock(@Nonnull BlockGetter reader, @Nonnull BlockPos center, @Nonnull BlockPos centerOffset) {
        BlockPos absolute = center.offset(centerOffset);
        return matchesSingleBlock(reader, center, centerOffset, reader.getBlockState(absolute), reader.getBlockEntity(absolute));
    }

    default boolean matchesSingleBlock(@Nullable BlockGetter reader,
                                       @Nonnull BlockPos center,
                                       @Nonnull BlockPos centerOffset,
                                       @Nonnull BlockState comparing,
                                       @Nullable BlockEntity tileEntity) {
        if (!hasBlockAt(centerOffset)) {
            return false;
        }
        MatchableState state = getBlockStateAt(centerOffset);
        MatchableTile tileMatch = getTileEntityAt(centerOffset);
        BlockPos absolute = center.offset(centerOffset);
        return state.matches(reader, absolute, comparing) &&
                (tileEntity == null || tileMatch == null || tileMatch.matches(reader, absolute, tileEntity));
    }

    ResourceLocation getRegistryName();
}
