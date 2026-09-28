package hellfirepvp.astralsorcery.common.data.sync.client;

import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.*;

public class ClientTimeFreezeEntities extends ClientData<ClientTimeFreezeEntities> {

    private final Map<ResourceKey<Level>, Set<Integer>> clientActiveEntityFreeze = new HashMap<>();

    // 1.16.5: e.getEntityWorld().getDimensionKey() / e.getEntityId() -> 1.20.1 Entity#level()#dimension() / Entity#getId()
    public boolean isFrozen(Entity e) {
        return this.clientActiveEntityFreeze.getOrDefault(e.level().dimension(), Collections.emptySet()).contains(e.getId());
    }

    @Override
    public void clear(ResourceKey<Level> dimType) {
        this.clientActiveEntityFreeze.remove(dimType);
    }

    @Override
    public void clearClient() {
        this.clientActiveEntityFreeze.clear();
    }

    public static class Reader extends ClientDataReader<ClientTimeFreezeEntities> {

        @Override
        public void readFromIncomingFullSync(ClientTimeFreezeEntities data, CompoundTag compound) {
            this.readEntityInformation(data, compound);
        }

        @Override
        public void readFromIncomingDiff(ClientTimeFreezeEntities data, CompoundTag compound) {
            this.readEntityInformation(data, compound);
        }

        private void readEntityInformation(ClientTimeFreezeEntities data, CompoundTag compound) {
            CompoundTag dimTypes = compound.getCompound("dimTypes");
            // 1.16.5: dimTypes.keySet() -> 1.20.1 CompoundTag#getAllKeys()
            for (String key : dimTypes.getAllKeys()) {
                // 1.16.5: RegistryKey.getOrCreateKey(Registry.WORLD_KEY, ...) -> ResourceKey.create(Registries.DIMENSION, ...)
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(key));

                // 1.16.5: Constants.NBT.TAG_INT -> 1.20.1 Tag.TAG_INT
                ListTag list = dimTypes.getList(key, Tag.TAG_INT);
                Set<Integer> entities = new HashSet<>();
                for (int i = 0; i < list.size(); i++) {
                    entities.add(list.getInt(i));
                }

                data.clientActiveEntityFreeze.put(dim, entities);
            }
        }
    }
}
