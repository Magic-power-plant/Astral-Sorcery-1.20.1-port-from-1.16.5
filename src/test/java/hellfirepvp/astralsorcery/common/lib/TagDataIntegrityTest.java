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
import java.util.List;
import java.util.Map;
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
 */
class TagDataIntegrityTest {

    private static final Gson GSON = new Gson();

    private static final Path RESOURCES = Paths.get("src", "main", "resources");
    private static final Path DATA = RESOURCES.resolve("data");
    private static final Path ITEM_MODELS = RESOURCES.resolve(Paths.get("assets", "astralsorcery", "models", "item"));
    private static final Path BLOCKSTATES = RESOURCES.resolve(Paths.get("assets", "astralsorcery", "blockstates"));

    private static final String AS_NAMESPACE = "astralsorcery";

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
