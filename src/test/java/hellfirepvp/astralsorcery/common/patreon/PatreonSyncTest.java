package hellfirepvp.astralsorcery.common.patreon;

import hellfirepvp.astralsorcery.common.data.sync.client.ClientPatreonFlares;
import hellfirepvp.astralsorcery.common.data.sync.server.DataPatreonFlares;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonFlare;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonPartialEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PatreonSyncTest {
    private final UUID owner = UUID.randomUUID();
    private final UUID first = UUID.randomUUID();
    private final UUID second = UUID.randomUUID();
    private final ClientPatreonFlares.Reader reader = new ClientPatreonFlares.Reader();

    private CompoundTag state(double x) {
        CompoundTag tag = new CompoundTag();
        tag.put("pos", NBTHelper.writeVector3(new Vector3(x, 2, 3)));
        tag.put("prevPos", NBTHelper.writeVector3(new Vector3(x - 1, 2, 3)));
        return tag;
    }
    private CompoundTag payload(String key, List<UUID> ids, double x) {
        CompoundTag ownerTag = new CompoundTag();
        ownerTag.putUUID("playerUUID", owner);
        ListTag entities = new ListTag();
        for (UUID id : ids) {
            CompoundTag entity = new CompoundTag();
            entity.putUUID("id", id);
            entity.put("data", state(x));
            entities.add(entity);
        }
        ownerTag.put("entityList", entities);
        ListTag owners = new ListTag();
        owners.add(ownerTag);
        CompoundTag result = new CompoundTag();
        result.put(key, owners);
        return result;
    }

    @Test void fullSyncWorksBeforeCatalogDownloadAndReplacesOldVisualState() {
        ClientPatreonFlares client = new ClientPatreonFlares();
        reader.readFromIncomingFullSync(client, payload("entities", List.of(first), 8));
        PatreonPartialEntity old = client.getEntities(owner).iterator().next();
        assertEquals(new Vector3(8, 2, 3), old.getPos());
        assertEquals(owner, old.getOwnerUUID());
        reader.readFromIncomingFullSync(client, payload("entities", List.of(second), 12));
        assertTrue(old.isRemoved());
        assertEquals(second, client.getEntities(owner).iterator().next().getEffectUUID());
        reader.readFromIncomingFullSync(client, new CompoundTag());
        assertTrue(client.getOwners().isEmpty());
    }

    @Test void ownerDiffRetainsIdentityUpdatesPositionAndRetiresMissingEntities() {
        ClientPatreonFlares client = new ClientPatreonFlares();
        reader.readFromIncomingFullSync(client, payload("entities", List.of(first, second), 1));
        PatreonPartialEntity kept = client.getEntities(owner).stream().filter(e -> first.equals(e.getEffectUUID())).findFirst().orElseThrow();
        PatreonPartialEntity removed = client.getEntities(owner).stream().filter(e -> second.equals(e.getEffectUUID())).findFirst().orElseThrow();
        reader.readFromIncomingDiff(client, payload("updates", List.of(first), 4));
        assertSame(kept, client.getEntities(owner).iterator().next());
        assertEquals(new Vector3(4, 2, 3), kept.getPos());
        assertTrue(removed.isRemoved());
        CompoundTag removal = payload("removals", List.of(), 0);
        reader.readFromIncomingDiff(client, removal);
        assertTrue(kept.isRemoved());
        assertTrue(client.getOwners().isEmpty());
    }

    @Test void fullSnapshotDoesNotConsumePendingDiffAndRemovalNeverLeavesServerGhosts() {
        var server = new DataPatreonFlares.Provider(new ResourceLocation("test", "patreon")).provideServerData();
        var entity = new PatreonFlare(first, owner);
        entity.readFromNBT(state(21));
        server.addEntity(entity);
        CompoundTag full = new CompoundTag();
        server.writeAllDataToPacket(full);
        CompoundTag secondFull = new CompoundTag();
        server.writeAllDataToPacket(secondFull);
        assertEquals(full, secondFull);
        CompoundTag diff = new CompoundTag();
        server.writeDiffDataToPacket(diff);
        assertEquals(full.getList("entities", Tag.TAG_COMPOUND), diff.getList("updates", Tag.TAG_COMPOUND));
        ClientPatreonFlares client = new ClientPatreonFlares();
        reader.readFromIncomingFullSync(client, full);
        assertEquals(entity.getPos(), client.getEntities(owner).iterator().next().getPos());
        server.removeEntities(owner);
        assertTrue(entity.isRemoved());
        assertTrue(server.getOwners().isEmpty());
        server.updateEntitiesOf(owner); // Late dirty signal must not resurrect a removed owner.
        CompoundTag removal = new CompoundTag();
        server.writeDiffDataToPacket(removal);
        assertTrue(removal.getList("updates", Tag.TAG_COMPOUND).isEmpty());
        assertEquals(owner, removal.getList("removals", Tag.TAG_COMPOUND).getCompound(0).getUUID("playerUUID"));
        reader.readFromIncomingDiff(client, removal);
        assertTrue(client.getOwners().isEmpty());
        CompoundTag drained = new CompoundTag();
        server.writeDiffDataToPacket(drained);
        assertTrue(drained.getList("removals", Tag.TAG_COMPOUND).isEmpty());
    }

    @Test void reconciliationAndShutdownRetireCompanionState() {
        var server = new DataPatreonFlares.Provider(new ResourceLocation("test", "patreon")).provideServerData();
        var a = new PatreonFlare(first, owner);
        var b = new PatreonFlare(second, owner);
        server.addEntity(a);
        server.addEntity(b);
        server.retainEntities(owner, Set.of(first));
        assertTrue(b.isRemoved());
        assertFalse(a.isRemoved());
        assertEquals(List.of(a), List.copyOf(server.getEntities(owner)));
        server.clearServer();
        assertTrue(a.isRemoved());
        assertTrue(server.getOwners().isEmpty());
    }
}
