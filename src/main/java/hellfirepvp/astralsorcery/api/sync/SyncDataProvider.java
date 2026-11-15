package hellfirepvp.astralsorcery.api.sync;

import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import net.minecraft.resources.ResourceLocation;

/**
 * Factories for one named sync channel. The non-null key is stable and must match the server state's key.
 * Successful registration calls each factory once and atomically publishes all products. Null products or
 * factory exceptions leave the channel unpublished; external factory side effects cannot be rolled back.
 * Products are reused and cleared between sessions, and must be safe to construct on a dedicated server.
 */
public interface SyncDataProvider<S extends AbstractData, C extends ClientData<C>> {
    ResourceLocation getKey();
    S provideServerData();
    C provideClientData();
    SyncDataReader<C> createReader();
}
