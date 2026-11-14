package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.registry.RegistryConstellations;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The four Minor constellations are ritual <em>traits</em>: TileRitualPedestal,
 * StarlightReceiverRitualPedestal and CEffectArmara all call
 * {@code getRitualTrait().affectConstellationEffect(properties)} on the properties of the ritual the trait is
 * slotted into. {@link IMinorConstellation#affectConstellationEffect} defaults to a no-op, so building the four
 * as plain {@code Constellation.Minor} left every Minor trait with no effect on any ritual while still looking
 * correct - it compiled, registered and showed up in the GUI.
 *
 * <p>These are the multipliers of 1.16.5 common/registry/RegistryConstellations.java:380-471. The maths is pure,
 * so it runs without FML; only ConstellationsAS#init (which just builds objects and fills a HashMap) is needed.
 */
class MinorConstellationTraitTest {

    @BeforeAll
    static void buildConstellations() {
        //Idempotent: guarded by ConstellationsAS#initialized, and the argument is ignored.
        RegistryConstellations.init();
    }

    private static ConstellationEffectProperties propertiesOfSize(double size) {
        return new ConstellationEffectProperties(size);
    }

    @Test
    void geluBoostsPotencyAndDoublesSize() {
        ConstellationEffectProperties props = propertiesOfSize(4.0D);
        float basePotency = props.getPotency();
        ConstellationsAS.gelu.affectConstellationEffect(props);

        assertEquals(8.0D, props.getSize(), 1.0E-6D, "gelu multiplies size by 2");
        assertEquals(basePotency * 1.4F, props.getPotency(), 1.0E-5F, "gelu multiplies potency by 1.4");
        assertFalse(props.isCorrupted(), "gelu does not corrupt");
    }

    @Test
    void ulteriaTradesPotencyAndSizeForAmplifier() {
        ConstellationEffectProperties props = propertiesOfSize(4.0D);
        float basePotency = props.getPotency();
        float baseAmplifier = props.getEffectAmplifier();
        ConstellationsAS.ulteria.affectConstellationEffect(props);

        assertEquals(2.0D, props.getSize(), 1.0E-6D, "ulteria halves size");
        assertEquals(basePotency * 0.5F, props.getPotency(), 1.0E-5F, "ulteria halves potency");
        assertEquals(baseAmplifier * 2F, props.getEffectAmplifier(), 1.0E-5F, "ulteria doubles the amplifier");
    }

    @Test
    void alcaraCorruptsAndSlightlyEnlarges() {
        ConstellationEffectProperties props = propertiesOfSize(5.0D);
        ConstellationsAS.alcara.affectConstellationEffect(props);

        assertTrue(props.isCorrupted(), "alcara is the corrupting trait - this flag drives the corrupted ritual path");
        assertEquals(6.0D, props.getSize(), 1.0E-6D, "alcara multiplies size by 1.2");
    }

    @Test
    void voruxTradesAmplifierForHeavyPotency() {
        ConstellationEffectProperties props = propertiesOfSize(5.0D);
        float basePotency = props.getPotency();
        float baseAmplifier = props.getEffectAmplifier();
        ConstellationsAS.vorux.affectConstellationEffect(props);

        assertEquals(6.0D, props.getSize(), 1.0E-6D, "vorux multiplies size by 1.2");
        assertEquals(basePotency * 5F, props.getPotency(), 1.0E-4F, "vorux multiplies potency by 5");
        assertEquals(baseAmplifier * 0.8F, props.getEffectAmplifier(), 1.0E-5F, "vorux reduces the amplifier to 0.8");
    }

    @Test
    void everyMinorConstellationActuallyOverridesTheTraitHook() {
        //The regression this whole file exists for: a plain Constellation.Minor inherits the no-op default, so
        //the properties come back untouched. Asserting "something changed" catches a future re-plaining of any
        //of the four, independently of the exact multipliers above.
        IMinorConstellation[] minors = {
                ConstellationsAS.gelu, ConstellationsAS.ulteria, ConstellationsAS.alcara, ConstellationsAS.vorux
        };
        for (IMinorConstellation minor : minors) {
            ConstellationEffectProperties props = propertiesOfSize(4.0D);
            double size = props.getSize();
            float potency = props.getPotency();
            float amplifier = props.getEffectAmplifier();
            boolean corrupted = props.isCorrupted();

            minor.affectConstellationEffect(props);

            boolean changed = props.getSize() != size
                    || props.getPotency() != potency
                    || props.getEffectAmplifier() != amplifier
                    || props.isCorrupted() != corrupted;
            assertTrue(changed, minor.getSimpleName() + " must affect the ritual properties, not fall through to "
                    + "IMinorConstellation's no-op default");
        }
    }
}
