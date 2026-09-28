/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.placement.ChancePlacement;
import hellfirepvp.astralsorcery.common.world.placement.RiverbedPlacement;
import hellfirepvp.astralsorcery.common.world.placement.WorldFilteredPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Consumer;

import static hellfirepvp.astralsorcery.common.lib.WorldGenerationAS.Config.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryWorldGeneration
 * Created by HellFirePvP
 * Date: 18.11.2020 / 21:17
 */
// Port notes for 1.16.5 common/registry/RegistryWorldGeneration.java (193 lines):
//
// The 1.16.5 class did seven things. Where each one lives in 1.20.1:
//   1. registerFeature(KEY, Feature)              (ref:72-73, 173-175)
//      -> WorldGenerationAS.FEATURES (DeferredRegister<Feature<?>>), handed to the mod bus by
//         RegistryRegistries#init and filled by WorldGenerationAS#init. Not duplicated here; doing so would
//         register replace_block / rock_crystal twice.
//   2. registerPlacement(KEY, Placement)          (ref:75-77, 177-179)
//      -> ported below. In 1.16.5 the Placement instance was itself the registry entry
//         (Placement extends ForgeRegistryEntry); in 1.20.1 the registry entry is the PlacementModifierType
//         (a codec holder) in BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, and the modifier itself is a plain
//         value inside a PlacedFeature. The three types are therefore registered through RegisterEvent.
//   3. registerStructurePiece(KEY, IStructurePieceType) (ref:79-81, 181-183)
//      -> registered below; WorldGenerationAS#init only constructs the instances, because STRUCTURE_PIECE is
//         frozen during mod construction.
//   4. registerStructure(KEY, cfg, TemplateStructureFeature) (ref:83-85, 185-192)
//      -> split in two by 1.18/1.19: the *type* (StructureType, a codec holder) is a BuiltInRegistries entry and
//         is registered below; the *instance* (Structure) and its placement are datapack entries, see
//         data/astralsorcery/worldgen/structure/*.json and .../structure_set/*.json.
//         Structure.NAME_STRUCTURE_BIMAP and StructureFeature (the "configured structure feature") are gone.
//   5. registerConfiguredFeature(...) + the four GEN_* definitions (ref:87-110, 167-171)
//      -> data/astralsorcery/worldgen/configured_feature/*.json + .../placed_feature/*.json. ConfiguredFeature
//         and PlacedFeature stopped being built-in registries in 1.18.2 (RegistryDataLoader loads them from the
//         datapack), so they cannot be created from code at mod-construction time at all. The translation of each
//         1.16.5 body is documented in the json's sibling notes below.
//   6. registerStructureGeneration() (ref:113-133) / loadBiomeFeatures(BiomeLoadingEvent) (ref:135-154)
//      -> the structure half is data/astralsorcery/worldgen/structure_set/*.json (DimensionStructuresSettings.DEFAULTS
//         and Structure.NOISE_AFFECTING_FEATURES, the two static fields 1.16.5 mutated, were both deleted in
//         1.18.2 in favour of Registries.STRUCTURE_SET + StructurePlacement).
//         The feature half is common/world/biome/ConfigGatedAddFeaturesBiomeModifier +
//         data/astralsorcery/forge/biome_modifier/*.json: BiomeLoadingEvent and
//         BiomeGenerationSettingsBuilder#withStructure/#withFeature were removed in 1.19, and Biome.Category
//         (what FeatureGenerationConfig#canGenerateIn matched on) was deleted in 1.19.3, so the category names are
//         mapped onto biome tags by FeatureGenerationConfig#matchesBiome.
//         The three 1.16.5 fields STRUCTURES / FEATURES / FEATURE_STAGE (ref:67-69) only existed to feed those two
//         methods and have no 1.20.1 counterpart.
//   7. addConfigEntries(Consumer<ConfigEntry>) (ref:156-165) -> ported below, verbatim. Called from
//      CommonProxy#initializeConfigurations, as in 1.16.5.
//
// Two things 1.16.5 could do that 1.20.1 data cannot, both carried by StructureGenerationConfig:
//   - spacing / separation are now part of the structure_set json (a datapack registry entry that is baked into
//     the world's dimension settings), so editing them in astralsorcery-common.toml no longer has any effect.
//     The json carries the 1.16.5 defaults (18 / 4) and the salt 1.16.5 derived from the config path.
//   - "enabled" for structures is checked in TemplateStructureFeature#findGenerationPoint instead. Forge's
//     StructureModifier would be the natural place, but Structure#biomes() reads the unmodified settings in
//     47.4.20 and nothing calls getModifiedStructureSettings(), so a modifier there would never be consulted.

