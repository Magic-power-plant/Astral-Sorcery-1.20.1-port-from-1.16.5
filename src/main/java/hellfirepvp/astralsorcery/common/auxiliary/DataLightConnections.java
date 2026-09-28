package hellfirepvp.astralsorcery.common.auxiliary;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;

public class DataLightConnections {

    private final Map<BlockPos, ConnectionInfo> connections = new HashMap<>();

    public void addConnection(BlockPos source, BlockPos target, float strength) {
        connections.put(source, new ConnectionInfo(target, strength));
    }

    public ConnectionInfo getConnection(BlockPos source) {
        return connections.get(source);
    }

    public boolean hasConnection(BlockPos source) {
        return connections.containsKey(source);
    }

    public void removeConnection(BlockPos source) {
        connections.remove(source);
    }

    public Map<BlockPos, ConnectionInfo> getAllConnections() {
        return new HashMap<>(connections);
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        CompoundTag connTag = new CompoundTag();
        for (Map.Entry<BlockPos, ConnectionInfo> entry : connections.entrySet()) {
            String key = entry.getKey().getX() + "," + entry.getKey().getY() + "," + entry.getKey().getZ();
            CompoundTag infoTag = new CompoundTag();
            infoTag.putLong("target", entry.getValue().target.asLong());
            infoTag.putFloat("strength", entry.getValue().strength);
            connTag.put(key, infoTag);
        }
        nbt.put("connections", connTag);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        connections.clear();
        if (nbt.contains("connections", CompoundTag.TAG_COMPOUND)) {
            CompoundTag connTag = nbt.getCompound("connections");
            for (String key : connTag.getAllKeys()) {
                String[] parts = key.split(",");
                BlockPos source = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                CompoundTag infoTag = connTag.getCompound(key);
                BlockPos target = BlockPos.of(infoTag.getLong("target"));
                float strength = infoTag.getFloat("strength");
                connections.put(source, new ConnectionInfo(target, strength));
            }
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(connections.size());
        for (Map.Entry<BlockPos, ConnectionInfo> entry : connections.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            buf.writeBlockPos(entry.getValue().target);
            buf.writeFloat(entry.getValue().strength);
        }
    }

    public void fromBytes(FriendlyByteBuf buf) {
        connections.clear();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            BlockPos source = buf.readBlockPos();
            BlockPos target = buf.readBlockPos();
            float strength = buf.readFloat();
            connections.put(source, new ConnectionInfo(target, strength));
        }
    }

    public static class ConnectionInfo {
        public final BlockPos target;
        public final float strength;

        public ConnectionInfo(BlockPos target, float strength) {
            this.target = target;
            this.strength = strength;
        }
    }
}