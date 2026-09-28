package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionReceiver;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

public interface IStarlightReceiver<T extends ITransmissionReceiver> extends IStarlightTransmission<T> {

    @Nonnull
    T provideEndpoint(BlockPos at);

    @Override
    @Nonnull
    default T provideTransmissionNode(BlockPos at) {
        return provideEndpoint(at);
    }
}
