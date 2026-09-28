package hellfirepvp.astralsorcery.common.world;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The worldgen data of 1.16.5 was Java: a wrong feature name or a wrong config section could not compile. In 1.20.1
 * the same graph is json loaded by RegistryDataLoader at world creation, where a dangling id shows up as a silent
 * "this feature never generates" (or, for a missing configured_feature, a datapack error long after boot).
 * <p>
 * These tests walk the shipped json and assert the graph is closed:
 * biome_modifier -> placed_feature -> configured_feature, plus the astralsorcery placement modifiers and
 * config sections they reference.
 */
class WorldGenDataIntegrityTest {

    private static final Gson GSON = new Gson();

    private static final Path DATA = Paths.get("src", "main", "resources", "data", "astralsorcery");
    private static final Path CONFIGURED = DATA.resolve(Paths.get("worldgen", "configured_feature"));
    private static final Path PLACED = DATA.resolve(Paths.get("worldgen", "placed_feature"));
    private static final Path BIOME_MODIFIERS = DATA.resolve(Paths.get("forge", "biome_modifier"));
    private static final Path STRUCTURES = DATA.resolve(Paths.get("worldgen", "structure"));
    private static final Path STRUCTURE_SETS = DATA.resolve(Paths.get("worldgen", "structure_set"));
    private static final Path BIOME_TAGS = DATA.resolve(Paths.get("tags", "worldgen", "biome"));

    //Registered by RegistryWorldGeneration#registerWorldGenTypes. Duplicated here on purpose: loading that class
    //pulls in ForgeRegistries, which needs a running FML.
    private static final Set<String> AS_PLACEMENT_TYPES = new HashSet<>(Arrays.asList(
            "astralsorcery:chance", "astralsorcery:riverbed", "astralsorcery:world_filter"));

    //Registered by WorldGenerationAS.FEATURES.
    private static final Set<String> AS_FEATURE_TYPES = new HashSet<>(Arrays.asList(
            "astralsorcery:replace_block", "astralsorcery:rock_crystal"));

    //Registered to Registries.STRUCTURE_TYPE by RegistryWorldGeneration#registerWorldGenTypes.
    private static final Set<String> AS_STRUCTURE_TYPES = new HashSet<>(Arrays.asList(
            "astralsorcery:ancient_shrine", "astralsorcery:desert_shrine", "astralsorcery:small_shrine"));

    //The sections RegistryWorldGeneration#addConfigEntries hands to WorldGenConfig.
    private static final Set<String> AS_CONFIG_SECTIONS = new HashSet<>(Arrays.asList(
            "ancient_shrine", "desert_shrine", "small_shrine",
            "glow_flower", "rock_crystal", "aquamarine", "marble"));

    @Test
    void everyPlacedFeatureResolvesToAConfiguredFeature() throws IOException {
        List<Path> placed = listJson(PLACED);
        assertFalse(placed.isEmpty(), "no placed_feature json found under " + PLACED.toAbsolutePath());

        for (Path file : placed) {
            JsonObject json = read(file);
            String feature = json.get("feature").getAsString();
            assertTrue(feature.startsWith("astralsorcery:"),
                    file + " references a non-astralsorcery configured feature: " + feature);
            Path target = CONFIGURED.resolve(feature.substring("astralsorcery:".length()) + ".json");
            assertTrue(Files.isRegularFile(target),
                    file + " references " + feature + ", but " + target + " does not exist");
        }
    }

    @Test
    void everyBiomeModifierResolvesToAPlacedFeature() throws IOException {
        List<Path> modifiers = listJson(BIOME_MODIFIERS);
        assertFalse(modifiers.isEmpty(), "no biome_modifier json found under " + BIOME_MODIFIERS.toAbsolutePath());

        for (Path file : modifiers) {
            JsonObject json = read(file);
            assertEquals("astralsorcery:config_gated_add_features", json.get("type").getAsString(),
                    file + " uses an unexpected biome modifier type");
            for (String feature : asStringList(json.get("features"))) {
                assertTrue(feature.startsWith("astralsorcery:"),
                        file + " references a non-astralsorcery placed feature: " + feature);
                Path target = PLACED.resolve(feature.substring("astralsorcery:".length()) + ".json");
                assertTrue(Files.isRegularFile(target),
                        file + " references " + feature + ", but " + target + " does not exist");
            }
            //A tag reference that does not start with # would be a single biome id, which is almost never intended.
            for (String biome : asStringList(json.get("biomes"))) {
                assertTrue(biome.startsWith("#") || biome.contains(":"),
                        file + " has a malformed biome reference: " + biome);
            }
        }
    }

