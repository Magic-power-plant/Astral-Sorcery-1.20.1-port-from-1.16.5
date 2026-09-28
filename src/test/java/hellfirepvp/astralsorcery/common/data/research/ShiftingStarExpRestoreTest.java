package hellfirepvp.astralsorcery.common.data.research;

import net.minecraft.util.Mth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the perk-experience restore that makes the Shifting Star non-destructive.
 * <p>
 * {@code ResearchManager#setAttunedConstellation} deliberately zeroes perk experience and then persists that
 * with {@code ResearchHelper#savePlayerKnowledge} in the same call, so re-attuning is a data-loss operation
 * unless the caller puts the experience back. {@code ItemShiftingStar#finishUsingItem} is the one caller that
 * has to: it reads the experience, re-attunes, and calls {@code ResearchManager#setExp} with
 * {@code Mth.lfloor} of what it read. That method had been dropped in the port, so the read value was computed
 * and discarded and every use of a Shifting Star wiped the player's perk experience on disk.
 * <p>
 * The sequence is reproduced here against {@link PlayerPerkData} directly. This test lives in the production
 * package on purpose: {@code setExp} is protected, which is also why {@code ResearchManager} can call it.
 * {@code ItemShiftingStar} itself is not reachable from a unit test, because constructing an {@code Item}
 * needs {@code BuiltInRegistries}. The NBT round trip of {@code perkExp} is covered separately by
 * {@link PlayerProgressPersistenceTest}.
 */
class ShiftingStarExpRestoreTest {

    /** What {@code ItemShiftingStar} reads, then floors, then hands to {@code ResearchManager#setExp}. */
    private static final double ACCUMULATED_EXP = 12345.9D;

    @Test
    void reAttuningWithoutTheRestoreLosesAllPerkExperience() {
        PlayerPerkData data = new PlayerPerkData();
        data.setExp(ACCUMULATED_EXP);

        // setAttunedConstellation's own zeroing step, in isolation.
        data.setExp(0);

        assertEquals(0.0D, data.getPerkExp(), 0.0D,
                "this is the defect being guarded against: re-attuning zeroes perk exp, so a caller that does "
                        + "not restore it destroys the player's progress");
    }

    @Test
    void theShiftingStarRestoreSequencePutsTheExperienceBack() {
        PlayerPerkData data = new PlayerPerkData();
        data.setExp(ACCUMULATED_EXP);

        // ItemShiftingStar#finishUsingItem, step for step.
        double readBack = data.getPerkExp();          // prog.getPerkData().getPerkExp()
        data.setExp(0);                               // inside setAttunedConstellation
        data.setExp(Mth.lfloor(readBack));            // ResearchManager.setExp(player, Mth.lfloor(perkExp))

        assertEquals(12345.0D, data.getPerkExp(), 0.0D,
                "after the restore the player must keep their accumulated perk experience");
        assertNotEquals(0.0D, data.getPerkExp(), "the restore must actually run");
    }

    @Test
    void fractionalExperienceIsFlooredNotRounded() {
        // 1.16.5 uses MathHelper.lfloor here, so a player loses the fractional part of one exp point and never
        // gains any. Asserted so that swapping in Math.round - which would hand out free experience on every
        // re-attune - shows up as a failure rather than as a silent balance change.
        assertEquals(12345L, Mth.lfloor(12345.9D));
        assertEquals(0L, Mth.lfloor(0.9D));
        assertEquals(12345L, Mth.lfloor(12345.0D));

        PlayerPerkData data = new PlayerPerkData();
        data.setExp(Mth.lfloor(12345.9D));
        assertTrue(data.getPerkExp() <= 12345.9D,
                "the restore must never hand the player more experience than they had");
    }

    @Test
    void setExpClampsNegativeValuesToZero() {
        // PlayerPerkData#setExp is Math.max(exp, 0); a negative restore must not produce negative experience,
        // which PerkLevelManager#getLevel would otherwise have to cope with.
        PlayerPerkData data = new PlayerPerkData();
        data.setExp(500.0D);
        data.setExp(-1.0D);

        assertEquals(0.0D, data.getPerkExp(), 0.0D);
    }
}
