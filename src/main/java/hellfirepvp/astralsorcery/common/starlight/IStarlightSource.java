package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionSource;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

public interface IStarlightSource<T extends ITransmissionSource> extends IStarlightTransmission<T> {

    @Nonnull
    IIndependentStarlightSource provideNewSourceNode();

    @Nonnull
    T provideSourceNode(BlockPos at);

    boolean needsToRefreshNetworkChain();

    void markChainRebuilt();

    @Override
    @Nonnull
    default T provideTransmissionNode(BlockPos at) {
        return provideSourceNode(at);
    }
}