    @Test
    void everyConfigEntryReferenceNamesARegisteredSection() throws IOException {
        List<String> referenced = new ArrayList<>();
        for (Path file : listJson(BIOME_MODIFIERS)) {
            referenced.add(read(file).get("config_entry").getAsString());
        }
        for (Path file : listJson(PLACED)) {
            for (JsonElement modifier : read(file).getAsJsonArray("placement")) {
                JsonObject obj = modifier.getAsJsonObject();
                if ("astralsorcery:world_filter".equals(obj.get("type").getAsString())) {
                    referenced.add(obj.get("config_entry").getAsString());
                }
            }
        }
        assertFalse(referenced.isEmpty(), "no config_entry references found at all");
        for (String name : referenced) {
            assertTrue(AS_CONFIG_SECTIONS.contains(name),
                    "config_entry '" + name + "' is not one of the sections addConfigEntries registers; the gate "
                            + "would fall back to 'always generate' instead of honouring the config");
        }
    }

    @Test
    void everyPlacementModifierTypeIsAVanillaOrRegisteredAstralSorceryType() throws IOException {
        for (Path file : listJson(PLACED)) {
            for (JsonElement modifier : read(file).getAsJsonArray("placement")) {
                String type = modifier.getAsJsonObject().get("type").getAsString();
                if (type.startsWith("astralsorcery:")) {
                    assertTrue(AS_PLACEMENT_TYPES.contains(type),
                            file + " uses unregistered placement modifier type " + type);
                } else {
                    assertTrue(type.startsWith("minecraft:"),
                            file + " uses placement modifier type " + type + " from an unexpected namespace");
                }
            }
        }
    }

    @Test
    void astralSorceryConfiguredFeatureTypesAreRegisteredFeatures() throws IOException {
        for (Path file : listJson(CONFIGURED)) {
            String type = read(file).get("type").getAsString();
            if (type.startsWith("astralsorcery:")) {
                assertTrue(AS_FEATURE_TYPES.contains(type),
                        file + " uses unregistered feature type " + type);
            }
        }
    }

    @Test
    void theShippedBiomeCategoryDefaultsAreAllMappedToTags() {
        //Defaults of WorldGenerationAS.Config (1.16.5 WorldGenerationAS.Config:60-91). An unmapped name makes
        //FeatureGenerationConfig#matchesBiome return false for every biome, i.e. the feature never generates.
        for (String category : Arrays.asList("icy", "extreme_hills", "mesa", "desert", "savanna", "forest", "plains")) {
            assertTrue(BiomeCategoryTags.knownCategories().contains(category),
                    "biome category '" + category + "' is used as a shipped default but has no biome tag mapping");
        }
    }

    @Test
    void everyBiomeCategoryMapsToWellFormedTagIds() {
        BiomeCategoryTags.categoryTagIds().forEach((category, ids) -> {
            assertFalse(ids.isEmpty(), "category '" + category + "' maps to no tag at all");
            for (String id : ids) {
                String[] parts = id.split(":", -1);
                assertEquals(2, parts.length, "malformed tag id '" + id + "' for category " + category);
                assertTrue(parts[0].equals("minecraft") || parts[0].equals("forge"),
                        "tag id '" + id + "' is from a namespace that is not guaranteed to exist");
                assertTrue(parts[1].startsWith("is_"), "tag id '" + id + "' is not a biome classification tag");
            }
        });
    }

