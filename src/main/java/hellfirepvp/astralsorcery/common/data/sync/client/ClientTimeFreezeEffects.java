package hellfirepvp.astralsorcery.common.data.sync.client;

import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import hellfirepvp.astralsorcery.common.data.sync.server.DataTimeFreezeEffects;
import hellfirepvp.astralsorcery.common.util.time.TimeStopEffectHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.*;

public class ClientTimeFreezeEffects extends ClientData<ClientTimeFreezeEffects> {

    private final Map<ResourceKey<Level>, List<TimeStopEffectHelper>> clientActiveFreezeZones = new HashMap<>();

    @Nonnull
    public List<TimeStopEffectHelper> getTimeStopEffects(Level world) {
        // 1.16.5: world.getDimensionKey() -> 1.20.1 Level#dimension()
        return getTimeStopEffects(world.dimension());
    }

    @Nonnull
    public List<TimeStopEffectHelper> getTimeStopEffects(ResourceKey<Level> dim) {
        return clientActiveFreezeZones.getOrDefault(dim, Collections.emptyList());
    }

    private void applyChange(DataTimeFreezeEffects.ServerSyncAction action) {
        ResourceKey<Level> worldKey = action.getDimKey();
        switch (action.getType()) {
            case ADD:
                List<TimeStopEffectHelper> zones = clientActiveFreezeZones.computeIfAbsent(worldKey, (id) -> new LinkedList<>());
                zones.add(action.getInvolvedEffect());
                break;
            case REMOVE:
                if (clientActiveFreezeZones.containsKey(worldKey)) {
                    clientActiveFreezeZones.get(worldKey).remove(action.getInvolvedEffect());
                }
                break;
            case CLEAR:
                clientActiveFreezeZones.remove(worldKey);
                break;
            default:
                break;
        }
    }

    @Override
    public void clear(ResourceKey<Level> dim) {
        this.clientActiveFreezeZones.remove(dim);
    }

    @Override
    public void clearClient() {
        this.clientActiveFreezeZones.clear();
    }

    public static class Reader extends ClientDataReader<ClientTimeFreezeEffects> {

        @Override
        public void readFromIncomingFullSync(ClientTimeFreezeEffects data, CompoundTag compound) {
            data.clientActiveFreezeZones.clear();

            CompoundTag dimTag = compound.getCompound("dimTypes");
            // 1.16.5: dimTag.keySet() -> 1.20.1 CompoundTag#getAllKeys()
            for (String dimKey : dimTag.getAllKeys()) {
                // 1.16.5: RegistryKey.getOrCreateKey(Registry.WORLD_KEY, ...) -> ResourceKey.create(Registries.DIMENSION, ...)
                ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dimKey));

                List<TimeStopEffectHelper> effects = new LinkedList<>();
                // 1.16.5: Constants.NBT.TAG_COMPOUND -> 1.20.1 Tag.TAG_COMPOUND
                ListTag listEffects = dimTag.getList(dimKey, Tag.TAG_COMPOUND);
                for (Tag iNBT : listEffects) {
                    effects.add(TimeStopEffectHelper.deserializeNBT((CompoundTag) iNBT));
                }
                data.clientActiveFreezeZones.put(dim, effects);
            }
        }

        @Override
        public void readFromIncomingDiff(ClientTimeFreezeEffects data, CompoundTag compound) {
            ListTag changes = compound.getList("changes", Tag.TAG_COMPOUND);
            for (Tag iNBT : changes) {
                DataTimeFreezeEffects.ServerSyncAction action = DataTimeFreezeEffects.ServerSyncAction.deserializeNBT((CompoundTag) iNBT);
                data.applyChange(action);
            }
        }
    }
}
