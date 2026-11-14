package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.crystal.CrystalPropertyRegistry;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RegistryDeclarationsTest {
    @BeforeAll
    static void initialize() {
        RegistryConstellations.init();
        RegistryConstellationEffects.init();
        RegistryEngravingEffects.init();
        RegistryCrystalPropertyUsages.init();
        RegistryCrystalProperties.init();
        RegistryStructureTypes.init();
        RegistryAltarRecipeEffects.init();
    }

    @Test
    void allTwelveRegistryIdsRetainTheirSerializedSpelling() {
        Set<String> expected = Set.of("constellations", "constellation_effect", "mantle_effect", "engraving_effect",
                "structure_types", "perk_attribute_types", "perk_attribute_converters",
                "perk_attribute_custom_modifiers", "perk_attribute_readers", "attribute_crystal_properties",
                "attribute_crystal_usages", "altar_recipe_effects");
        assertEquals(12, RegistriesAS.all().size());
        assertEquals(expected.stream().map(id -> ResourceLocation.parse("astralsorcery:" + id)).collect(Collectors.toSet()),
                RegistriesAS.all().stream().map(ForgeRegistryHandle::getRegistryName).collect(Collectors.toSet()));
        assertThrows(UnsupportedOperationException.class, () -> RegistriesAS.all().clear());
    }

    @Test
    void builtInDomainViewsShareTheCanonicalObjects() {
        assertEquals(16, RegistriesAS.CONSTELLATIONS.getValues().size());
        assertEquals(5, ConstellationRegistry.getMajorConstellations().size());
        assertEquals(12, ConstellationRegistry.getWeakConstellations().size());
        assertEquals(4, ConstellationRegistry.getMinorConstellations().size());
        assertEquals(12, RegistriesAS.CONSTELLATION_EFFECTS.getValues().size());
        assertEquals(16, RegistriesAS.ENGRAVING_EFFECTS.getValues().size());
        assertEquals(20, RegistriesAS.CRYSTAL_PROPERTIES.getValues().size());
        assertEquals(7, RegistriesAS.CRYSTAL_PROPERTY_USAGES.getValues().size());
        assertEquals(12, RegistriesAS.STRUCTURE_TYPES.getValues().size());
        assertSame(ConstellationsAS.aevitas, RegistriesAS.CONSTELLATIONS.getValue(ConstellationsAS.aevitas.getRegistryName()));
        assertSame(CrystalPropertiesAS.Properties.PROPERTY_CST_AEVITAS,
                CrystalPropertyRegistry.INSTANCE.getConstellationProperty(ConstellationsAS.aevitas));
        assertSame(ConstellationsAS.aevitas.getConstellationEffect(),
                RegistriesAS.CONSTELLATION_EFFECTS.getValue(ConstellationsAS.aevitas.getRegistryName()));
        assertSame(ConstellationsAS.aevitas.getEngravingEffect(),
                RegistriesAS.ENGRAVING_EFFECTS.getValue(ConstellationsAS.aevitas.getRegistryName()));
    }

    @Test
    void repeatedInitializationDoesNotReplaceObjectsOrDuplicateEntries() {
        var constellations = RegistriesAS.CONSTELLATIONS.getValues();
        var effects = RegistriesAS.CONSTELLATION_EFFECTS.getValues();
        var crystals = RegistriesAS.CRYSTAL_PROPERTIES.getValues();
        var altarEffects = RegistriesAS.ALTAR_RECIPE_EFFECTS.getValues();
        initialize();
        assertEquals(constellations, RegistriesAS.CONSTELLATIONS.getValues());
        assertEquals(effects, RegistriesAS.CONSTELLATION_EFFECTS.getValues());
        assertEquals(crystals, RegistriesAS.CRYSTAL_PROPERTIES.getValues());
        assertEquals(altarEffects, RegistriesAS.ALTAR_RECIPE_EFFECTS.getValues());
    }

    @Test
    void explicitAltarEffectIdsMatchThePreviousClassDerivedIds() {
        var effects = RegistriesAS.ALTAR_RECIPE_EFFECTS.getValues();
        assertEquals(20, effects.size());
        for (var effect : effects) {
            assertEquals(NameUtil.fromClass(effect, "Effect"), effect.getRegistryName());
            assertSame(effect, RegistriesAS.ALTAR_RECIPE_EFFECTS.getValue(effect.getRegistryName()));
        }
    }

    @Test
    void forgeCreationPreservesSaveSyncAndMutationPoliciesWithoutResolvingEarly() {
        CapturingNewRegistryEvent event = new CapturingNewRegistryEvent();
        ForgeRegistryHandle<ResourceLocation> ordinary = new ForgeRegistryHandle<>(
                ResourceLocation.parse("test:ordinary"), id -> id);
        ordinary.register(ResourceLocation.parse("test:value"));
        ordinary.create(event);
        assertFalse(event.builder.getSaveToDisc());
        assertFalse(event.builder.getSync());
        assertFalse(event.builder.getAllowModifications());
        assertEquals(List.of(ResourceLocation.parse("test:value")), ordinary.getValues());
        assertThrows(IllegalStateException.class, () -> ordinary.create(event));

        ForgeRegistryHandle<ResourceLocation> mutable = new ForgeRegistryHandle<>(
                ResourceLocation.parse("test:mutable"), id -> id, true);
        mutable.create(event);
        assertFalse(event.builder.getSaveToDisc());
        assertTrue(event.builder.getSync());
        assertTrue(event.builder.getAllowModifications());
    }

    private static class CapturingNewRegistryEvent extends NewRegistryEvent {
        private RegistryBuilder<?> builder;
        @Override
        public <V> Supplier<IForgeRegistry<V>> create(RegistryBuilder<V> builder, Consumer<IForgeRegistry<V>> onFill) {
            this.builder = builder;
            assertNotNull(onFill, "must bind onFill, not call the unpopulated supplier");
            return () -> { throw new AssertionError("Registry supplier was resolved before onFill"); };
        }
    }
}