    @Test
    void everyStructureSetResolvesToAStructure() throws IOException {
        List<Path> sets = listJson(STRUCTURE_SETS);
        assertFalse(sets.isEmpty(), "no structure_set json found under " + STRUCTURE_SETS.toAbsolutePath());

        for (Path file : sets) {
            JsonObject json = read(file);
            JsonObject placement = json.getAsJsonObject("placement");
            assertEquals("minecraft:random_spread", placement.get("type").getAsString(),
                    file + " uses an unexpected structure placement type");
            //1.16.5 StructureGenerationConfig defaults; separation must stay below spacing or the placement throws.
            int spacing = placement.get("spacing").getAsInt();
            int separation = placement.get("separation").getAsInt();
            assertTrue(separation < spacing, file + " has separation >= spacing (" + separation + " >= " + spacing + ")");

            for (JsonElement entry : json.getAsJsonArray("structures")) {
                String structure = entry.getAsJsonObject().get("structure").getAsString();
                assertTrue(structure.startsWith("astralsorcery:"),
                        file + " references a non-astralsorcery structure: " + structure);
                Path target = STRUCTURES.resolve(structure.substring("astralsorcery:".length()) + ".json");
                assertTrue(Files.isRegularFile(target),
                        file + " references " + structure + ", but " + target + " does not exist");
            }
        }
    }

    @Test
    void everyStructureUsesARegisteredTypeAndAnExistingBiomeTag() throws IOException {
        List<Path> structures = listJson(STRUCTURES);
        assertFalse(structures.isEmpty(), "no structure json found under " + STRUCTURES.toAbsolutePath());

        for (Path file : structures) {
            JsonObject json = read(file);
            String type = json.get("type").getAsString();
            assertTrue(AS_STRUCTURE_TYPES.contains(type), file + " uses unregistered structure type " + type);

            String biomes = json.get("biomes").getAsString();
            assertTrue(biomes.startsWith("#astralsorcery:"),
                    file + " should select biomes through an astralsorcery tag, got " + biomes);
            Path tag = BIOME_TAGS.resolve(biomes.substring("#astralsorcery:".length()) + ".json");
            assertTrue(Files.isRegularFile(tag),
                    file + " references biome tag " + biomes + ", but " + tag + " does not exist");
        }
    }

    @Test
    void biomeTagsOnlyReferenceTagsThatForgeOrVanillaShip() throws IOException {
        //Forge ships data/forge/tags/worldgen/biome/is_*.json for these; a tag file referencing a tag that does
        //not exist anywhere makes the whole tag load fail at world creation.
        Set<String> forgeBiomeTags = new HashSet<>(Arrays.asList(
                "#forge:is_desert", "#forge:is_plains", "#forge:is_snowy", "#forge:is_swamp",
                "#forge:is_sandy", "#forge:is_mushroom", "#forge:is_underground", "#forge:is_mountain"));
        for (Path file : listJsonRecursively(BIOME_TAGS)) {
            for (JsonElement value : read(file).getAsJsonArray("values")) {
                String id = value.getAsString();
                if (id.startsWith("#forge:")) {
                    assertTrue(forgeBiomeTags.contains(id), file + " references " + id + ", which Forge does not ship");
                } else {
                    assertTrue(id.startsWith("#minecraft:") || id.startsWith("minecraft:"),
                            file + " references " + id + " from an unexpected namespace");
                }
            }
        }
    }

    private static List<Path> listJson(Path directory) throws IOException {
        assertTrue(Files.isDirectory(directory), directory.toAbsolutePath() + " is not a directory");
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
        }
    }

    private static List<Path> listJsonRecursively(Path directory) throws IOException {
        assertTrue(Files.isDirectory(directory), directory.toAbsolutePath() + " is not a directory");
        try (Stream<Path> files = Files.walk(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .sorted().toList();
        }
    }

    private static JsonObject read(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, JsonObject.class);
        }
    }

    private static List<String> asStringList(JsonElement element) {
        if (element.isJsonArray()) {
            List<String> values = new ArrayList<>();
            element.getAsJsonArray().forEach(e -> values.add(e.getAsString()));
            return values;
        }
        return List.of(element.getAsString());
    }
}
