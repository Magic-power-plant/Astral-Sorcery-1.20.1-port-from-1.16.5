package hellfirepvp.astralsorcery.common.network.login;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.network.base.ASLoginPacket;
import hellfirepvp.astralsorcery.common.network.login.client.PktLoginAcknowledge;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncDataHolder;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncGateway;
import hellfirepvp.astralsorcery.common.network.login.server.PktLoginSyncPerkInformation;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The login packets carry everything a joining client needs before the play phase: the SyncDataHolder snapshot,
 * the celestial gateway cache and the perk tree + level cap. They were ported as play packets with static
 * encode/decode/handle, which left {@code makeLogin()} with no caller, and {@link PktLoginSyncGateway} had been
 * reduced to a stub that wrote a literal 0 instead of the gateway nodes.
 * <p>
 * These tests exercise the production encoder()/decoder() pairs directly. All four codecs are pure buffer
 * operations (no registry lookups), so they run without FML.
 */
class LoginPacketCodecTest {

    private static FriendlyByteBuf buffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    /**
     * Forge's {@code SimpleChannel.MessageBuilder#add()} rejects a {@code loginIndex(...)} registration whose
     * message type is not an {@link IntSupplier}, and the handshake matches replies to requests by that index.
     * Every login packet therefore has to inherit the index plumbing from {@link ASLoginPacket}.
     */
    @Test
    void everyLoginPacketSuppliesItsLoginIndex() {
        for (ASLoginPacket<?> pkt : List.of(
                new PktLoginSyncDataHolder(),
                new PktLoginSyncGateway(),
                new PktLoginSyncPerkInformation(),
                new PktLoginAcknowledge())) {
            assertInstanceOf(IntSupplier.class, pkt, pkt.getClass().getSimpleName() + " must be an IntSupplier");
            pkt.setLoginIndex(7);
            assertEquals(7, pkt.getLoginIndex());
            assertEquals(7, ((IntSupplier) pkt).getAsInt(),
                    pkt.getClass().getSimpleName() + " must expose the login index through getAsInt");
        }
    }

    @Test
    void dataHolderSnapshotSurvivesTheBufferRoundTrip() {
        CompoundTag connections = new CompoundTag();
        connections.putInt("count", 3);
        CompoundTag endpoints = new CompoundTag();
        endpoints.putString("dim", "minecraft:overworld");

        PktLoginSyncDataHolder sent = new PktLoginSyncDataHolder();
        Map<ResourceLocation, CompoundTag> syncData = syncDataOf(sent);
        syncData.put(new ResourceLocation("astralsorcery", "connections"), connections);
        syncData.put(new ResourceLocation("astralsorcery", "endpoints"), endpoints);

        FriendlyByteBuf buf = buffer();
        sent.encoder().accept(sent, buf);
        PktLoginSyncDataHolder received = sent.decoder().apply(buf);

        assertEquals(0, buf.readableBytes(), "decoder must consume exactly what the encoder wrote");
        Map<ResourceLocation, CompoundTag> got = syncDataOf(received);
        assertEquals(2, got.size());
        assertEquals(3, got.get(new ResourceLocation("astralsorcery", "connections")).getInt("count"));
        assertEquals("minecraft:overworld",
                got.get(new ResourceLocation("astralsorcery", "endpoints")).getString("dim"));
    }

    @Test
    void perkInformationCarriesTreeDataAndLevelCap() {
        JsonObject node = new JsonObject();
        node.addProperty("name", "astralsorcery:root");
        node.addProperty("x", 12);

        PktLoginSyncPerkInformation sent = new PktLoginSyncPerkInformation();
        sent.getRawPerkTreeData().add(node);
        setInt(sent, "maxLevel", 30);

        FriendlyByteBuf buf = buffer();
        sent.encoder().accept(sent, buf);
        PktLoginSyncPerkInformation received = sent.decoder().apply(buf);

        assertEquals(0, buf.readableBytes());
        assertEquals(30, received.getMaxLevel(), "the client's level cap only ever comes from this packet");
        assertEquals(1, received.getRawPerkTreeData().size());
        assertEquals("astralsorcery:root", received.getRawPerkTreeData().get(0).get("name").getAsString());
        assertEquals(12, received.getRawPerkTreeData().get(0).get("x").getAsInt());
    }

