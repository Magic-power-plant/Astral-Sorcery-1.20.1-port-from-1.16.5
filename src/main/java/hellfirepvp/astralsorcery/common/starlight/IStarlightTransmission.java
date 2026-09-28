package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IStarlightTransmission<T extends IPrismTransmissionNode> {

    @Nullable
    @SuppressWarnings("unchecked")
    default T getNode() {
        Level world = getTrWorld();
        if (world == null) {
            return null;
        }
        return (T) WorldNetworkHandler.getNetworkHandler(world).getTransmissionNode(getTrPos());
    }

    @Nonnull
    BlockPos getTrPos();

    @Nonnull
    Level getTrWorld();

    @Nonnull
    T provideTransmissionNode(BlockPos at);
}
