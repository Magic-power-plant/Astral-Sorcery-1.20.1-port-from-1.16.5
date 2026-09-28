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

import java.util.*;

public class ClientLightBlockEndpoints extends ClientData<ClientLightBlockEndpoints> {

    private final Map<ResourceKey<Level>, Set<BlockPos>> clientPositions = new HashMap<>();

    // 1.16.5: world.getDimensionKey() -> 1.20.1 Level#dimension()
    public boolean doesPositionReceiveStarlightClient(Level world, BlockPos pos) {
        return this.clientPositions.getOrDefault(world.dimension(), Collections.emptySet()).contains(pos);
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        this.clientPositions.remove(dim);
    }

    @Override
    public void clearClient() {
        this.clientPositions.clear();
    }

    public static class Reader extends ClientDataReader<ClientLightBlockEndpoints> {

        @Override
        public void readFromIncomingFullSync(ClientLightBlockEndpoints data, CompoundTag compound) {
            data.clientPositions.clear();

            // 1.16.5: compound.keySet() -> 1.20.1 CompoundTag#getAllKeys()
            for (String dimKey : compound.getAllKeys()) {
                // 1.16.5: RegistryKey.getOrCreateKey(Registry.WORLD_KEY, ...) -> ResourceKey.create(Registries.DIMENSION, ...)
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimKey));

                Set<BlockPos> positions = new HashSet<>();
                // 1.16.5: Constants.NBT.TAG_COMPOUND -> 1.20.1 Tag.TAG_COMPOUND
                ListTag list = compound.getList(dimKey, Tag.TAG_COMPOUND);
                for (Tag iTag : list) {
                    CompoundTag tag = (CompoundTag) iTag;

                    // 1.16.5: BlockPos.fromLong -> 1.20.1 BlockPos.of
                    BlockPos pos = BlockPos.of(tag.getLong("pos"));
                    positions.add(pos);
                }
                data.clientPositions.put(dim, positions);
            }
        }

        @Override
        public void readFromIncomingDiff(ClientLightBlockEndpoints data, CompoundTag compound) {
            Set<String> clearedDimensions = new HashSet<>();
            for (Tag dimKeyNBT : compound.getList("clear", Tag.TAG_STRING)) {
                // 1.16.5: INBT#getString() -> 1.20.1 Tag#getAsString()
                String dimKey = dimKeyNBT.getAsString();
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimKey));
                data.clientPositions.remove(dim);

                clearedDimensions.add(dimKey);
            }

            for (String dimKey : compound.getAllKeys()) {
                if (clearedDimensions.contains(dimKey)) {
                    continue;
                }
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimKey));

                Set<BlockPos> positions = data.clientPositions.computeIfAbsent(dim, k -> new HashSet<>());

                ListTag list = compound.getList(dimKey, Tag.TAG_COMPOUND);
                for (Tag iTag : list) {
                    CompoundTag tag = (CompoundTag) iTag;

                    BlockPos pos = BlockPos.of(tag.getLong("pos"));
                    boolean addNew = tag.getBoolean("add");

                    if (addNew) {
                        positions.add(pos);
                    } else {
                        positions.remove(pos);
                    }
                }
            }
        }
    }
}
