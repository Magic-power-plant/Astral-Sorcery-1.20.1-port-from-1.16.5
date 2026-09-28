/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.sync.server;

import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffect;
import hellfirepvp.astralsorcery.common.base.patreon.entity.PatreonPartialEntity;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractDataProvider;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import hellfirepvp.astralsorcery.common.data.sync.client.ClientPatreonFlares;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DataPatreonFlares
 * Created by HellFirePvP
 * Date: 30.08.2019 / 17:36
 */
public class DataPatreonFlares extends AbstractData {

    private final Map<UUID, Set<PatreonPartialEntity>> entitiesServer = new HashMap<>();

    private final Set<UUID> flarePlayerUpdates = new HashSet<>();
    private final Set<UUID> flareRemovals = new HashSet<>();

    private DataPatreonFlares(ResourceLocation key) {
        super(key);
    }

    //Only actually called when there's an entity to be provided.
    @Nullable
    public PatreonPartialEntity createEntity(Player player, PatreonEffect value) {
        UUID owner = player.getUUID();
        PatreonPartialEntity entity = value.createEntity(owner);
        if (entity == null) {
            return null;
        }
        entity.placeNear(player);

        entitiesServer.computeIfAbsent(owner, o -> new HashSet<>()).add(entity);

        flareRemovals.remove(owner);
        flarePlayerUpdates.add(owner);
        markDirty();

        return entity;
    }

    public void updateEntitiesOf(UUID playerUUID) {
        flareRemovals.remove(playerUUID);
        flarePlayerUpdates.add(playerUUID);
        markDirty();
    }

    public void removeEntities(UUID playerUUID) {
        flarePlayerUpdates.remove(playerUUID);
        flareRemovals.add(playerUUID);
        markDirty();

        //BLOCKED_PORT[AS-SYNC-PF-02]: 1.16.5 flags every owned flare via PatreonPartialEntity#setRemoved(true) here
        // so the server tick handler drops it. The target's common/base/patreon/entity/PatreonPartialEntity is still
        // a stub without setRemoved(boolean), so the flag cannot be raised yet.
        // (1.16.5 common/data/sync/server/DataPatreonFlares.java:77)
        this.entitiesServer.getOrDefault(playerUUID, Collections.emptySet())
                .forEach(p -> {});
    }

    @Nonnull
    public Collection<UUID> getOwners() {
        return this.entitiesServer.keySet();
    }

    @Nonnull
    public Collection<PatreonPartialEntity> getEntities(UUID playerUUID) {
        return this.entitiesServer.getOrDefault(playerUUID, Collections.emptySet());
    }

    @Nonnull
    public Collection<Collection<PatreonPartialEntity>> getEntities() {
        return new ArrayList<>(this.entitiesServer.values());
    }

    @Override
    public void clear(ResourceKey<Level> dim) {}

    @Override
    public void clearServer() {
        this.entitiesServer.clear();
        this.flarePlayerUpdates.clear();
        this.flareRemovals.clear();
    }

    @Override
    public void writeAllDataToPacket(CompoundTag compound) {
        ListTag entities = new ListTag();
        for (UUID playerUUID : this.entitiesServer.keySet()) {
            CompoundTag tag = new CompoundTag();
            // 1.16.5 CompoundNBT#putUniqueId -> 1.20.1 CompoundTag#putUUID
            tag.putUUID("playerUUID", playerUUID);

            ListTag entityList = new ListTag();
            for (PatreonPartialEntity entity : this.entitiesServer.get(playerUUID)) {
                CompoundTag entityNBT = new CompoundTag();
                entityNBT.putUUID("id", entity.getEffectUUID());

                CompoundTag data = new CompoundTag();
                //BLOCKED_PORT[AS-SYNC-PF-02]: entity.writeToNBT(data) - PatreonPartialEntity has no NBT support in
                // the target yet, so the flare payload stays empty. (1.16.5 DataPatreonFlares.java:119)
                entityNBT.put("data", data);

                entityList.add(entityNBT);
            }
            tag.put("entityList", entityList);

            entities.add(tag);
        }
        compound.put("entities", entities);
    }

    @Override
    public void writeDiffDataToPacket(CompoundTag compound) {
        ListTag listUpdates = new ListTag();
        for (UUID playerUUID : this.flarePlayerUpdates) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("playerUUID", playerUUID);

            ListTag entityList = new ListTag();
            for (PatreonPartialEntity entity : this.entitiesServer.get(playerUUID)) {
                CompoundTag entityNBT = new CompoundTag();
                entityNBT.putUUID("id", entity.getEffectUUID());

                CompoundTag data = new CompoundTag();
                //BLOCKED_PORT[AS-SYNC-PF-02]: entity.writeToNBT(data) - see writeAllDataToPacket.
                // (1.16.5 DataPatreonFlares.java:144)
                entityNBT.put("data", data);

                entityList.add(entityNBT);
            }
            tag.put("entityList", entityList);
            listUpdates.add(tag);
        }

        ListTag listRemovals = new ListTag();
        for (UUID playerUUID : this.flareRemovals) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("playerUUID", playerUUID);
            listRemovals.add(playerTag);
        }

        compound.put("updates", listUpdates);
        compound.put("removals", listRemovals);

        this.flarePlayerUpdates.clear();
        this.flareRemovals.clear();
    }

    public static class Provider extends AbstractDataProvider<DataPatreonFlares, ClientPatreonFlares> {

        public Provider(ResourceLocation key) {
            super(key);
        }

        @Override
        public DataPatreonFlares provideServerData() {
            return new DataPatreonFlares(getKey());
        }

        @Override
        public ClientPatreonFlares provideClientData() {
            return new ClientPatreonFlares();
        }

        @Override
        public ClientDataReader<ClientPatreonFlares> createReader() {
            return new ClientPatreonFlares.Reader();
        }
    }
}
