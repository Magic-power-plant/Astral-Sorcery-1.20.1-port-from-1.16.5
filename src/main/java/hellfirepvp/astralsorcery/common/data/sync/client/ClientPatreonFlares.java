/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.sync.client;

import hellfirepvp.astralsorcery.common.patreon.entity.PatreonFlare;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonPartialEntity;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;
import java.util.*;

/** Side-neutral mirror. Network decoding never creates visual resources or consults render config. */
public class ClientPatreonFlares extends ClientData<ClientPatreonFlares> {
    private final Map<UUID, Set<PatreonPartialEntity>> entitiesClient = new HashMap<>();
    @Nonnull
    public Collection<UUID> getOwners() { return entitiesClient.keySet(); }
    @Nonnull
    public Collection<PatreonPartialEntity> getEntities(UUID playerUUID) {
        return entitiesClient.getOrDefault(playerUUID, Collections.emptySet());
    }
    @Nonnull
    public Collection<Collection<PatreonPartialEntity>> getEntities() { return new ArrayList<>(entitiesClient.values()); }

    private void removeOwner(UUID owner) {
        Set<PatreonPartialEntity> removed = entitiesClient.remove(owner);
        if (removed != null) removed.forEach(entity -> entity.setRemoved(true));
    }
    @Override
    public void clear(ResourceKey<Level> dim) {
        entitiesClient.values().forEach(entities -> entities.removeIf(entity -> {
            if (!dim.equals(entity.getLastTickedDimension())) return false;
            entity.setRemoved(true);
            return true;
        }));
        entitiesClient.values().removeIf(Set::isEmpty);
    }
    @Override
    public void clearClient() {
        entitiesClient.values().forEach(entities -> entities.forEach(entity -> entity.setRemoved(true)));
        entitiesClient.clear();
    }

    public static class Reader extends ClientDataReader<ClientPatreonFlares> {
        @Override
        public void readFromIncomingFullSync(ClientPatreonFlares data, CompoundTag compound) {
            data.clearClient();
            readOwners(data, compound, "entities");
        }
        @Override
        public void readFromIncomingDiff(ClientPatreonFlares data, CompoundTag compound) {
            readOwners(data, compound, "updates");
            for (Tag entry : compound.getList("removals", Tag.TAG_COMPOUND)) {
                data.removeOwner(((CompoundTag) entry).getUUID("playerUUID"));
            }
        }
        private void readOwners(ClientPatreonFlares data, CompoundTag compound, String key) {
            for (Tag entry : compound.getList(key, Tag.TAG_COMPOUND)) {
                CompoundTag ownerTag = (CompoundTag) entry;
                UUID owner = ownerTag.getUUID("playerUUID");
                Map<UUID, PatreonPartialEntity> existing = new HashMap<>();
                data.getEntities(owner).forEach(entity -> existing.put(entity.getEffectUUID(), entity));
                Set<PatreonPartialEntity> replacement = new HashSet<>();
                for (Tag entityEntry : ownerTag.getList("entityList", Tag.TAG_COMPOUND)) {
                    CompoundTag entityTag = (CompoundTag) entityEntry;
                    UUID id = entityTag.getUUID("id");
                    PatreonPartialEntity entity = existing.remove(id);
                    if (entity == null) entity = new PatreonFlare(id, owner);
                    entity.readFromNBT(entityTag.getCompound("data"));
                    replacement.add(entity);
                }
                existing.values().forEach(entity -> entity.setRemoved(true));
                if (replacement.isEmpty()) data.entitiesClient.remove(owner);
                else data.entitiesClient.put(owner, replacement);
            }
        }
    }
}
