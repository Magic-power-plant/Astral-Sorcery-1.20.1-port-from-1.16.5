package hellfirepvp.astralsorcery.common.data.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Save/load coverage for per-player progression, i.e. the ".astral" files {@link ResearchIOThread} writes.
 * <p>
 * Everything here goes through the production {@link PlayerProgress#store}/{@link PlayerProgress#load} pair
 * and, where the on-disk format matters, through the same {@code NbtIo#write}/{@code NbtIo#read} calls
 * {@code ResearchIOThread#saveNow} and {@code ResearchHelper#loadPlayerKnowledge} use.
 * <p>
 * Perk allocations are deliberately not asserted: {@code PlayerPerkData#load} asks
 * {@code PerkTree.PERK_TREE.getVersion(SERVER)}, which is empty without a running server, so it takes the
 * "perk tree version changed" branch and clears allocations by design. The perk <em>experience</em> is read
 * before that branch, so it is covered below.
 */
class PlayerProgressPersistenceTest {

    private static final ResourceLocation DISCOVERY = new ResourceLocation("astralsorcery", "discidia");
    private static final ResourceLocation ONLY_SEEN = new ResourceLocation("astralsorcery", "aevitas");

    private static PlayerProgress loadFrom(String snbt) throws Exception {
        PlayerProgress progress = new PlayerProgress();
        progress.load(TagParser.parseTag(snbt));
        return progress;
    }

    @Test
    void legacyProgressFileWithoutBookReceivedCountsAsTomeReceived() throws Exception {
        // 1.16.5 PlayerProgress.java:118-119 treats a missing "bookReceived" as true and calls it out as
        // "Legacy support for player progress files that do not have the tag yet". Getting this backwards
        // would hand every pre-existing player a second journal on their next login, because
        // EventHandlerCache#onPlayerConnect gates the giveJournalOnJoin grant on didReceiveTome().
        PlayerProgress progress = loadFrom("{tierReached:0}");

        assertTrue(progress.didReceiveTome(),
                "a progress file predating the bookReceived tag must count as already handed out");
    }

    @Test
    void explicitBookReceivedFalseIsHonouredSoNewPlayersStillGetTheJournal() throws Exception {
        PlayerProgress progress = loadFrom("{tierReached:0,bookReceived:0b}");

        assertFalse(progress.didReceiveTome(), "an explicit false must not be overridden by the legacy rule");
    }

    @Test
    void tomeReceivedSurvivesAStoreLoadCycle() {
        PlayerProgress progress = new PlayerProgress();
        assertFalse(progress.didReceiveTome(), "a brand new progress has not received the tome");
        progress.setTomeReceived();

        CompoundTag stored = new CompoundTag();
        progress.store(stored);
        PlayerProgress reloaded = new PlayerProgress();
        reloaded.load(stored);

        assertTrue(reloaded.didReceiveTome());
        assertTrue(stored.contains("bookReceived"), "store must emit the tag so the legacy rule stops applying");
    }

    @Test
    void knowledgeSurvivesTheResearchIoThreadFileRoundTrip(@TempDir Path dir) throws Exception {
        PlayerProgress progress = new PlayerProgress();
        progress.discoverConstellation(DISCOVERY);
        progress.memorizeConstellation(ONLY_SEEN);
        progress.setTierReached(ProgressionTier.CONSTELLATION_CRAFT);
        progress.forceGainResearch(ResearchProgression.BASIC_CRAFT);
        progress.setAttunedBefore(true);

        // The exact IO ResearchIOThread#saveNow performs: NbtIo#write of the stored compound. perkExp is set
        // on the stored tag rather than through PlayerPerkData (whose mutators are protected), which also
        // means the assertion below covers the real read path in PlayerPerkData#load.
        File file = dir.resolve("progress.astral").toFile();
        CompoundTag stored = new CompoundTag();
        progress.store(stored);
        assertTrue(stored.contains("perkExp"), "store must emit perkExp so the round trip is meaningful");
        stored.putDouble("perkExp", 1234.5D);
        NbtIo.write(stored, file);

        PlayerProgress reloaded = new PlayerProgress();
        reloaded.load(NbtIo.read(file));

        assertTrue(reloaded.hasConstellationDiscovered(DISCOVERY), "a discovered constellation must persist");
        assertTrue(reloaded.hasSeenConstellation(ONLY_SEEN), "a merely seen constellation must persist");
        assertFalse(reloaded.hasConstellationDiscovered(ONLY_SEEN), "seen must not be promoted to discovered");
        assertEquals(ProgressionTier.CONSTELLATION_CRAFT, reloaded.getTierReached());
        assertTrue(reloaded.hasResearch(ResearchProgression.BASIC_CRAFT));
        assertTrue(reloaded.wasOnceAttuned());
        assertEquals(1234.5D, reloaded.getPerkData().getPerkExp(), 1.0E-9D,
                "perk experience is read before the perk-tree-version branch and must survive");
    }

    @Test
    void discoveringAConstellationAlsoMemorizesItAcrossAReload() {
        PlayerProgress progress = new PlayerProgress();
        progress.discoverConstellation(DISCOVERY);

        CompoundTag stored = new CompoundTag();
        progress.store(stored);
        PlayerProgress reloaded = new PlayerProgress();
        reloaded.load(stored);

        // PlayerProgress#load re-adds every known constellation to the seen list, so a file that only listed
        // it under "constellations" still comes back fully consistent.
        assertTrue(reloaded.hasSeenConstellation(DISCOVERY));
        assertTrue(reloaded.getSeenConstellations().contains(DISCOVERY));
    }

    @Test
    void legacyFileListingOnlyKnownConstellationsBackfillsTheSeenList() throws Exception {
        // Older files could carry "constellations" without a matching "seenConstellations" entry.
        PlayerProgress progress = loadFrom("{constellations:[\"astralsorcery:discidia\"],tierReached:1}");

        assertTrue(progress.hasConstellationDiscovered(DISCOVERY));
        assertTrue(progress.hasSeenConstellation(DISCOVERY),
                "a known constellation must be backfilled into the seen list on load");
    }

    @Test
    void researchProgressionRoundTripsAsOrdinalsAndIgnoresUnknownOnes() throws Exception {
        // "research" is an int array of ordinals. A file from a version with more entries than this one must
        // not blow up the whole progression load - it just drops the unknown ordinals.
        PlayerProgress progress = loadFrom("{tierReached:0,research:[I;0,9999,-4]}");

        assertTrue(progress.hasResearch(ResearchProgression.values()[0]));
        assertEquals(1, progress.getResearchProgression().size(),
                "out-of-range research ordinals must be skipped, not throw");
    }

    @Test
    void loadResetsEveryFieldSoAReusedInstanceCannotLeakOldProgress() throws Exception {
        PlayerProgress progress = new PlayerProgress();
        progress.discoverConstellation(DISCOVERY);
        progress.setTierReached(ProgressionTier.TRAIT_CRAFT);
        progress.setTomeReceived();
        progress.setAttunedBefore(true);
        progress.forceGainResearch(ResearchProgression.BASIC_CRAFT);

        // ResearchHelper#loadPlayerKnowledge reuses the map entry for a UUID, so load() must fully reset.
        progress.load(TagParser.parseTag("{tierReached:0,bookReceived:0b}"));

        assertTrue(progress.getKnownConstellations().isEmpty(), "stale constellations must be cleared");
        assertTrue(progress.getSeenConstellations().isEmpty());
        assertTrue(progress.getResearchProgression().isEmpty(), "stale research must be cleared");
        assertEquals(ProgressionTier.DISCOVERY, progress.getTierReached());
        assertFalse(progress.wasOnceAttuned());
        assertFalse(progress.didReceiveTome());
    }

    @Test
    void mergingProgressIntoItselfIsANoOpAndDoesNotThrow() {
        // CommonProxy#onPlayerClone merges getProgress(newPlayer) with getProgress(original). Both resolve
        // through ResearchHelper's static UUID map and a respawned player keeps their UUID, so those are the
        // same object: acceptMergeFrom runs against itself on every death. It has to stay a no-op - the two
        // constellation lists are ArrayLists being iterated while the merge re-adds into them, so anything
        // that dropped the duplicate guards would turn every death into a ConcurrentModificationException.
        PlayerProgress progress = new PlayerProgress();
        progress.discoverConstellation(DISCOVERY);
        progress.memorizeConstellation(ONLY_SEEN);
        progress.forceGainResearch(ResearchProgression.BASIC_CRAFT);
        progress.setTierReached(ProgressionTier.CONSTELLATION_CRAFT);

        assertDoesNotThrow(() -> progress.acceptMergeFrom(progress));

        assertEquals(1, progress.getKnownConstellations().size(), "self-merge must not duplicate entries");
        assertEquals(2, progress.getSeenConstellations().size());
        assertEquals(1, progress.getResearchProgression().size());
        assertEquals(ProgressionTier.CONSTELLATION_CRAFT, progress.getTierReached());
    }

    @Test
    void mergingFromAnotherProgressUnionsKnowledgeAndKeepsTheHigherTier() {
        PlayerProgress target = new PlayerProgress();
        target.discoverConstellation(DISCOVERY);
        target.setTierReached(ProgressionTier.DISCOVERY);

        PlayerProgress source = new PlayerProgress();
        source.discoverConstellation(ONLY_SEEN);
        source.forceGainResearch(ResearchProgression.BASIC_CRAFT);
        source.setTierReached(ProgressionTier.CONSTELLATION_CRAFT);
        source.setAttunedBefore(true);

        target.acceptMergeFrom(source);

        assertTrue(target.hasConstellationDiscovered(DISCOVERY), "own knowledge is kept");
        assertTrue(target.hasConstellationDiscovered(ONLY_SEEN), "merged knowledge is added");
        assertTrue(target.hasResearch(ResearchProgression.BASIC_CRAFT));
        assertTrue(target.wasOnceAttuned());
        assertEquals(ProgressionTier.CONSTELLATION_CRAFT, target.getTierReached(),
                "the later of the two tiers wins");
    }

    @Test
    void storeKnowledgeOmitsPerkDataButKeepsTheKnowledgeHalf() {
        PlayerProgress progress = new PlayerProgress();
        progress.discoverConstellation(DISCOVERY);
        progress.setTierReached(ProgressionTier.ATTUNEMENT);

        CompoundTag knowledgeOnly = new CompoundTag();
        progress.storeKnowledge(knowledgeOnly);

        assertTrue(knowledgeOnly.contains("constellations"));
        assertTrue(knowledgeOnly.contains("research"));
        assertFalse(knowledgeOnly.contains("perkExp"),
                "storeKnowledge is the knowledge-sync half and must not carry perk data");

        PlayerProgress receiver = new PlayerProgress();
        receiver.loadKnowledge(knowledgeOnly);
        assertTrue(receiver.hasSeenConstellation(DISCOVERY));
    }
}
