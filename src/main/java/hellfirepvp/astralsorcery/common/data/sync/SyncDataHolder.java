package hellfirepvp.astralsorcery.common.data.sync;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.api.sync.SyncDataProvider;
import hellfirepvp.astralsorcery.api.sync.SyncDataReader;
import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractDataProvider;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import hellfirepvp.astralsorcery.common.data.sync.server.*;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

//1.16.5 common/data/sync/SyncDataHolder.java is an ITickHandler: AbstractData#markDirty routes to
//markForUpdate(key), and the server tick drains the dirty set into a PktSyncData broadcast. The port kept the
//data registry but left markForUpdate empty ("Will be wired to actual sync when PktSyncData is ported") and
//never registered the holder as a tick handler, so PktSyncData - fully implemented on both ends - was never
//sent: light connections, block endpoints, time freeze state and patreon flares never reached any client.
public class SyncDataHolder implements ITickHandler {

    private static final SyncDataHolder tickInstance = new SyncDataHolder();

    public static final ResourceLocation DATA_LIGHT_CONNECTIONS = AstralSorcery.key("connections");
    public static final ResourceLocation DATA_LIGHT_BLOCK_ENDPOINTS = AstralSorcery.key("endpoints");
    public static final ResourceLocation DATA_TIME_FREEZE_EFFECTS = AstralSorcery.key("time_freeze");
    public static final ResourceLocation DATA_TIME_FREEZE_ENTITIES = AstralSorcery.key("time_freeze_entities");
    public static final ResourceLocation DATA_PATREON_FLARES = AstralSorcery.key("patreon");

    private static final Set<ResourceLocation> dirtyData = new HashSet<>();
    private static final Object lock = new Object();

    private SyncDataHolder() {}

    public static SyncDataHolder getTickInstance() {
        return tickInstance;
    }

    public static void register(AbstractDataProvider<? extends AbstractData, ? extends ClientData> provider) {
        register((SyncDataProvider<?, ?>) provider);
    }

    public static void register(SyncDataProvider<?, ?> provider) {
        synchronized (lock) { SyncDataRegistry.register(provider); }
    }

    public static <T extends AbstractData> void executeServer(ResourceLocation key, Class<T> typeHint, Consumer<T> function) {
        computeServer(key, typeHint, data -> { function.accept(data); return null; });
    }

    public static <T extends AbstractData, V> Optional<V> computeServer(ResourceLocation key, Class<T> typeHint, Function<T, V> function) {
        synchronized (lock) {
            RegisteredSyncData<?, ?> entry = SyncDataRegistry.getData(key);
            T data = entry == null ? null : typeHint.cast(entry.server);
            if (data != null) {
                return Optional.ofNullable(function.apply(data));
            }
            return Optional.empty();
        }
    }

    public static <T extends ClientData<T>> void executeClient(ResourceLocation key, Class<T> typeHint, Consumer<T> function) {
        RegisteredSyncData<?, ?> entry = SyncDataRegistry.getData(key);
        T data = entry == null ? null : typeHint.cast(entry.client);
        if (data != null) {
            function.accept(data);
        }
    }

    public static <T extends ClientData<T>, V> Optional<V> computeClient(ResourceLocation key, Class<T> typeHint, Function<T, V> function) {
        RegisteredSyncData<?, ?> entry = SyncDataRegistry.getData(key);
        T data = entry == null ? null : typeHint.cast(entry.client);
        if (data != null) {
            return Optional.ofNullable(function.apply(data));
        }
        return Optional.empty();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends ClientData<T>> ClientDataReader<T> getReader(ResourceLocation key) {
        RegisteredSyncData<?, ?> entry = SyncDataRegistry.getData(key);
        if (entry == null) return null;
        SyncDataReader<T> reader = (SyncDataReader<T>) entry.reader;
        if (reader instanceof ClientDataReader<?>) return (ClientDataReader<T>) reader;
        return new ClientDataReader<>() {
            public void readFromIncomingFullSync(T data, CompoundTag tag) { reader.readFromIncomingFullSync(data, tag); }
            public void readFromIncomingDiff(T data, CompoundTag tag) { reader.readFromIncomingDiff(data, tag); }
        };
    }

    public static void applyFullSync(ResourceLocation key, CompoundTag tag) {
        RegisteredSyncData<?, ?> entry = SyncDataRegistry.getData(key);
        if (entry != null) entry.readFull(Objects.requireNonNull(tag));
    }

    public static void applyDiff(ResourceLocation key, CompoundTag tag) {
        RegisteredSyncData<?, ?> entry = SyncDataRegistry.getData(key);
        if (entry != null) entry.readDiff(Objects.requireNonNull(tag));
    }

    public static void markForUpdate(ResourceLocation key) {
        synchronized (lock) {
            dirtyData.add(key);
        }
    }

    public static void clearWorld(ResourceKey<Level> dim) {
        for (ResourceLocation key : SyncDataRegistry.getKnownKeys()) {
            executeServer(key, AbstractData.class, data -> data.clear(dim));
        }
    }

    public static void clearAll() {
        synchronized (lock) {
            for (ResourceLocation key : SyncDataRegistry.getKnownKeys()) {
                RegisteredSyncData<?, ?> entry = SyncDataRegistry.getData(key);
                entry.server.clearServer();
                entry.client.clearClient();
            }
            dirtyData.clear();
        }
    }

    public static void initialize() {
        register(new DataLightConnections.Provider(DATA_LIGHT_CONNECTIONS));
        register(new DataLightBlockEndpoints.Provider(DATA_LIGHT_BLOCK_ENDPOINTS));
        register(new DataTimeFreezeEffects.Provider(DATA_TIME_FREEZE_EFFECTS));
        register(new DataTimeFreezeEntities.Provider(DATA_TIME_FREEZE_ENTITIES));
        register(new DataPatreonFlares.Provider(DATA_PATREON_FLARES));
    }

    /**
     * Drains the dirty set into one {@link PktSyncData} broadcast, exactly as 1.16.5
     * SyncDataHolder#tick (common/data/sync/SyncDataHolder.java:134-153) does.
     */
    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Map<ResourceLocation, CompoundTag> pktData = drainDirtyData();
        if (pktData.isEmpty()) {
            return;
        }
        PacketChannel.sendToAll(new PktSyncData(pktData));
    }

    /**
     * Collects a diff NBT tag for every dirty data key and clears the dirty set.
     * <p>
     * 1.16.5 checks {@code dirtyData.isEmpty()} before taking the lock; the check happens inside the lock here
     * because {@code markForUpdate} is reachable from the light-network threads. Same observable behaviour, minus
     * the race on the unsynchronised read.
     * <p>
     * Not private: this is the only seam through which a test can observe that marking data dirty actually
     * produces a payload, since {@link #tick(TickEvent.Type, Object...)} needs a running server to broadcast.
     */
    static Map<ResourceLocation, CompoundTag> drainDirtyData() {
        Map<ResourceLocation, CompoundTag> pktData = new HashMap<>();
        synchronized (lock) {
            if (dirtyData.isEmpty()) {
                return pktData;
            }
            for (ResourceLocation key : dirtyData) {
                RegisteredSyncData<?, ?> entry = SyncDataRegistry.getData(key);
                AbstractData data = entry == null ? null : entry.server;
                if (data != null) {
                    CompoundTag nbt = new CompoundTag();
                    data.writeDiffDataToPacket(nbt);
                    pktData.put(key, nbt);
                }
            }
            dirtyData.clear();
        }
        return pktData;
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.SERVER);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "Sync Data Holder";
    }

}
