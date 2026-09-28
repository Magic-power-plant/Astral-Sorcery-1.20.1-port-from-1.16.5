package hellfirepvp.astralsorcery.common.data.world;

import hellfirepvp.astralsorcery.common.data.world.cache.WorldCacheDomain;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Save/load coverage for the CachedWorldData subclasses that AstralSorcery actually reaches at runtime.
 * <p>
 * These drive the real {@code IWorldRelatedData#writeData/readData} pair against temporary directories, so
 * they exercise the on-disk layout ObserverLib defines (a "general.dat" plus one
 * "[save key]_[sectionX]_[sectionZ].dat" per dirty section) rather than just the in-memory NBT helpers.
 * No FML, server or level is required: writeData/readData take explicit directories, and the production save
 * keys come from {@link DataAS}, whose static initializer only builds a domain out of plain maps.
 */
class WorldDataPersistenceTest {

    private static final BlockPos ORE_A = new BlockPos(100, 40, 100);
    private static final BlockPos ORE_B = new BlockPos(112, 12, 96);
    // Negative y: a 1.20.1-only coordinate. Rock crystal ore generates down to the world floor, so the
    // section bookkeeping has to survive it (LightNetworkBuffer had a real (y & 255) >> 4 bug for exactly
    // this reason). RockCrystalBuffer keys sections on x/z only, so this must simply round-trip verbatim.
    private static final BlockPos ORE_BELOW_ZERO = new BlockPos(103, -47, 104);

    private static RockCrystalBuffer newRockCrystalBuffer() {
        return DataAS.KEY_ROCK_CRYSTAL_BUFFER.getNewInstance(DataAS.KEY_ROCK_CRYSTAL_BUFFER);
    }

    private static List<BlockPos> collectAround(RockCrystalBuffer buffer, BlockPos pos) {
        return buffer.collectPositions(new ChunkPos(pos), 1);
    }

    @Test
    void addingOreMarksTheBufferDirtySoItEverReachesDisk() {
        RockCrystalBuffer buffer = newRockCrystalBuffer();
        assertFalse(buffer.needsSaving(), "a freshly created buffer has nothing to save");

        buffer.addOre(ORE_A);

        // WorldCacheManager#scheduleSaveAll and #doSave only hand a CachedWorldData to the IO thread when
        // needsSaving() is true. If addOre forgot to markDirty, generated ore positions would be dropped on
        // every restart without any error, so this is the guard for that whole path.
        assertTrue(buffer.needsSaving(), "addOre must mark its section dirty");
    }

    @Test
    void orePositionsSurviveTheSectionFileRoundTrip(@TempDir Path dir) throws Exception {
        File actual = dir.resolve("rock-crystals").toFile();
        File backup = dir.resolve("rock-crystals-Backup").toFile();

        RockCrystalBuffer written = newRockCrystalBuffer();
        written.addOre(ORE_A);
        written.addOre(ORE_B);
        written.addOre(ORE_BELOW_ZERO);
        written.writeData(actual, backup);

        RockCrystalBuffer read = newRockCrystalBuffer();
        read.readData(actual);

        assertTrue(collectAround(read, ORE_A).containsAll(Arrays.asList(ORE_A, ORE_B, ORE_BELOW_ZERO)),
                "all three ore positions must come back out of the section file");
        assertTrue(collectAround(read, ORE_BELOW_ZERO).contains(ORE_BELOW_ZERO),
                "a negative-y ore position must round-trip verbatim");
    }

    @Test
    void sectionFilesUseTheSaveKeyIdentifierAndSectionCoordinates(@TempDir Path dir) throws Exception {
        File actual = dir.resolve("rock-crystals").toFile();
        File backup = dir.resolve("rock-crystals-Backup").toFile();

        RockCrystalBuffer buffer = newRockCrystalBuffer();
        buffer.addOre(ORE_A);
        buffer.writeData(actual, backup);

        // The file name IS the save format: SectionWorldData#readData splits it on '_' and requires exactly
        // three parts, the first of which must equal the save key identifier. 1.16.5 uses section precision
        // 10 for this buffer (1.16.5 common/data/world/RockCrystalBuffer.java:37), so x=100,z=100 both shift
        // to 0. Renaming the key or changing the precision silently orphans every existing section file.
        assertTrue(new File(actual, "rock-crystals_0_0.dat").isFile(),
                () -> "expected rock-crystals_0_0.dat, found " + Arrays.toString(actual.list()));
        assertTrue(new File(actual, "general.dat").isFile(), "general.dat is always written");
    }

    @Test
    void saveKeyIdentifiersMayNotContainUnderscores() {
        // Same reason as above: an identifier with an underscore would make the three-way split ambiguous and
        // every section file would be skipped on load.
        for (WorldCacheDomain.SaveKey<?> key : DataAS.DOMAIN_AS.getKnownSaveKeys()) {
            assertFalse(key.getIdentifier().contains("_"),
                    () -> "save key '" + key.getIdentifier() + "' must not contain '_'");
        }
    }

    @Test
    void allRegisteredSaveKeysCanBeInstantiated() {
        // A save key whose provider throws would only be noticed the first time a world touched that data.
        for (WorldCacheDomain.SaveKey<?> key : DataAS.DOMAIN_AS.getKnownSaveKeys()) {
            assertNotNull(instantiate(key), () -> "save key '" + key.getIdentifier() + "' produced no instance");
        }
        assertEquals(4, DataAS.DOMAIN_AS.getKnownSaveKeys().size(),
                "1.16.5 registers exactly four save keys: gateway-cache, lightnetwork, storagenetwork, rock-crystals");
    }

    private static <T extends hellfirepvp.astralsorcery.common.data.world.cache.CachedWorldData> T instantiate(
            WorldCacheDomain.SaveKey<T> key) {
        return key.getNewInstance(key);
    }

    @Test
    void legacySectionFileWrittenBy1165StillLoads(@TempDir Path dir) throws Exception {
        File actual = dir.resolve("rock-crystals").toFile();
        assertTrue(actual.mkdirs());

        // Exactly what 1.16.5's RockCrystalBuffer.BufferSection#writeToNBT produced: a "posList" of compounds
        // keyed bposX/bposY/bposZ. Written uncompressed with NbtIo#write, which is what SectionWorldData uses.
        CompoundTag legacySection = TagParser.parseTag("{posList:["
                + "{bposX:100,bposY:40,bposZ:100},"
                + "{bposX:112,bposY:12,bposZ:96}"
                + "]}");
        NbtIo.write(legacySection, new File(actual, "rock-crystals_0_0.dat"));

        RockCrystalBuffer read = newRockCrystalBuffer();
        read.readData(actual);

        List<BlockPos> loaded = collectAround(read, ORE_A);
        assertTrue(loaded.contains(ORE_A), "legacy ore position (100,40,100) must load");
        assertTrue(loaded.contains(ORE_B), "legacy ore position (112,12,96) must load");
    }

    @Test
    void removingAnOreTakesItOutOfTheNextSave(@TempDir Path dir) throws Exception {
        File actual = dir.resolve("rock-crystals").toFile();
        File backup = dir.resolve("rock-crystals-Backup").toFile();

        RockCrystalBuffer buffer = newRockCrystalBuffer();
        buffer.addOre(ORE_A);
        buffer.addOre(ORE_B);
        buffer.removeOre(ORE_A);
        buffer.writeData(actual, backup);

        RockCrystalBuffer read = newRockCrystalBuffer();
        read.readData(actual);

        List<BlockPos> loaded = collectAround(read, ORE_B);
        assertFalse(loaded.contains(ORE_A), "a mined ore must not come back after a save/load");
        assertTrue(loaded.contains(ORE_B), "the untouched ore must still be there");
    }

    @Test
    void gatewayNodesSurviveTheNbtRoundTrip() throws Exception {
        // 1.16.5 GatewayCache.GatewayNode#write layout: bposX/Y/Z, "locked", plus optional "display" (json),
        // "color" (an NBTHelper#writeEnum ordinal) and the owner/allowedUsers entries. The optionals are left
        // out on purpose: DyeColor and Component.Serializer both pull in BuiltInRegistries, which throws
        // "Not bootstrapped" outside a running game, and vanilla's Bootstrap#bootStrap cannot be used here
        // either because the Forge-patched build calls NetworkHooks#init from it. A gateway that was never
        // renamed or dyed is also the ordinary case.
        CompoundTag legacy = TagParser.parseTag("{posList:["
                + "{bposX:8,bposY:70,bposZ:-16,locked:0b},"
                + "{bposX:-2048,bposY:5,bposZ:2048,locked:1b}"
                + "]}");

        GatewayCache read = DataAS.KEY_GATEWAY_CACHE.getNewInstance(DataAS.KEY_GATEWAY_CACHE);
        read.readFromNBT(legacy);

        assertEquals(2, read.getGatewayPositions().size());
        GatewayCache.GatewayNode plain = read.getGatewayNode(new BlockPos(8, 70, -16));
        assertNotNull(plain, "gateway at (8,70,-16) must load");
        assertFalse(plain.isLocked());
        assertNull(plain.getDisplayName(), "an absent display stays absent");
        assertNull(plain.getColor(), "an absent color stays absent");
        assertNull(plain.getOwner(), "an absent owner stays absent");
        assertTrue(plain.getAllowedUsers().isEmpty());

        GatewayCache.GatewayNode locked = read.getGatewayNode(new BlockPos(-2048, 5, 2048));
        assertNotNull(locked, "gateway at far negative coordinates must load");
        assertTrue(locked.isLocked(), "the locked flag must survive");

        // Re-write with the production writer and read it back: the two directions must agree.
        CompoundTag rewritten = new CompoundTag();
        read.writeToNBT(rewritten);
        assertEquals(2, rewritten.getList("posList", net.minecraft.nbt.Tag.TAG_COMPOUND).size());

        GatewayCache reread = DataAS.KEY_GATEWAY_CACHE.getNewInstance(DataAS.KEY_GATEWAY_CACHE);
        reread.readFromNBT(rewritten);
        assertTrue(reread.hasGateway(new BlockPos(8, 70, -16)));
        assertTrue(reread.hasGateway(new BlockPos(-2048, 5, 2048)));
        assertTrue(Objects.requireNonNull(reread.getGatewayNode(new BlockPos(-2048, 5, 2048))).isLocked());
        assertFalse(Objects.requireNonNull(reread.getGatewayNode(new BlockPos(8, 70, -16))).isLocked());
    }

    @Test
    void gatewayCacheWritesAnEmptyListWhenThereAreNoGateways() {
        GatewayCache empty = DataAS.KEY_GATEWAY_CACHE.getNewInstance(DataAS.KEY_GATEWAY_CACHE);

        CompoundTag tag = new CompoundTag();
        empty.writeToNBT(tag);

        ListTag list = tag.getList("posList", net.minecraft.nbt.Tag.TAG_COMPOUND);
        assertTrue(list.isEmpty());

        // An empty save file must not resurrect anything either.
        GatewayCache reread = DataAS.KEY_GATEWAY_CACHE.getNewInstance(DataAS.KEY_GATEWAY_CACHE);
        reread.readFromNBT(tag);
        assertTrue(reread.getGatewayPositions().isEmpty());
    }
}