    /**
     * The regression this pins down: the stub encoder wrote {@code buf.writeInt(0)} per dimension instead of the
     * node collection, so every gateway was dropped on login.
     * <p>
     * Only the encoder is driven here. {@code ByteBufUtils#readVanillaRegistryEntry} calls
     * {@code ResourceKey.createRegistryKey}, which reads {@code BuiltInRegistries.ROOT_REGISTRY_NAME} and therefore
     * needs vanilla's {@code Bootstrap} — and on a Forge-patched jar {@code Bootstrap.bootStrap()} runs
     * {@code NetworkHooks.init()}, which builds a {@code SimpleChannel} and requires a live FML event bus. So the
     * decode half of a dimension key cannot run outside a game launch. The dimension key is written and read back
     * as two plain strings, which this test verifies byte for byte, and the node collection is decoded with the
     * production {@code GatewayNode} codec.
     */
    @Test
    void gatewayEncoderEmitsTheNodeCollectionPerDimension() {
        ResourceKey<Level> overworld = dimensionKeyWithoutBootstrap("minecraft:overworld");
        GatewayCache.GatewayNode node = unlockedNodeAt(new BlockPos(120, 64, -300));

        PktLoginSyncGateway sent = new PktLoginSyncGateway();
        sent.getPositions().put(overworld, List.of(node));

        FriendlyByteBuf buf = buffer();
        sent.encoder().accept(sent, buf);

        assertEquals(1, buf.readInt(), "one dimension was written");
        assertEquals("minecraft:dimension", ByteBufUtils.readResourceLocation(buf).toString());
        assertEquals("minecraft:overworld", ByteBufUtils.readResourceLocation(buf).toString());
        assertEquals(1, buf.readInt(),
                "the stub wrote a literal 0 here; the node collection size must be written instead");
        assertEquals(new BlockPos(120, 64, -300), GatewayCache.GatewayNode.read(buf).getPos());
        assertEquals(0, buf.readableBytes(), "the whole node payload must be accounted for");
    }

    /**
     * The gateway node collection is the payload that the stub dropped; its codec round-trips independently of the
     * dimension key, so it can be checked end to end.
     */
    @Test
    void gatewayNodeCollectionRoundTripsThroughItsOwnCodec() {
        List<GatewayCache.GatewayNode> nodes = List.of(
                unlockedNodeAt(new BlockPos(1, 2, 3)),
                unlockedNodeAt(new BlockPos(-400, 200, 512)));

        FriendlyByteBuf buf = buffer();
        ByteBufUtils.writeCollection(buf, nodes, (b, n) -> n.write(b));
        List<GatewayCache.GatewayNode> read = ByteBufUtils.readList(buf, GatewayCache.GatewayNode::read);

        assertEquals(0, buf.readableBytes());
        assertEquals(2, read.size());
        assertEquals(new BlockPos(1, 2, 3), read.get(0).getPos());
        assertEquals(new BlockPos(-400, 200, 512), read.get(1).getPos());
        assertFalse(read.get(0).isLocked());
        assertNull(read.get(0).getColor());
    }

    /**
     * {@code ResourceKey.createRegistryKey} is off limits here (see above), but the private
     * {@code ResourceKey.create(ResourceLocation registryName, ResourceLocation location)} it delegates to only
     * touches {@code ResourceKey}'s own intern map.
     */
    @SuppressWarnings("unchecked")
    private static ResourceKey<Level> dimensionKeyWithoutBootstrap(String dimension) {
        try {
            java.lang.reflect.Method create = ResourceKey.class.getDeclaredMethod(
                    "create", ResourceLocation.class, ResourceLocation.class);
            create.setAccessible(true);
            return (ResourceKey<Level>) create.invoke(null,
                    new ResourceLocation("minecraft", "dimension"), ResourceLocation.parse(dimension));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("ResourceKey.create(ResourceLocation, ResourceLocation) is gone", e);
        }
    }

    @Test
    void acknowledgeIsAnEmptyPayloadAndStillDecodes() {
        PktLoginAcknowledge sent = new PktLoginAcknowledge();
        FriendlyByteBuf buf = buffer();
        sent.encoder().accept(sent, buf);

        assertEquals(0, buf.readableBytes(), "the acknowledge reply carries no payload of its own");
        assertNotNull(sent.decoder().apply(buf));
    }

    /**
     * {@code GatewayNode}'s constructors are private, so the only way to obtain a real one outside the mod is its
     * own buffer codec. Mirrors {@code GatewayNode#write}: pos, absent display, absent color, unlocked, no owner,
     * empty allowed-user map.
     */
    private static GatewayCache.GatewayNode unlockedNodeAt(BlockPos pos) {
        FriendlyByteBuf buf = buffer();
        ByteBufUtils.writePos(buf, pos);
        buf.writeBoolean(false);
        buf.writeBoolean(false);
        buf.writeBoolean(false);
        buf.writeBoolean(false);
        buf.writeInt(0);
        return GatewayCache.GatewayNode.read(buf);
    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, CompoundTag> syncDataOf(PktLoginSyncDataHolder pkt) {
        return (Map<ResourceLocation, CompoundTag>) read(pkt, "syncData");
    }

    private static Object read(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("expected field " + name + " on " + target.getClass().getName(), e);
        }
    }

    private static void setInt(Object target, String name, int value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.setInt(target, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("expected field " + name + " on " + target.getClass().getName(), e);
        }
    }
}
