package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Properties.*;
import static org.junit.jupiter.api.Assertions.*;

class CrystalGeneratorTest {

    // ItemCrystalBase#getGeneratedPropertyTiers (rock crystal) and ItemCelestialCrystal#getGeneratedPropertyTiers,
    // both unchanged from 1.16.5.
    private static final int ROCK_TIERS = 5;
    private static final int CELESTIAL_TIERS = 8;
    private static final int SAMPLES = 2000;

    @BeforeAll
    static void setup() {
        CrystalTestSupport.init();
    }

    @Test
    void rockCrystalGenerationHitsExactlyItsTierBudget() {
        assertGenerationBounds(ROCK_TIERS);
    }

    @Test
    void celestialCrystalGenerationHitsExactlyItsTierBudget() {
        assertGenerationBounds(CELESTIAL_TIERS);
    }

    @Test
    void generationReachesThePhysicalPropertiesIncludingShape() {
        Set<CrystalProperty> seen = new HashSet<>();
        Random random = new Random(42L);
        for (int i = 0; i < SAMPLES; i++) {
            seen.addAll(CrystalGenerator.generateNewAttributes(ROCK_TIERS, random).getProperties());
        }
        assertTrue(seen.contains(PROPERTY_SIZE));
        assertTrue(seen.contains(PROPERTY_SHAPE));
        assertTrue(seen.contains(PROPERTY_PURITY));
        assertTrue(seen.contains(PROPERTY_TOOL_DURABILITY));
        assertTrue(seen.contains(PROPERTY_TOOL_EFFICIENCY));
    }

    @Test
    void generatedAttributesSurviveCopyThroughNbt() {
        Random random = new Random(7L);
        for (int i = 0; i < 200; i++) {
            CrystalAttributes generated = CrystalGenerator.generateNewAttributes(ROCK_TIERS, random);
            assertEquals(generated, generated.copy());
        }
    }

    private static void assertGenerationBounds(int budget) {
        Random random = new Random(20260927L);
        for (int i = 0; i < SAMPLES; i++) {
            CrystalAttributes generated = CrystalGenerator.generateNewAttributes(budget, random);
            assertEquals(budget, generated.getTotalTierLevel());
            for (CrystalAttributes.Attribute attribute : generated.getCrystalAttributes()) {
                CrystalProperty property = attribute.getProperty();
                assertSame(property, RegistriesAS.CRYSTAL_PROPERTIES.getValue(property.getRegistryName()),
                        "generated a property that is not registered: " + property.getRegistryName());
                assertTrue(attribute.getTier() >= 1 && attribute.getTier() <= property.getMaxTier(),
                        property.getRegistryName() + " tier " + attribute.getTier() + " exceeds " + property.getMaxTier());
                assertTrue(attribute.isDiscovered());
            }
        }
    }
}
