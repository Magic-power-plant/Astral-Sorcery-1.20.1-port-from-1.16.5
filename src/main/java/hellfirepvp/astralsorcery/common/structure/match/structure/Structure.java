package hellfirepvp.astralsorcery.common.structure.match.structure;

import hellfirepvp.astralsorcery.common.structure.match.block.MatchableState;
import hellfirepvp.astralsorcery.common.structure.match.tile.MatchableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Structure {

    @Nonnull
    Map<BlockPos, ? extends MatchableState> getContents();

    @Nonnull
    Map<BlockPos, ? extends MatchableTile<? extends BlockEntity>> getTileEntities();

    Vec3i getMaximumOffset();

    Vec3i getMinimumOffset();

    default boolean hasBlockAt(BlockPos offset) {
        return getContents().containsKey(offset);
    }

    @Nonnull
    default MatchableState getBlockStateAt(BlockPos offset) {
        if (!hasBlockAt(offset)) {
            return MatchableState.AIR;
        }
        return Objects.requireNonNullElse(getContents().get(offset), MatchableState.AIR);
    }

    default boolean hasTileAt(BlockPos offset) {
        return getTileEntities().containsKey(offset);
    }

    @Nullable
    default MatchableTile<? extends BlockEntity> getTileEntityAt(BlockPos offset) {
        if (!hasTileAt(offset)) {
            return null;
        }
        return getTileEntities().get(offset);
    }

    default List<Map.Entry<BlockPos, ? extends MatchableState>> getStructureSlice(int yOffset) {
        return getContents().entrySet().stream()
                .filter(entry -> entry.getKey().getY() == yOffset)
                .collect(Collectors.toList());
    }
}
