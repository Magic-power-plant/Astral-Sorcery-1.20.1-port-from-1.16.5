package hellfirepvp.astralsorcery.common.perk;

import net.minecraftforge.fml.LogicalSide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the perk experience curve of {@link PerkLevelManager}.
 *
 * <p>The curve is the one thing in the perk module that can be exercised without FML: it is pure arithmetic over
 * a level cap, and {@code getLevelCap} ignores the {@code Player} argument entirely, so every public method here
 * can be driven with a null player. Everything else in the module needs a real registry or a level.
 *
 * <p>The reference is 1.16.5 {@code common/perk/PerkLevelManager.java:110-117}, whose body is identical to the
 * 1.20.1 one apart from {@code MathHelper} becoming {@code Mth}. Pinning it matters because the port briefly
 * carried a second, invented curve (MAX_TIER 10 / BASE_EXP_COST 100 / +50 per tier, in the since-deleted
 * common/perk/PerkManager) and a reader could not tell from the code alone which one was authoritative.
 */
class PerkLevelCurveTest {

    private static final int TEST_CAP = 30;

    /** The 1.16.5 formula, written out independently of the implementation. */
    private static long referenceTotalExpFor(int level) {
        long total = 0;
        for (int i = 1; i <= level; i++) {
            total += 150L + 100L * (long) Math.floor(Math.pow(1.2F, i));
        }
        return total;
    }

    private static void installLevelData(int levelCap) {
        try {
            Class<?> levelDataClass = Class.forName(
                    "hellfirepvp.astralsorcery.common.perk.PerkLevelManager$LevelData");
            Constructor<?> ctor = levelDataClass.getDeclaredConstructor(int.class);
            ctor.setAccessible(true);
            Object levelData = ctor.newInstance(levelCap);

            Field field = PerkLevelManager.class.getDeclaredField("LEVEL_DATA");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            hellfirepvp.astralsorcery.common.util.object.SidedReference<Object> ref =
                    (hellfirepvp.astralsorcery.common.util.object.SidedReference<Object>) field.get(null);
            ref.setData(LogicalSide.SERVER, levelData);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("could not install LevelData", e);
        }
    }

    @AfterEach
    void clearServerLevelData() {
        //LEVEL_DATA is static, so leaving a cap behind would leak into any later test.
        PerkLevelManager.clearCache(LogicalSide.SERVER);
    }

    @Test
    void totalExperiencePerLevelMatchesTheReferenceFormula() {
        installLevelData(TEST_CAP);
        for (int level = 2; level <= TEST_CAP; level++) {
            assertEquals(referenceTotalExpFor(level),
                    PerkLevelManager.getExpForLevel(level, null, LogicalSide.SERVER),
                    "total experience required for level " + level);
        }
    }

    @Test
    void levelOneCostsNothingAndIsTheFloor() {
        installLevelData(TEST_CAP);
        assertEquals(0L, PerkLevelManager.getExpForLevel(1, null, LogicalSide.SERVER));
        assertEquals(0L, PerkLevelManager.getExpForLevel(0, null, LogicalSide.SERVER));
        assertEquals(1, PerkLevelManager.getLevel(0D, null, LogicalSide.SERVER));
        assertEquals(1, PerkLevelManager.getLevel(-5D, null, LogicalSide.SERVER),
                "negative experience must not underflow below level 1");
    }

    @Test
    void getLevelIsTheInverseOfGetExpForLevel() {
        installLevelData(TEST_CAP);
        for (int level = 2; level < TEST_CAP; level++) {
            long exact = PerkLevelManager.getExpForLevel(level, null, LogicalSide.SERVER);
            assertEquals(level + 1, PerkLevelManager.getLevel((double) exact, null, LogicalSide.SERVER),
                    "landing exactly on the level " + level + " threshold should already be level " + (level + 1));
            assertEquals(level, PerkLevelManager.getLevel((double) (exact - 1), null, LogicalSide.SERVER),
                    "one point short of the level " + level + " threshold should still be level " + level);
        }
    }

    @Test
    void experienceIsClampedToTheLevelCap() {
        installLevelData(TEST_CAP);
        assertEquals(TEST_CAP, PerkLevelManager.getLevelCap(LogicalSide.SERVER, null));
        assertEquals(TEST_CAP, PerkLevelManager.getLevel(Long.MAX_VALUE / 2D, null, LogicalSide.SERVER),
                "no amount of experience may exceed the cap");
        assertEquals(PerkLevelManager.getExpForLevel(TEST_CAP, null, LogicalSide.SERVER),
                PerkLevelManager.getExpForLevel(TEST_CAP + 10, null, LogicalSide.SERVER),
                "asking past the cap returns the cap's requirement");
    }

    @Test
    void progressToNextLevelRunsFromZeroToOne() {
        installLevelData(TEST_CAP);
        long levelFive = PerkLevelManager.getExpForLevel(5, null, LogicalSide.SERVER);
        long levelSix = PerkLevelManager.getExpForLevel(6, null, LogicalSide.SERVER);

        float atStart = PerkLevelManager.getNextLevelPercent((double) levelFive, null, LogicalSide.SERVER);
        float nearEnd = PerkLevelManager.getNextLevelPercent((double) (levelSix - 1), null, LogicalSide.SERVER);

        assertEquals(0F, atStart, 1.0E-4F, "sitting exactly on a threshold means no progress into the next level");
        assertTrue(nearEnd > 0.99F && nearEnd < 1F, "one point short of the next level should be just under full, was " + nearEnd);
        assertEquals(1F, PerkLevelManager.getNextLevelPercent((double) Long.MAX_VALUE / 2D, null, LogicalSide.SERVER),
                "at the cap the bar reads full");
    }

    @Test
    void withoutLoadedLevelDataEverythingFallsBackToLevelOne() {
        //loadPerkLevels() has not run - the state a client is in before the login packet delivers the cap.
        PerkLevelManager.clearCache(LogicalSide.SERVER);
        assertEquals(1, PerkLevelManager.getLevelCap(LogicalSide.SERVER, null));
        assertEquals(1, PerkLevelManager.getLevel(999999D, null, LogicalSide.SERVER));
        assertEquals(0L, PerkLevelManager.getExpForLevel(10, null, LogicalSide.SERVER));
    }
}