@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryWorldGeneration {

    // 1.16.5 kept these three ids in WorldGenerationAS.Placements together with the Placement singletons.
    // WorldGenerationAS has no Placements holder in the 1.20.1 target (the placement modifiers are constructed
    // per PlacedFeature entry instead of being registry singletons), so the ids live with the type registration.
    public static final ResourceLocation KEY_PLACEMENT_CHANCE = AstralSorcery.key("chance");
    public static final ResourceLocation KEY_PLACEMENT_RIVERBED = AstralSorcery.key("riverbed");
    public static final ResourceLocation KEY_PLACEMENT_WORLD_FILTER = AstralSorcery.key("world_filter");

    @SubscribeEvent
    public static void registerWorldGenTypes(RegisterEvent event) {
        // 1.16.5: registerPlacement(KEY_PLACEMENT_*, CHANCE/RIVERBED/WORLD_FILTER) (ref:75-77). The registered
        // instance has to be the very object the modifier's type() hands back, hence the TYPE constants.
        event.register(Registries.PLACEMENT_MODIFIER_TYPE, KEY_PLACEMENT_CHANCE, () -> ChancePlacement.TYPE);
        event.register(Registries.PLACEMENT_MODIFIER_TYPE, KEY_PLACEMENT_RIVERBED, () -> RiverbedPlacement.TYPE);
        event.register(Registries.PLACEMENT_MODIFIER_TYPE, KEY_PLACEMENT_WORLD_FILTER, () -> WorldFilteredPlacement.TYPE);

        // 1.16.5: registerStructure(KEY_*_SHRINE, CFG_*_SHRINE, new Feature*ShrineStructure()) (ref:83-85).
        // The StructureType instances are created by WorldGenerationAS#init, which runs from
        // RegistryRegistries#init during mod construction, i.e. strictly before RegisterEvent is fired.
        event.register(Registries.STRUCTURE_TYPE, WorldGenerationAS.Structures.KEY_ANCIENT_SHRINE,
                () -> WorldGenerationAS.Structures.ANCIENT_SHRINE_TYPE);
        event.register(Registries.STRUCTURE_TYPE, WorldGenerationAS.Structures.KEY_DESERT_SHRINE,
                () -> WorldGenerationAS.Structures.DESERT_SHRINE_TYPE);
        event.register(Registries.STRUCTURE_TYPE, WorldGenerationAS.Structures.KEY_SMALL_SHRINE,
                () -> WorldGenerationAS.Structures.SMALL_SHRINE_TYPE);

        // 1.16.5: registerStructurePiece(KEY_*_SHRINE, *_SHRINE_PIECE) (ref:79-81). Created by WorldGenerationAS#init
        // like the types above; registering them there directly hit the frozen vanilla registry.
        event.register(Registries.STRUCTURE_PIECE, WorldGenerationAS.Structures.KEY_ANCIENT_SHRINE,
                () -> WorldGenerationAS.Structures.ANCIENT_SHRINE_PIECE);
        event.register(Registries.STRUCTURE_PIECE, WorldGenerationAS.Structures.KEY_DESERT_SHRINE,
                () -> WorldGenerationAS.Structures.DESERT_SHRINE_PIECE);
        event.register(Registries.STRUCTURE_PIECE, WorldGenerationAS.Structures.KEY_SMALL_SHRINE,
                () -> WorldGenerationAS.Structures.SMALL_SHRINE_PIECE);
    }

    public static void addConfigEntries(Consumer<ConfigEntry> registrar) {
        registrar.accept(CFG_ANCIENT_SHRINE);
        registrar.accept(CFG_DESERT_SHRINE);
        registrar.accept(CFG_SMALL_SHRINE);

        registrar.accept(CFG_GLOW_FLOWER);
        registrar.accept(CFG_ROCK_CRYSTAL);
        registrar.accept(CFG_AQUAMARINE);
        registrar.accept(CFG_MARBLE);
    }
}
