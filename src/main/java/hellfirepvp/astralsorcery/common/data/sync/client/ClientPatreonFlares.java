/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.sync.client;

import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffect;
import hellfirepvp.astralsorcery.common.base.patreon.PatreonEffectHelper;
import hellfirepvp.astralsorcery.common.base.patreon.entity.PatreonPartialEntity;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientPatreonFlares
 * Created by HellFirePvP
 * Date: 30.08.2019 / 17:39
 */
public class ClientPatreonFlares extends ClientData<ClientPatreonFlares> {

    private final Map<UUID, Set<PatreonPartialEntity>> entitiesClient = new HashMap<>();

    @Nonnull
    public Collection<UUID> getOwners() {
        return this.entitiesClient.keySet();
    }

    @Nonnull
    public Collection<PatreonPartialEntity> getEntities(UUID playerUUID) {
        return this.entitiesClient.getOrDefault(playerUUID, Collections.emptySet());
    }

    @Nonnull
    public Collection<Collection<PatreonPartialEntity>> getEntities() {
        return new ArrayList<>(this.entitiesClient.values());
    }

    @Override
    public void clear(ResourceKey<Level> dim) {}

    @Override
    public void clearClient() {
        this.entitiesClient.clear();
    }

    public static class Reader extends ClientDataReader<ClientPatreonFlares> {

        @Override
        public void readFromIncomingFullSync(ClientPatreonFlares data, CompoundTag compound) {
            data.entitiesClient.clear();

            // 1.16.5 used Forge's Constants.NBT.TAG_COMPOUND; 1.20.1 exposes the same id as Tag.TAG_COMPOUND.
            ListTag entities = compound.getList("entities", Tag.TAG_COMPOUND);
            for (Tag iNBT : entities) {
                CompoundTag tag = (CompoundTag) iNBT;

                // 1.16.5 CompoundNBT#getUniqueId -> 1.20.1 CompoundTag#getUUID
                UUID playerUUID = tag.getUUID("playerUUID");
                Set<PatreonPartialEntity> entitySet = new HashSet<>();

                ListTag entityList = tag.getList("entityList", Tag.TAG_COMPOUND);
                for (Tag iEntityTag : entityList) {
                    CompoundTag entityNBT = (CompoundTag) iEntityTag;

                    UUID effectUUID = entityNBT.getUUID("id");
                    PatreonEffect effect = PatreonEffectHelper.getPatreonEffects(LogicalSide.CLIENT, playerUUID)
                            .stream()
                            .filter(eff -> eff.getEffectUUID().equals(effectUUID))
                            .findFirst()
                            .orElse(null);
                    if (effect == null) {
                        continue;
                    }
                    PatreonPartialEntity entity = effect.createEntity(playerUUID);
                    if (entity == null) {
                        continue;
                    }
                    //BLOCKED_PORT[AS-SYNC-PF-01]: entity.readFromNBT(entityNBT.getCompound("data")) - the target's
                    // common/base/patreon/entity/PatreonPartialEntity is still a stub without readFromNBT, so the
                    // decoded flare keeps its freshly-created default position/motion.
                    // (1.16.5 common/data/sync/client/ClientPatreonFlares.java:92)
                    entitySet.add(entity);
                }

                data.entitiesClient.put(playerUUID, entitySet);
            }
        }

        @Override
        public void readFromIncomingDiff(ClientPatreonFlares data, CompoundTag compound) {
            ListTag entities = compound.getList("updates", Tag.TAG_COMPOUND);
            for (Tag iNBT : entities) {
                CompoundTag tag = (CompoundTag) iNBT;

                UUID playerUUID = tag.getUUID("playerUUID");
                Set<PatreonPartialEntity> entitySet = data.entitiesClient.computeIfAbsent(playerUUID, p -> new HashSet<>());

                ListTag entityList = tag.getList("entityList", Tag.TAG_COMPOUND);
                for (Tag iEntityTag : entityList) {
                    CompoundTag entityNBT = (CompoundTag) iEntityTag;

                    UUID effectUUID = entityNBT.getUUID("id");
                    PatreonEffect effect = PatreonEffectHelper.getPatreonEffects(LogicalSide.CLIENT, playerUUID)
                            .stream()
                            .filter(eff -> eff.getEffectUUID().equals(effectUUID))
                            .findFirst()
                            .orElse(null);
                    if (effect == null) {
                        continue;
                    }
                    PatreonPartialEntity entity = entitySet.stream()
                            .filter(e -> e.getEffectUUID().equals(effectUUID))
                            .findFirst()
                            .orElse(null);
                    if (entity == null) {
                        entity = effect.createEntity(playerUUID);
                        if (entity == null) {
                            continue;
                        }
                        entitySet.add(entity);
                    }
                    //BLOCKED_PORT[AS-SYNC-PF-01]: entity.readFromNBT(entityNBT.getCompound("data")) - see
                    // readFromIncomingFullSync. (1.16.5 ClientPatreonFlares.java:133)
                }
            }

            ListTag removals = compound.getList("removals", Tag.TAG_COMPOUND);
            for (Tag iNBT : removals) {
                CompoundTag tag = (CompoundTag) iNBT;

                UUID playerUUID = tag.getUUID("playerUUID");
                data.entitiesClient.remove(playerUUID);
            }
        }
    }
}
