package hellfirepvp.astralsorcery.common.data.sync.server;

import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractDataProvider;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import hellfirepvp.astralsorcery.common.data.sync.client.ClientLightConnections;
import hellfirepvp.astralsorcery.common.starlight.network.LightConnection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.util.Tuple;

import java.util.*;

public class DataLightConnections extends AbstractData {

    private final Map<ResourceKey<Level>, Map<BlockPos, Set<BlockPos>>> serverPosBuffer = new HashMap<>();

    private final Map<ResourceKey<Level>, LinkedList<Tuple<LightConnection, Boolean>>> serverChangeBuffer = new HashMap<>();
    private final Set<ResourceKey<Level>> dimensionClearBuffer = new HashSet<>();

    private DataLightConnections(ResourceLocation key) {
        super(key);
    }

    public void updateNewConnectionsThreaded(ResourceKey<Level> dim, List<LightConnection> newlyAddedConnections) {
        Map<BlockPos, Set<BlockPos>> posBufferDim = serverPosBuffer.computeIfAbsent(dim, k -> new HashMap<>());
        for (LightConnection c : newlyAddedConnections) {
            BlockPos start = c.getStart();
            Set<BlockPos> endpoints = posBufferDim.computeIfAbsent(start, k -> new HashSet<>());
            endpoints.add(c.getEnd());
        }
        notifyConnectionAdd(dim, newlyAddedConnections);
        if (newlyAddedConnections.size() > 0) {
            markDirty();
        }
    }

    public void removeOldConnectionsThreaded(ResourceKey<Level> dim, List<LightConnection> invalidConnections) {
        Map<BlockPos, Set<BlockPos>> posBufferDim = serverPosBuffer.get(dim);
        if (posBufferDim != null) {
            for (LightConnection c : invalidConnections) {
                BlockPos start = c.getStart();
                Set<BlockPos> ends = posBufferDim.get(start);
                if (ends == null) continue;
                ends.remove(c.getEnd());
                if (ends.isEmpty()) {
                    posBufferDim.remove(start);
                }
            }
        }
        notifyConnectionRemoval(dim, invalidConnections);
        if (invalidConnections.size() > 0) {
            markDirty();
        }
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        if (this.serverPosBuffer.remove(dim) != null) {
            this.dimensionClearBuffer.add(dim);
            markDirty();
        }
    }

    @Override
    public void clearServer() {
        this.dimensionClearBuffer.clear();
        this.serverChangeBuffer.clear();
        this.serverPosBuffer.clear();
    }

    private void notifyConnectionAdd(ResourceKey<Level> dim, List<LightConnection> added) {
        LinkedList<Tuple<LightConnection, Boolean>> ch = serverChangeBuffer.computeIfAbsent(dim, k -> new LinkedList<>());
        for (LightConnection l : added) {
            ch.add(new Tuple<>(l, true));
        }
        this.dimensionClearBuffer.remove(dim);
    }

    private void notifyConnectionRemoval(ResourceKey<Level> dim, List<LightConnection> removal) {
        LinkedList<Tuple<LightConnection, Boolean>> ch = serverChangeBuffer.computeIfAbsent(dim, k -> new LinkedList<>());
        for (LightConnection l : removal) {
            ch.add(new Tuple<>(l, false));
        }
    }

    @Override
    public void writeAllDataToPacket(CompoundTag compound) {
        for (ResourceKey<Level> dim : serverPosBuffer.keySet()) {
            Map<BlockPos, Set<BlockPos>> dat = serverPosBuffer.get(dim);
            ListTag dataList = new ListTag();
            for (BlockPos start : dat.keySet()) {
                Set<BlockPos> endPositions = dat.get(start);
                if (endPositions == null) continue;
                for (BlockPos end : endPositions) {
                    CompoundTag cmp = new CompoundTag();
                    cmp.putLong("start", start.asLong());
                    cmp.putLong("end", end.asLong());
                    dataList.add(cmp);
                }
            }
            compound.put(dim.location().toString(), dataList);
        }
    }

    @Override
    public void writeDiffDataToPacket(CompoundTag compound) {
        ListTag clearList = new ListTag();
        for (ResourceKey<Level> dim : this.dimensionClearBuffer) {
            clearList.add(StringTag.valueOf(dim.location().toString()));
        }
        compound.put("clear", clearList);

        for (ResourceKey<Level> dim : serverChangeBuffer.keySet()) {
            if (this.dimensionClearBuffer.contains(dim)) continue;

            LinkedList<Tuple<LightConnection, Boolean>> changes = serverChangeBuffer.get(dim);
            if (!changes.isEmpty()) {
                ListTag list = new ListTag();
                for (Tuple<LightConnection, Boolean> tuple : changes) {
                    CompoundTag connection = new CompoundTag();
                    connection.putLong("start", tuple.getA().getStart().asLong());
                    connection.putLong("end", tuple.getA().getEnd().asLong());
                    connection.putBoolean("connect", tuple.getB());
                    list.add(connection);
                }
                compound.put(dim.location().toString(), list);
            }
        }

        this.dimensionClearBuffer.clear();
        this.serverChangeBuffer.clear();
    }

    public static class Provider extends AbstractDataProvider<DataLightConnections, ClientLightConnections> {

        public Provider(ResourceLocation key) {
            super(key);
        }

        @Override
        public DataLightConnections provideServerData() {
            return new DataLightConnections(getKey());
        }

        @Override
        public ClientLightConnections provideClientData() {
            return new ClientLightConnections();
        }

        @Override
        public ClientDataReader<ClientLightConnections> createReader() {
            return new ClientLightConnections.Reader();
        }
    }

}
