package hellfirepvp.astralsorcery.common.data.sync.base;

import net.minecraft.nbt.CompoundTag;

public abstract class ClientDataReader<C extends ClientData<C>> {

    public abstract void readFromIncomingFullSync(C data, CompoundTag compound);

    public abstract void readFromIncomingDiff(C data, CompoundTag compound);

}
