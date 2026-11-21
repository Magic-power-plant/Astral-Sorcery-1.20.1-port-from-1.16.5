package hellfirepvp.astralsorcery.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Lifecycle notifications for block entities participating in the starlight network. */
public interface IStarlightNetworkMember {

    default void onNetworkJoin(Level level, BlockPos pos) {}

    default void onNetworkLeave(Level level, BlockPos pos) {}

    default void onNetworkDirty(Level level, BlockPos pos) {}
}
