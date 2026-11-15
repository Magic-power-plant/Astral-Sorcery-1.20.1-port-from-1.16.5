package hellfirepvp.astralsorcery.common.data.sync;

import hellfirepvp.astralsorcery.api.event.RegisterAstralSorceryExtensionsEvent;
import hellfirepvp.astralsorcery.api.sync.SyncDataProvider;
import hellfirepvp.astralsorcery.api.sync.SyncDataReader;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class SyncDataRegistrationTest {
    private static final AtomicInteger IDS = new AtomicInteger();
    private final RegisterAstralSorceryExtensionsEvent event = new RegisterAstralSorceryExtensionsEvent();
    private final ResourceLocation key = new ResourceLocation("exampleaddon", "counter_" + IDS.incrementAndGet());

    @BeforeEach void drainPriorChanges() { SyncDataHolder.drainDirtyData(); }

    @Test void interfaceProviderSupportsSnapshotDiffAndLegacyReaderAccess() {
        Provider provider = new Provider(key);
        event.registerSyncData(provider);
        SyncDataHolder.executeServer(key, ServerState.class, state -> state.set(7));
        CompoundTag full = new CompoundTag();
        SyncDataHolder.executeServer(key, ServerState.class, state -> state.writeAllDataToPacket(full));
        SyncDataHolder.applyFullSync(key, full);
        assertEquals(7, SyncDataHolder.computeClient(key, ClientState.class, state -> state.value).orElseThrow());
        // Taking a full login snapshot must not consume the next broadcast delta.
        CompoundTag delta = SyncDataHolder.drainDirtyData().get(key);
        assertEquals(7, delta.getInt("value"));
        SyncDataHolder.executeServer(key, ServerState.class, state -> state.set(9));
        SyncDataHolder.applyDiff(key, SyncDataHolder.drainDirtyData().get(key));
        assertEquals(9, provider.client.value);
        assertTrue(SyncDataHolder.drainDirtyData().isEmpty());
        // The old reader API still adapts a decoder that did not extend ClientDataReader.
        SyncDataHolder.<ClientState>getReader(key).readFromIncomingFullSync(provider.client, full);
        assertEquals(7, provider.client.value);
        SyncDataHolder.applyFullSync(key, new CompoundTag());
        assertEquals(0, provider.client.value, "Full snapshots replace stale state");
    }

    @Test void duplicateRegistrationKeepsTheFirstStatesAndDoesNotRunFactories() {
        Provider first = new Provider(key);
        event.registerSyncData(first);
        first.server.set(5);
        Provider duplicate = new Provider(key);
        assertThrows(IllegalArgumentException.class, () -> event.registerSyncData(duplicate));
        assertEquals(0, duplicate.serverFactoryCalls);
        assertSame(first, SyncDataRegistry.getProvider(key));
        assertEquals(5, SyncDataHolder.computeServer(key, ServerState.class, state -> state.value).orElseThrow());
    }

    @Test void aBrokenReaderDoesNotLeaveAPartialRegistrationAndCanBeRetried() {
        Provider broken = new Provider(key);
        broken.nullReader = true;
        assertThrows(NullPointerException.class, () -> event.registerSyncData(broken));
        assertNull(SyncDataRegistry.getProvider(key));
        assertTrue(SyncDataHolder.computeServer(key, ServerState.class, state -> state.value).isEmpty());
        assertTrue(SyncDataHolder.computeClient(key, ClientState.class, state -> state.value).isEmpty());
        assertNull(SyncDataHolder.getReader(key));
        event.registerSyncData(new Provider(key));
        assertNotNull(SyncDataRegistry.getProvider(key));
    }

    @Test void mismatchedDirtyTrackingKeyIsRejectedBeforePublication() {
        Provider provider = new Provider(key);
        provider.server = new ServerState(new ResourceLocation("exampleaddon", "wrong"));
        assertThrows(IllegalArgumentException.class, () -> event.registerSyncData(provider));
        assertFalse(SyncDataRegistry.getKnownKeys().contains(key));
    }

    @Test void clearAllResetsBothSidesAndPendingChanges() {
        Provider provider = new Provider(key);
        event.registerSyncData(provider);
        provider.server.set(8);
        provider.client.value = 8;
        SyncDataHolder.clearAll();
        assertEquals(0, provider.server.value);
        assertEquals(0, provider.client.value);
        assertTrue(SyncDataHolder.drainDirtyData().isEmpty());
    }

    @Test void officialEntryPointRejectsReservedNamespacesAndNullProviders() {
        assertThrows(IllegalArgumentException.class, () -> event.registerSyncData(
                new Provider(new ResourceLocation("astralsorcery", "reserved"))));
        assertThrows(NullPointerException.class, () -> event.registerSyncData(null));
    }

    @Test void unknownChannelsAreIgnoredAndTheKeyViewCannotMutateTheRegistry() {
        assertDoesNotThrow(() -> SyncDataHolder.applyFullSync(key, new CompoundTag()));
        assertDoesNotThrow(() -> SyncDataHolder.applyDiff(key, new CompoundTag()));
        assertThrows(UnsupportedOperationException.class, () -> SyncDataRegistry.getKnownKeys().clear());
    }

    @Test void successfulRegistrationConstructsEachProductExactlyOnce() {
        Provider provider = new Provider(key);
        event.registerSyncData(provider);
        SyncDataHolder.computeServer(key, ServerState.class, state -> state.value);
        SyncDataHolder.computeClient(key, ClientState.class, state -> state.value);
        SyncDataHolder.getReader(key);
        SyncDataHolder.getReader(key);
        assertEquals(1, provider.serverFactoryCalls);
        assertEquals(1, provider.clientFactoryCalls);
        assertEquals(1, provider.readerFactoryCalls);
    }

    @Test void nullServerAndClientProductsNeverPublishAndTheSameIdCanBeRetried() {
        Provider nullServer = new Provider(key);
        nullServer.server = null;
        assertThrows(NullPointerException.class, () -> event.registerSyncData(nullServer));
        assertNull(SyncDataRegistry.getProvider(key));
        assertEquals(0, nullServer.clientFactoryCalls);
        Provider nullClient = new Provider(key);
        nullClient.nullClient = true;
        assertThrows(NullPointerException.class, () -> event.registerSyncData(nullClient));
        assertNull(SyncDataRegistry.getProvider(key));
        assertEquals(0, nullClient.readerFactoryCalls);
        event.registerSyncData(new Provider(key));
        assertNotNull(SyncDataRegistry.getProvider(key));
    }

    @Test void unknownChannelsNeverInvokeCallbacksAndWrongTypesFailBeforeCallbacks() {
        assertTrue(SyncDataHolder.computeServer(key, ServerState.class, state -> fail()).isEmpty());
        assertTrue(SyncDataHolder.computeClient(key, ClientState.class, state -> fail()).isEmpty());
        SyncDataHolder.executeServer(key, ServerState.class, state -> fail());
        SyncDataHolder.executeClient(key, ClientState.class, state -> fail());
        event.registerSyncData(new Provider(key));
        assertThrows(ClassCastException.class, () -> SyncDataHolder.executeClient(
                key, OtherClientState.class, state -> fail()));
        assertThrows(ClassCastException.class, () -> SyncDataHolder.computeClient(
                key, OtherClientState.class, state -> fail()));
        assertThrows(ClassCastException.class, () -> SyncDataHolder.executeServer(
                key, OtherServerState.class, state -> fail()));
        assertThrows(ClassCastException.class, () -> SyncDataHolder.computeServer(
                key, OtherServerState.class, state -> fail()));
    }

    @Test void nullCallbackResultsAreEmptyAndSnapshotsDoNotTrackLaterRegistrations() {
        var keysBefore = SyncDataRegistry.getKnownKeys();
        event.registerSyncData(new Provider(key));
        assertFalse(keysBefore.contains(key));
        assertTrue(SyncDataRegistry.getKnownKeys().contains(key));
        assertTrue(SyncDataHolder.computeServer(key, ServerState.class, state -> null).isEmpty());
        assertTrue(SyncDataHolder.computeClient(key, ClientState.class, state -> null).isEmpty());
    }

    private static final class OtherClientState extends ClientData<OtherClientState> {
        public void clear(ResourceKey<Level> dim) {}
        public void clearClient() {}
    }

    private abstract static class OtherServerState extends AbstractData {
        OtherServerState(ResourceLocation key) { super(key); }
    }

    private static final class Provider implements SyncDataProvider<ServerState, ClientState> {
        final ResourceLocation key;
        ServerState server;
        final ClientState client = new ClientState();
        int serverFactoryCalls;
        int clientFactoryCalls;
        int readerFactoryCalls;
        boolean nullClient;
        boolean nullReader;
        Provider(ResourceLocation key) { this.key = key; this.server = new ServerState(key); }
        public ResourceLocation getKey() { return key; }
        public ServerState provideServerData() { serverFactoryCalls++; return server; }
        public ClientState provideClientData() { clientFactoryCalls++; return nullClient ? null : client; }
        public SyncDataReader<ClientState> createReader() { readerFactoryCalls++; return nullReader ? null : new Reader(); }
    }

    private static final class ServerState extends AbstractData {
        int value;
        boolean changed;
        ServerState(ResourceLocation key) { super(key); }
        void set(int value) { this.value = value; changed = true; markDirty(); }
        public void clear(ResourceKey<Level> dim) {}
        public void clearServer() { value = 0; changed = false; }
        public void writeAllDataToPacket(CompoundTag tag) { tag.putInt("value", value); }
        public void writeDiffDataToPacket(CompoundTag tag) {
            if (changed) tag.putInt("value", value);
            changed = false;
        }
    }

    private static final class ClientState extends ClientData<ClientState> {
        int value;
        public void clear(ResourceKey<Level> dim) {}
        public void clearClient() { value = 0; }
    }

    private static class UnrelatedDecoderBase {}
    private static final class Reader extends UnrelatedDecoderBase implements SyncDataReader<ClientState> {
        public void readFromIncomingFullSync(ClientState data, CompoundTag tag) { data.value = tag.getInt("value"); }
        public void readFromIncomingDiff(ClientState data, CompoundTag tag) {
            if (tag.contains("value")) data.value = tag.getInt("value");
        }
    }
}
