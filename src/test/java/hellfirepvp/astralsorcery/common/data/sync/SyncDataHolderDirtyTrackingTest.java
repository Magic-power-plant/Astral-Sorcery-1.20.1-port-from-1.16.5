package hellfirepvp.astralsorcery.common.data.sync;

import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code SyncDataHolder.markForUpdate} was an empty synchronized block ("Will be wired to actual sync when
 * PktSyncData is ported") and the holder was not an {@link ITickHandler} at all, so nothing ever broadcast
 * {@code PktSyncData} even though both ends of that packet were fully implemented.
 * <p>
 * These tests cover the server half: marking data dirty must produce a diff payload keyed by the data's
 * ResourceLocation, and draining must be one-shot. The broadcast itself needs a running server.
 * <p>
 * Nothing here touches a dimension key: building a {@code ResourceKey<Level>} pulls in
 * {@code BuiltInRegistries}, whose initialiser refuses to run outside a game launch.
 */
class SyncDataHolderDirtyTrackingTest {

    @BeforeAll
    static void registerProviders() {
        //The same call CommonProxy#initialize makes.
        SyncDataHolder.initialize();
    }

    @BeforeEach
    void clearPending() {
        SyncDataHolder.drainDirtyData();
    }

    @Test
    void markingDataDirtyProducesADiffPayloadForThatKey() {
        //AbstractData#markDirty is the route every data object takes; it calls markForUpdate(this.key).
        SyncDataHolder.executeServer(SyncDataHolder.DATA_LIGHT_BLOCK_ENDPOINTS, AbstractData.class,
                AbstractData::markDirty);

        Map<ResourceLocation, CompoundTag> payload = SyncDataHolder.drainDirtyData();

        assertTrue(payload.containsKey(SyncDataHolder.DATA_LIGHT_BLOCK_ENDPOINTS),
                "AbstractData#markDirty must reach SyncDataHolder's dirty set");
        assertNotNull(payload.get(SyncDataHolder.DATA_LIGHT_BLOCK_ENDPOINTS));
    }

    @Test
    void drainingClearsTheDirtySetSoTheNextTickSendsNothing() {
        SyncDataHolder.markForUpdate(SyncDataHolder.DATA_LIGHT_CONNECTIONS);

        assertFalse(SyncDataHolder.drainDirtyData().isEmpty(), "the marked key must be drained once");
        assertTrue(SyncDataHolder.drainDirtyData().isEmpty(),
                "a second drain must be empty, otherwise every tick would re-broadcast the same diff");
    }

    @Test
    void unknownKeysAreDrainedWithoutProducingAPayload() {
        SyncDataHolder.markForUpdate(new ResourceLocation("astralsorcery", "not_a_registered_data_key"));

        //1.16.5 skips keys with no server data rather than writing an empty tag for them.
        assertTrue(SyncDataHolder.drainDirtyData().isEmpty());
    }

    @Test
    void theHolderTicksOnServerEndOnly() {
        ITickHandler handler = SyncDataHolder.getTickInstance();

        assertEquals(EnumSet.of(TickEvent.Type.SERVER), handler.getHandledTypes(),
                "a client-side tick would try to broadcast from the client");
        assertTrue(handler.canFire(TickEvent.Phase.END));
        assertFalse(handler.canFire(TickEvent.Phase.START));
    }

    /**
     * Guards the provider list: a provider whose {@code provideServerData()} returned nothing would make
     * {@code markForUpdate} silently produce no payload for that key forever.
     */
    @Test
    void everyRegisteredProviderHasServerDataToDiff() {
        assertTrue(SyncDataRegistry.getKnownKeys().containsAll(java.util.List.of(
                SyncDataHolder.DATA_LIGHT_CONNECTIONS, SyncDataHolder.DATA_LIGHT_BLOCK_ENDPOINTS,
                SyncDataHolder.DATA_TIME_FREEZE_EFFECTS, SyncDataHolder.DATA_TIME_FREEZE_ENTITIES,
                SyncDataHolder.DATA_PATREON_FLARES)));
        for (ResourceLocation key : SyncDataRegistry.getKnownKeys()) {
            SyncDataHolder.markForUpdate(key);
        }

        Map<ResourceLocation, CompoundTag> payload = SyncDataHolder.drainDirtyData();

        for (ResourceLocation key : SyncDataRegistry.getKnownKeys()) {
            assertTrue(payload.containsKey(key), key + " produced no diff payload");
        }
    }
}
