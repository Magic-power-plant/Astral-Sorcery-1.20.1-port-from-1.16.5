package hellfirepvp.astralsorcery.common.starlight.transmission;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.List;

public interface ITransmissionReceiver extends IPrismTransmissionNode {

    @Override
    default List<NodeConnection<IPrismTransmissionNode>> queryNext(WorldNetworkHandler handler) {
        return new LinkedList<>();
    }

    @Override
    default void notifyLink(Level world, BlockPos to) {}

    @Override
    default boolean notifyUnlink(Level world, BlockPos to) {
        return false;
    }

    void onStarlightReceive(Level world, IWeakConstellation type, double amount);
}
