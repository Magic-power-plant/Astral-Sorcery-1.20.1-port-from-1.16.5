package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.item.tool.CrystalToolTier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Properties.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The numbers ItemCrystalPickaxe reports through ItemCrystalTierItem#getMaxDamage and #getDestroySpeed once the
 * stack carries crystal attributes: max damage is CrystalCalculations#getToolDurability of the tier's uses, and on
 * an effective block the dig speed is CrystalCalculations#getToolEfficiency of (1.0 * tier speed).
 */
class CrystalPickaxeEffectTest {

    private static final int BASE_DURABILITY = CrystalToolTier.getInstance().getUses();
    private static final float BASE_DIG_SPEED = 1.0F * CrystalToolTier.getInstance().getSpeed();

    @BeforeAll
    static void setup() {
        CrystalTestSupport.init();
    }

    @Test
    void tierBaseline() {
        assertEquals(16192, BASE_DURABILITY);
        assertEquals(4.5F, BASE_DIG_SPEED);
    }

    @Test
    void propertiesWithoutAToolUsageLeaveThePickaxeUnchanged() {
        CrystalAttributes attr = CrystalAttributes.Builder.newBuilder(false)
                .addProperty(PROPERTY_PURITY, 2)
                .addProperty(PROPERTY_COLLECTOR_COLLECTION_RATE, 3)
                .build();
        assertEquals(BASE_DURABILITY, CrystalCalculations.getToolDurability(BASE_DURABILITY, attr));
        assertEquals(BASE_DIG_SPEED, CrystalCalculations.getToolEfficiency(BASE_DIG_SPEED, attr), 1e-6F);
    }

    @Test
    void sizeAndToolDurabilityRaiseMaxDamage() {
        CrystalAttributes attr = CrystalAttributes.Builder.newBuilder(false)
                .addProperty(PROPERTY_SIZE, 2)
                .addProperty(PROPERTY_TOOL_DURABILITY, 1)
                .build();
        // 16192 * (1 + 0.15 * 2) * (1 + 0.25 * 1)
        assertEquals(26312, CrystalCalculations.getToolDurability(BASE_DURABILITY, attr));
    }

    @Test
    void shapeAndToolEfficiencyRaiseDigSpeed() {
        CrystalAttributes attr = CrystalAttributes.Builder.newBuilder(false)
                .addProperty(PROPERTY_SHAPE, 3)
                .addProperty(PROPERTY_TOOL_EFFICIENCY, 2)
                .build();
        // 4.5 * (1 + 0.1 * 3) * (1 + 0.15 * 2)
        assertEquals(7.605F, CrystalCalculations.getToolEfficiency(BASE_DIG_SPEED, attr), 1e-4F);
    }

    @Test
    void altarMergedPickaxeKeepsUncappedTiersButTheFormulaCapsThem() {
        // ConstellationBaseMergeStatsRecipe (crystal_pickaxe.json, recipe_class constellation_base_merge) sums the
        // three input crystals with an uncapped builder.
        CrystalAttributes crystal = CrystalAttributes.Builder.newBuilder(false)
                .addProperty(PROPERTY_SIZE, 3)
                .addProperty(PROPERTY_SHAPE, 3)
                .addProperty(PROPERTY_TOOL_EFFICIENCY, 3)
                .build();
        CrystalAttributes merged = CrystalAttributes.Builder.newBuilder(true)
                .addAll(crystal).addAll(crystal).addAll(crystal)
                .build();
        assertEquals(9, merged.getAttribute(PROPERTY_SHAPE).getTier());
        assertEquals(9, merged.getAttribute(PROPERTY_TOOL_EFFICIENCY).getTier());

        // shape counts at most 6 tiers, tool efficiency at most 4: 4.5 * 1.6 * 1.6
        assertEquals(11.52F, CrystalCalculations.getToolEfficiency(BASE_DIG_SPEED, merged), 1e-4F);
        // size is not capped for durability: 16192 * (1 + 0.15 * 9)
        assertEquals(38051, CrystalCalculations.getToolDurability(BASE_DURABILITY, merged));
    }
}
