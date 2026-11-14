package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProvider;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffect;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;
import hellfirepvp.astralsorcery.common.perk.PerkConverter;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.registry.ForgeRegistryHandle;

import java.util.List;

/** Custom registry identities and handles. Serialized identifiers deliberately retain their historic spelling. */
public final class RegistriesAS {
    private RegistriesAS() {}

    public static final ForgeRegistryHandle<IConstellation> CONSTELLATIONS = new ForgeRegistryHandle<>(
            AstralSorcery.key("constellations"), IConstellation::getRegistryName);
    public static final ForgeRegistryHandle<ConstellationEffectProvider> CONSTELLATION_EFFECTS = new ForgeRegistryHandle<>(
            AstralSorcery.key("constellation_effect"), ConstellationEffectProvider::getRegistryName);
    public static final ForgeRegistryHandle<MantleEffect> MANTLE_EFFECTS = new ForgeRegistryHandle<>(
            AstralSorcery.key("mantle_effect"), effect -> effect.getAssociatedConstellation().getRegistryName());
    public static final ForgeRegistryHandle<EngravingEffect> ENGRAVING_EFFECTS = new ForgeRegistryHandle<>(
            AstralSorcery.key("engraving_effect"), EngravingEffect::getRegistryName);
    public static final ForgeRegistryHandle<StructureType> STRUCTURE_TYPES = new ForgeRegistryHandle<>(
            AstralSorcery.key("structure_types"), StructureType::getRegistryName);
    public static final ForgeRegistryHandle<PerkAttributeType> PERK_ATTRIBUTE_TYPES = new ForgeRegistryHandle<>(
            AstralSorcery.key("perk_attribute_types"), PerkAttributeType::getRegistryName);
    public static final ForgeRegistryHandle<PerkConverter> PERK_CONVERTERS = new ForgeRegistryHandle<>(
            AstralSorcery.key("perk_attribute_converters"), PerkConverter::getRegistryName, true);
    public static final ForgeRegistryHandle<PerkAttributeModifier> PERK_CUSTOM_MODIFIERS = new ForgeRegistryHandle<>(
            AstralSorcery.key("perk_attribute_custom_modifiers"), PerkAttributeModifier::getRegistryName, true);
    public static final ForgeRegistryHandle<PerkAttributeReader> PERK_ATTRIBUTE_READERS = new ForgeRegistryHandle<>(
            AstralSorcery.key("perk_attribute_readers"), reader -> reader.getType().getRegistryName());
    public static final ForgeRegistryHandle<CrystalProperty> CRYSTAL_PROPERTIES = new ForgeRegistryHandle<>(
            AstralSorcery.key("attribute_crystal_properties"), CrystalProperty::getRegistryName);
    public static final ForgeRegistryHandle<PropertyUsage> CRYSTAL_PROPERTY_USAGES = new ForgeRegistryHandle<>(
            AstralSorcery.key("attribute_crystal_usages"), PropertyUsage::getRegistryName);
    public static final ForgeRegistryHandle<AltarRecipeEffect> ALTAR_RECIPE_EFFECTS = new ForgeRegistryHandle<>(
            AstralSorcery.key("altar_recipe_effects"), AltarRecipeEffect::getRegistryName);

    private static final List<ForgeRegistryHandle<?>> ALL = List.of(
            CONSTELLATIONS, CONSTELLATION_EFFECTS, MANTLE_EFFECTS,
            ENGRAVING_EFFECTS, STRUCTURE_TYPES, PERK_ATTRIBUTE_TYPES,
            PERK_CONVERTERS, PERK_CUSTOM_MODIFIERS, PERK_ATTRIBUTE_READERS,
            CRYSTAL_PROPERTIES, CRYSTAL_PROPERTY_USAGES, ALTAR_RECIPE_EFFECTS);

    public static List<ForgeRegistryHandle<?>> all() {
        return ALL;
    }
}
