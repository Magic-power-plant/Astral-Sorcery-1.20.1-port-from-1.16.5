package hellfirepvp.astralsorcery.common.data.sync.base;

import net.minecraft.nbt.CompoundTag;

/** Compatibility base for existing providers; new decoders may implement SyncDataReader directly. */
public abstract class ClientDataReader<C extends ClientData<C>> implements hellfirepvp.astralsorcery.api.sync.SyncDataReader<C> {

    public abstract void readFromIncomingFullSync(C data, CompoundTag compound);

    public abstract void readFromIncomingDiff(C data, CompoundTag compound);

}
