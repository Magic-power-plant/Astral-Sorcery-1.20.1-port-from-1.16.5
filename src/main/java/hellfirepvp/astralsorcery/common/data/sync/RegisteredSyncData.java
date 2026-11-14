package hellfirepvp.astralsorcery.common.data.sync;

import hellfirepvp.astralsorcery.api.sync.SyncDataProvider;
import hellfirepvp.astralsorcery.api.sync.SyncDataReader;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import java.util.Objects;

/** Keeps each provider's server state, client state and decoder type-bound and publishes them atomically. */
final class RegisteredSyncData<S extends AbstractData, C extends ClientData<C>> {
    final SyncDataProvider<S, C> provider;
    final S server;
    final C client;
    final SyncDataReader<C> reader;

    RegisteredSyncData(ResourceLocation key, SyncDataProvider<S, C> provider) {
        this.provider = provider;
        this.server = Objects.requireNonNull(provider.provideServerData(), "server data");
        this.client = Objects.requireNonNull(provider.provideClientData(), "client data");
        this.reader = Objects.requireNonNull(provider.createReader(), "client reader");
        if (!key.equals(server.getKey()) || !key.equals(provider.getKey())) {
            throw new IllegalArgumentException("Sync provider and server data keys must agree: " + key);
        }
    }

    void readFull(CompoundTag tag) {
        reader.readFromIncomingFullSync(client, tag);
    }

    void readDiff(CompoundTag tag) {
        reader.readFromIncomingDiff(client, tag);
    }
}
