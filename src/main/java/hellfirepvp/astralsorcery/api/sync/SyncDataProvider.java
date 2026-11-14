package hellfirepvp.astralsorcery.api.sync;

import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import net.minecraft.resources.ResourceLocation;

/** Factories for one named sync channel. Products must be non-null and common-side safe. */
public interface SyncDataProvider<S extends AbstractData, C extends ClientData<C>> {
    ResourceLocation getKey();
    S provideServerData();
    C provideClientData();
    SyncDataReader<C> createReader();
}
