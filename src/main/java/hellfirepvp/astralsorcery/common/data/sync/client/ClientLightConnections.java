package hellfirepvp.astralsorcery.common.data.sync.client;

import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientLightConnections extends ClientData<ClientLightConnections> {

    private final Map<ResourceKey<Level>, Map<BlockPos, Set<BlockPos>>> clientPosBuffer = new HashMap<>();

    @Nonnull
    public Map<BlockPos, Set<BlockPos>> getClientConnections(ResourceKey<Level> dim) {
        return this.clientPosBuffer.getOrDefault(dim, new HashMap<>());
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        this.clientPosBuffer.remove(dim);
    }

    @Override
    public void clearClient() {
        this.clientPosBuffer.clear();
    }

    public static class Reader extends ClientDataReader<ClientLightConnections> {

        @Override
        public void readFromIncomingFullSync(ClientLightConnections cl, CompoundTag compound) {
            cl.clientPosBuffer.clear();

            // 1.16.5: compound.keySet() -> 1.20.1 CompoundTag#getAllKeys()
            for (String dimKey : compound.getAllKeys()) {
                // 1.16.5: RegistryKey.getOrCreateKey(Registry.WORLD_KEY, ...) -> ResourceKey.create(Registries.DIMENSION, ...)
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimKey));

                Map<BlockPos, Set<BlockPos>> posMap = new HashMap<>();
                // 1.16.5: Constants.NBT.TAG_COMPOUND -> 1.20.1 Tag.TAG_COMPOUND
                ListTag list = compound.getList(dimKey, Tag.TAG_COMPOUND);
                for (Tag iTag : list) {
                    CompoundTag tag = (CompoundTag) iTag;

                    // 1.16.5: BlockPos.fromLong -> 1.20.1 BlockPos.of
                    BlockPos start = BlockPos.of(tag.getLong("start"));
                    BlockPos end   = BlockPos.of(tag.getLong("end"));
                    posMap.computeIfAbsent(start, s -> new HashSet<>())
                            .add(end);
                }

                cl.clientPosBuffer.put(dim, posMap);
            }
        }

        @Override
        public void readFromIncomingDiff(ClientLightConnections cl, CompoundTag compound) {
            Set<String> clearedDimensions = new HashSet<>();
            for (Tag dimKeyNBT : compound.getList("clear", Tag.TAG_STRING)) {
                // 1.16.5: INBT#getString() -> 1.20.1 Tag#getAsString()
                String dimKey = dimKeyNBT.getAsString();
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimKey));
                cl.clientPosBuffer.remove(dim);

                clearedDimensions.add(dimKey);
            }

            for (String dimKey : compound.getAllKeys()) {
                if (clearedDimensions.contains(dimKey)) {
                    continue;
                }
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimKey));

                Map<BlockPos, Set<BlockPos>> posMap = cl.clientPosBuffer.computeIfAbsent(dim, d -> new HashMap<>());

                ListTag list = compound.getList(dimKey, Tag.TAG_COMPOUND);
                for (Tag iTag : list) {
                    CompoundTag tag = (CompoundTag) iTag;

                    BlockPos start = BlockPos.of(tag.getLong("start"));
                    BlockPos end = BlockPos.of(tag.getLong("end"));
                    boolean newConnection = tag.getBoolean("connect");

                    if (newConnection) {
                        posMap.computeIfAbsent(start, s -> new HashSet<>())
                                .add(end);
                    } else {
                        Set<BlockPos> endPoints = posMap.get(start);
                        if (endPoints != null &&
                                endPoints.remove(end) &&
                                endPoints.isEmpty()) {

                            posMap.remove(start);
                        }
                    }
                }
            }
        }
    }
}
