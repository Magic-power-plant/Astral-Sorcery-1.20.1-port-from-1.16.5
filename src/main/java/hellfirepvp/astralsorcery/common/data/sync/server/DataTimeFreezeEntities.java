package hellfirepvp.astralsorcery.common.data.sync.server;

import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractDataProvider;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import hellfirepvp.astralsorcery.common.data.sync.client.ClientTimeFreezeEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class DataTimeFreezeEntities extends AbstractData {

    private final Set<Integer> frozenEntities = new HashSet<>();

    private DataTimeFreezeEntities(ResourceLocation key) {
        super(key);
    }

    public boolean isFrozen(Entity e) {
        return frozenEntities.contains(e.getId());
    }

    public void freeze(Entity e) {
        frozenEntities.add(e.getId());
    }

    public void unfreeze(Entity e) {
        frozenEntities.remove(e.getId());
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        frozenEntities.clear();
    }

    @Override
    public void clearServer() {
        frozenEntities.clear();
    }

    @Override
    public void writeAllDataToPacket(CompoundTag compound) { }

    @Override
    public void writeDiffDataToPacket(CompoundTag compound) { }

    public static class Provider extends AbstractDataProvider<DataTimeFreezeEntities, ClientTimeFreezeEntities> {

        public Provider(ResourceLocation key) {
            super(key);
        }

        @Override
        public DataTimeFreezeEntities provideServerData() {
            return new DataTimeFreezeEntities(getKey());
        }

        @Override
        public ClientTimeFreezeEntities provideClientData() {
            return new ClientTimeFreezeEntities();
        }

        @Override
        public ClientDataReader<ClientTimeFreezeEntities> createReader() {
            return new ClientTimeFreezeEntities.Reader();
        }
    }
}
