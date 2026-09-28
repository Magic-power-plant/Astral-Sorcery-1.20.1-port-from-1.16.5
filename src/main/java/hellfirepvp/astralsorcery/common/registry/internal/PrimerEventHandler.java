/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.internal;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PrimerEventHandler
 * Created by HellFirePvP
 * Date: 26.06.2017 / 14:50
 */
//BLOCKED_PORT[AS-REG-04]: PrimerEventHandler cannot be ported to 1.20.1 (1.16.5
// common/registry/internal/PrimerEventHandler.java:55-271). Its entire body is built out of APIs Forge removed:
//   - IEventBus#addGenericListener(Class, Consumer)  -> removed together with generic events in 1.19
//   - net.minecraftforge.event.RegistryEvent.Register<T> -> replaced by the single, key-dispatched RegisterEvent
//   - IForgeRegistry#getRegistrySuperType() / IForgeRegistryEntry -> removed in 1.19
//   - net.minecraftforge.registries.DataSerializerEntry -> removed (EntityDataSerializers/Forge registry instead)
//   - net.minecraftforge.common.loot.GlobalLootModifierSerializer -> replaced by codec-based IGlobalLootModifier
//   - net.minecraft.world.gen.placement.Placement -> replaced by PlacementModifier/PlacementModifierType
//   - net.minecraft.world.gen.feature.structure.Structure -> split into Structure/StructureType
//   - hellfirepvp.observerlib.api.ObserverProvider / .structure.MatchableStructure -> ObserverLib is a
//     source-only reference in this port and must never be imported (embedded copies live under
//     common/data/world/cache, common/event and client/util/draw).
// It also depends on InternalRegistryPrimer, which is itself blocked - see BLOCKED_PORT[AS-REG-03] in this package.
//
// Re-implementing the class against RegisterEvent would not be a port: it would be a second registration path,
// running in parallel with the DeferredRegister holders the 1.20.1 target already uses, and would register every
// object twice. The 28 generic listeners of attachEventHandlers therefore map onto the target as follows:
//
//   Item                          -> ItemsAS.ITEMS (DeferredRegister), plus RegistryItems for the
//                                    non-registry item side work (colors, item properties, dispense behaviours)
//   Block                         -> BlocksAS.BLOCKS (DeferredRegister); RegistryBlocks for the client-side work
//   Fluid                         -> FluidsAS.FLUIDS / FluidsAS.FLUID_TYPES
//   TileEntityType                -> TileEntityTypesAS.TILE_ENTITIES
//   EntityType                    -> EntityTypesAS.ENTITIES; RegistryEntities for attributes/renderers/spawns
//   Feature                       -> WorldGenerationAS.FEATURES
//   Placement                     -> 1.20.1 has no Placement registry entry; placement modifiers are plain
//                                    values inside a PlacedFeature (see RegistryWorldGeneration)
//   Effect (MobEffect)            -> EffectsAS.EFFECTS; RegistryEffects for the event-listener attachment
//   Enchantment                   -> EnchantmentsAS.ENCHANTMENTS
//   SoundEvent                    -> SoundsAS.SOUNDS
//   GlobalLootModifierSerializer  -> codec-based, see RegistryLoot
//   IConstellation                -> RegistriesAS.REGISTRY_CONSTELLATIONS, filled by ConstellationRegistry
//   DataSerializerEntry           -> see RegistryDataSerializers / ASDataSerializers
//   IRecipeSerializer             -> RecipeSerializersAS.RECIPE_SERIALIZERS
//   MatchableStructure            -> blocked, ObserverLib type (see RegistryStructures)
//   StructureType                 -> StructureTypesAS holds the instances; RegistryRegistries
//                                    #registerStructureTypes(RegisterEvent) flushes them into
//                                    RegistriesAS.REGISTRY_STRUCTURE_TYPES
//   ObserverProvider              -> blocked, ObserverLib type (see RegistryStructures)
//   ConstellationEffectProvider   -> RegistriesAS.REGISTRY_CONSTELLATION_EFFECT / RegistryConstellationEffects
//   MantleEffect                  -> RegistriesAS.REGISTRY_MANTLE_EFFECT / RegistryMantleEffects
//   EngravingEffect               -> RegistriesAS.REGISTRY_ENGRAVING_EFFECT / RegistryEngravingEffects
//   PerkAttributeType             -> RegistryRegistries#registerBuiltInPerkAttributeReaders(RegisterEvent)
//   PerkConverter                 -> RegistryRegistries#registerBuiltInPerkAttributeReaders(RegisterEvent)
//   PerkAttributeModifier         -> RegistryRegistries#registerBuiltInPerkAttributeReaders(RegisterEvent)
//   PerkAttributeReader           -> RegistryRegistries#registerBuiltInPerkAttributeReaders(RegisterEvent)
//   ContainerType (MenuType)      -> MenuTypesAS.MENU_TYPES; RegistryContainerTypes for the screen factories
//   CrystalProperty               -> RegistriesAS.REGISTRY_CRYSTAL_PROPERTIES / RegistryCrystalProperties
//   PropertyUsage                 -> RegistriesAS.REGISTRY_CRYSTAL_USAGES / RegistryCrystalPropertyUsages
//   AltarRecipeEffect             -> RegistriesAS.REGISTRY_ALTAR_EFFECTS / RegistryRecipeTypes#initAltarEffects
//   Structure (worldgen)          -> see RegistryStructures / RegistryStructureTypes
//
// The one piece of PrimerEventHandler that is real, order-sensitive behaviour rather than plumbing is
// registerRemainingData() (1.16.5 PrimerEventHandler.java:97-120). common/CommonProxy#initialize() is where that
// sequence lives in the 1.20.1 tree, and it is currently incomplete. Present there: RegistryMantleEffects.init(),
// RegistryConstellationEffects.init(), RegistryEngravingEffects.init(), RegistryCrystalPropertyUsages.init(),
// RegistryCrystalProperties.init(), RegistryCrystalProperties.initDefaultAttributes(),
// RegistryPerkAttributeTypes.init() (which itself chains RegistryPerkAttributeReaders/PerkConverters/
// PerkCustomModifiers, exactly as 1.16.5 did after it), TransmissionClassRegistry/SourceClassRegistry.
// Still absent, and reported as external changes on CommonProxy rather than invented here:
//   RegistryRecipeTypes.init();       (1.16.5 ref:109)
//   RegistryResearch.init();          (ref:111)
// Three more entries of the 1.16.5 list have no 1.20.1 counterpart to call:
//   RegistryStructures.init();        (ref:105) - the pattern instances are built by common/lib/StructuresAS now
//                                       and its primer/ObserverProvider half is blocked (see RegistryStructures)
//   RegistryWorldGeneration.init();   (ref:106) - the 1.20.1 RegistryWorldGeneration has no init(): feature
//                                       registration is a DeferredRegister and the configured/placed features are
//                                       datapack entries (see TODO_PORT[AS-REG-05] there)
//   RegistryRecipeSerializers.init(); (ref:110) - the seven serializers are a DeferredRegister in
//                                       common/crafting/RecipeSerializersAS (see RegistryRecipeSerializers)
//
// This class is intentionally left without members - see the InternalRegistryPrimer note.
public final class PrimerEventHandler {

    private PrimerEventHandler() {}
}
