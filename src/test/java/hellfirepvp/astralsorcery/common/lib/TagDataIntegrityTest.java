package hellfirepvp.astralsorcery.common.lib;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 1.16.5 declared its tags in code: common/lib/TagsAS held the {@code ITag.INamedTag} constants and the datagen
 * providers (datagen/data/tags/AstralBlockTagsProvider, AstralItemTagsProvider) wrote the contents, so a missing or
 * empty tag broke the build. 1.20.1 has no code-side tag registration at all - the json under
 * {@code data/<namespace>/tags/<kind>/} is the whole definition - which means a broken tag is silent: it resolves to
 * nothing and every "is this item/block in tag X" question quietly answers no.
 * <p>
 * TagsAS has therefore been dropped in favour of those json files. These tests keep the new failure modes visible:
 * a tag the mod used to declare losing its file, a tag file that is empty, a tag file whose {@code "replace"} would
 * erase every other mod's contribution to the same tag, and an entry pointing at an astralsorcery item/block that
 * ships no model/blockstate.
 * <p>
 * The asset check stands in for a registry lookup: loading {@code ForgeRegistries} needs a bootstrapped game, but
 * every registered astralsorcery item has a model and every block a blockstate, so a dangling entry is detectable
 * from the resource tree alone.
 * <p>
 * The coverage check is the other half of the port: 1.16.5 kept the harvest tool and tier in code
 * ({@code harvestTool}/{@code harvestLevel}), which Forge removed with {@code ToolType} in 1.18, so in 1.20.1 a
 * block's mining behaviour exists only in these tag files. A block that is in none of the three
 * {@code minecraft:mineable/*} tags is mined by hand at 1.0 speed regardless of its hardness, and a block that also
 * calls {@code requiresCorrectToolForDrops()} can never drop at all. Both failures are silent, hence the check that
 * every block registered with an item is either mineable or listed as deliberately tool-free.
 */
class TagDataIntegrityTest {

    private static final Gson GSON = new Gson();

    private static final Path RESOURCES = Paths.get("src", "main", "resources");
    private static final Path DATA = RESOURCES.resolve("data");
    private static final Path ITEM_MODELS = RESOURCES.resolve(Paths.get("assets", "astralsorcery", "models", "item"));
    private static final Path BLOCKSTATES = RESOURCES.resolve(Paths.get("assets", "astralsorcery", "blockstates"));

    private static final String AS_NAMESPACE = "astralsorcery";

    /**
     * The three vanilla tags that decide which tool mines a block. 1.20.1 has no code-side equivalent, so membership
     * here is the whole definition of a block's harvest tool.
     */
    private static final List<String> MINING_TAGS = List.of(
            "minecraft/tags/blocks/mineable/pickaxe.json",
            "minecraft/tags/blocks/mineable/axe.json",
            "minecraft/tags/blocks/mineable/shovel.json");

    /** Vanilla tier gates; an entry only has an effect when the block is also in one of {@link #MINING_TAGS}. */
    private static final List<String> NEEDS_TOOL_TAGS = List.of(
            "minecraft/tags/blocks/needs_stone_tool.json",
            "minecraft/tags/blocks/needs_iron_tool.json",
            "minecraft/tags/blocks/needs_diamond_tool.json");

    /** Source of truth for "which blocks have an item", since no registry can be read without a running game. */
    private static final Path BLOCKS_AS = Paths.get("src", "main", "java", "hellfirepvp", "astralsorcery",
            "common", "lib", "BlocksAS.java");

    private static final Pattern ITEM_BLOCK_REGISTRATION =
            Pattern.compile("registerBlockWithItem\\(\"([a-z0-9_]+)\"");

    /**
     * Every tag 1.16.5 declared in common/lib/TagsAS, mapped to the data file that now defines it. The class is gone,
     * so this table is the remaining record that all seven tags - including the four that live in other mods'
     * namespaces, which no astralsorcery json would otherwise mention - are expected to ship as data.
     */
    private static final Map<String, String> FORMER_TAGSAS_TAGS = new LinkedHashMap<>();

    static {
        FORMER_TAGSAS_TAGS.put("astralsorcery:stardust", "astralsorcery/tags/items/stardust.json");
        FORMER_TAGSAS_TAGS.put("astralsorcery:starmetal", "astralsorcery/tags/items/starmetal.json");
        FORMER_TAGSAS_TAGS.put("astralsorcery:colored_lens", "astralsorcery/tags/items/colored_lens.json");
        FORMER_TAGSAS_TAGS.put("curios:necklace", "curios/tags/items/necklace.json");
        FORMER_TAGSAS_TAGS.put("forge:gems/aquamarine", "forge/tags/items/gems/aquamarine.json");
        FORMER_TAGSAS_TAGS.put("forge:marble", "forge/tags/blocks/marble.json");
        FORMER_TAGSAS_TAGS.put("forge:ores", "forge/tags/blocks/ores.json");
    }

    @Test
    void everyFormerlyClassDeclaredTagShipsAsData() throws IOException {
        for (Map.Entry<String, String> tag : FORMER_TAGSAS_TAGS.entrySet()) {
            Path file = DATA.resolve(tag.getValue());
            assertTrue(Files.isRegularFile(file), tag.getKey() + " has no datapack definition: " + file + " is missing");
            assertFalse(values(file).isEmpty(),
                    tag.getKey() + " is defined by an empty tag file (" + file + "), so it matches nothing");
        }
    }

    @Test
    void shippedTagFilesAreNonEmptyAndNeverReplace() throws IOException {
        List<Path> tagFiles = listTagFiles();
        assertFalse(tagFiles.isEmpty(), "no tag json found under " + DATA.toAbsolutePath());

        for (Path file : tagFiles) {
            JsonObject json = read(file);
            JsonArray values = json.getAsJsonArray("values");
            assertNotNull(values, file + " has no \"values\" array");
            assertTrue(values.size() > 0, file + " lists no values, so the tag matches nothing");

            //"replace": true drops every entry other mods (or vanilla) put in the same tag before this file is applied.
            if (json.has("replace")) {
                assertFalse(json.get("replace").getAsBoolean(),
                        file + " uses \"replace\": true, which discards every other mod's entries in the same tag");
            }
        }
    }

    @Test
    void entriesPointAtShippedAstralsorceryAssets() throws IOException {
        List<Path> tagFiles = listTagFiles();
        assertFalse(tagFiles.isEmpty(), "no tag json found under " + DATA.toAbsolutePath());

        for (Path file : tagFiles) {
            String kind = tagKind(file);
            for (String id : values(file)) {
                if (id.startsWith("#")) {
                    //Nested tag reference, resolved by the tag itself rather than by an asset.
                    continue;
                }
                if (!id.startsWith(AS_NAMESPACE + ":")) {
                    //Entries in other namespaces belong to those mods; nothing here can verify them.
                    continue;
                }
                String path = id.substring(AS_NAMESPACE.length() + 1);
                if ("items".equals(kind)) {
                    assertTrue(Files.isRegularFile(ITEM_MODELS.resolve(path + ".json")),
                            file + " lists " + id + ", which ships no item model (" + ITEM_MODELS.resolve(path + ".json") + ")");
                } else if ("blocks".equals(kind)) {
                    assertTrue(Files.isRegularFile(BLOCKSTATES.resolve(path + ".json")),
                            file + " lists " + id + ", which ships no blockstate (" + BLOCKSTATES.resolve(path + ".json") + ")");
                }
            }
        }
    }

    @Test
    void everyBlockWithAnItemIsMineableOrDeliberatelyToolFree() throws IOException {
        //Two blocks of this mod are intentionally outside the mining tags:
        // - spectral_relay is coated glass: 1.16.5 gave it neither harvestTool nor harvestLevel, and
        //   BlockSpectralRelay never calls requiresCorrectToolForDrops(), so any tool breaks it at normal speed;
        // - glow_flower extends FlowerBlock and is a plant: plants are broken instantly and carry no mining tag.
        Map<String, String> toolFree = new LinkedHashMap<>();
        toolFree.put("spectral_relay", "coated glass without harvestTool/harvestLevel or requiresCorrectToolForDrops");
        toolFree.put("glow_flower", "FlowerBlock plant");

        Set<String> mineable = new LinkedHashSet<>();
        for (String file : MINING_TAGS) {
            mineable.addAll(values(DATA.resolve(file)));
        }

        List<String> itemBlocks = registeredItemBlockIds();
        assertFalse(itemBlocks.isEmpty(), "no registerBlockWithItem(\"...\") call found in " + BLOCKS_AS);

        for (String id : itemBlocks) {
            String blockId = AS_NAMESPACE + ":" + id;
            if (toolFree.containsKey(id)) {
                assertFalse(mineable.contains(blockId),
                        blockId + " is declared tool-free but appears in " + MINING_TAGS
                                + "; remove it from the tag or drop the exemption");
                continue;
            }
            assertTrue(mineable.contains(blockId),
                    blockId + " ships an item but is in none of " + MINING_TAGS
                            + "; in 1.20.1 that means it is mined by hand at 1.0 speed regardless of hardness,"
                            + " and with requiresCorrectToolForDrops() it can never drop: " + BLOCKS_AS);
        }
        for (String id : toolFree.keySet()) {
            assertTrue(itemBlocks.contains(id),
                    "stale tool-free exemption for " + id + ": it is no longer a block registered with an item");
        }
    }

    @Test
    void needsToolTagsOnlyGateBlocksThatAreAlsoMineable() throws IOException {
        Set<String> mineable = new LinkedHashSet<>();
        for (String file : MINING_TAGS) {
            mineable.addAll(values(DATA.resolve(file)));
        }

        for (String file : NEEDS_TOOL_TAGS) {
            Path path = DATA.resolve(file);
            if (!Files.isRegularFile(path)) {
                continue;
            }
            for (String id : values(path)) {
                assertTrue(mineable.contains(id),
                        path + " requires a tool tier for " + id + ", but " + id + " is in none of " + MINING_TAGS
                                + "; the tier gate does nothing without a harvest tool");
            }
        }
    }

    /**
     * The block ids registered through {@code BlocksAS#registerBlockWithItem}, read from the source because a
     * registry lookup needs a bootstrapped game and this test deliberately does not boot one.
     */
    private static List<String> registeredItemBlockIds() throws IOException {
        Set<String> ids = new LinkedHashSet<>();
        Matcher matcher = ITEM_BLOCK_REGISTRATION.matcher(Files.readString(BLOCKS_AS, StandardCharsets.UTF_8));
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return new ArrayList<>(ids);
    }

    /**
     * The values of one tag file, with object-form entries ({@code {"id": ..., "required": false}}) unwrapped to
     * their id so callers do not have to care which spelling a file uses.
     */
    private static List<String> values(Path file) throws IOException {
        JsonObject json = read(file);
        JsonArray values = json.getAsJsonArray("values");
        assertNotNull(values, file + " has no \"values\" array");

        List<String> ids = new ArrayList<>(values.size());
        for (JsonElement value : values) {
            if (value.isJsonPrimitive()) {
                ids.add(value.getAsString());
            } else {
                JsonObject entry = value.getAsJsonObject();
                assertTrue(entry.has("id"), file + " contains an object entry without \"id\": " + entry);
                ids.add(entry.get("id").getAsString());
            }
        }
        return ids;
    }

    /**
     * A tag file's {@code <kind>} segment, i.e. {@code items}/{@code blocks}/... in
     * {@code data/<namespace>/tags/<kind>/<path>.json}.
     */
    private static String tagKind(Path file) {
        List<String> segments = segments(file);
        return segments.get(2);
    }

    private static List<Path> listTagFiles() throws IOException {
        try (Stream<Path> files = Files.walk(DATA)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(TagDataIntegrityTest::isTagFile)
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    private static boolean isTagFile(Path file) {
        List<String> segments = segments(file);
        //<namespace>/tags/<kind>/... - the short path data/<namespace>/tags/<kind>/<one file>.json has four segments.
        return segments.size() >= 4 && "tags".equals(segments.get(1));
    }

    private static List<String> segments(Path file) {
        Path relative = DATA.relativize(file);
        List<String> out = new ArrayList<>();
        relative.forEach(part -> out.add(part.toString()));
        return out;
    }

    private static JsonObject read(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement json = GSON.fromJson(reader, JsonElement.class);
            assertNotNull(json, file + " is empty");
            assertTrue(json.isJsonObject(), file + " is not a json object");
            return json.getAsJsonObject();
        }
    }
}
