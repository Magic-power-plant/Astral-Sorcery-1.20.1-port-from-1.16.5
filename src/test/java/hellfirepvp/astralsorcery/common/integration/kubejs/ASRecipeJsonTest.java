package hellfirepvp.astralsorcery.common.integration.kubejs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the json the KubeJS schemas emit against the json Astral Sorcery's own recipe serializers read.
 *
 * A KubeJS RecipeSchema never hands a Java object to a serializer - it builds a JsonObject that is fed
 * straight through the datapack recipe loader - so the only thing that can silently make a script-created
 * recipe fail is the field layout. ASRecipeJson is the part of the integration that decides that layout, and
 * it is written against plain Gson precisely so it can be pinned here: kubejs-forge is a compileOnly
 * dependency and is therefore absent from testCompileClasspath, and the components that use it would drag in
 * RegistryInfo and so BuiltInRegistries, which needs Bootstrap#bootStrap and cannot run in this harness.
 *
 * Several cases below compare against the actual shipped recipe files under src/main/resources rather than
 * against a hand-written expectation, so the test fails if either side drifts.
 */
public class ASRecipeJsonTest {

    private static final Gson GSON = new Gson();

    @Test
    void aBlockIdBecomesTheObjectBlockStateHelperReads() {
        //BlockStateHelper#deserializeObject reads GsonHelper.getAsString(object, "block").
        JsonObject out = ASRecipeJson.blockStateObject(new JsonPrimitive("astralsorcery:starmetal_ore"));
        assertEquals(1, out.size());
        assertEquals("astralsorcery:starmetal_ore", out.get("block").getAsString());
    }

    @Test
    void theOutputBlockMatchesTheShippedTransmutationRecipe() {
        JsonObject shipped = readShipped("/data/astralsorcery/recipes/block_transmutation/iron_starmetal.json");
        assertEquals(shipped.getAsJsonObject("output"),
                ASRecipeJson.blockStateObject(new JsonPrimitive("astralsorcery:starmetal_ore")));
    }

    @Test
    void aLeadingHashSelectsTheBlockTagForm() {
        //BlockMatchInformation#read branches on "block" vs "tag"; the '#' prefix is the script-side spelling.
        JsonObject shipped = readShipped("/data/astralsorcery/recipes/block_transmutation/iron_starmetal.json");
        JsonObject shippedInput = shipped.getAsJsonArray("input").get(0).getAsJsonObject();
        assertEquals(shippedInput, ASRecipeJson.blockMatch(new JsonPrimitive("#forge:ores/iron")));
    }

    @Test
    void aBareIdSelectsTheBlockForm() {
        JsonObject shipped = readShipped("/data/astralsorcery/recipes/block_transmutation/craftingtable_altar.json");
        JsonObject shippedInput = shipped.getAsJsonArray("input").get(0).getAsJsonObject();
        JsonObject built = ASRecipeJson.blockMatch(new JsonPrimitive("minecraft:crafting_table"));
        //The shipped entry also carries a "display" override, which the schema does not emit; the match half
        //is what has to agree.
        assertEquals(shippedInput.get("block"), built.get("block"));
        assertFalse(built.has("tag"));
    }

    @Test
    void anEmptyTagIsRejectedRatherThanWrittenOut() {
        //"#" alone would otherwise become {"tag":""}, which TagKey.create turns into an unresolvable tag and
        //BlockMatchInformation would silently never match anything.
        assertThrows(JsonSyntaxException.class, () -> ASRecipeJson.blockMatch(new JsonPrimitive("#")));
    }

    @Test
    void aNonStringBlockSpecIsRejected() {
        assertThrows(JsonSyntaxException.class, () -> ASRecipeJson.blockMatch(new JsonObject()));
        assertThrows(JsonSyntaxException.class, () -> ASRecipeJson.blockStateObject(new JsonPrimitive(7)));
    }

    @Test
    void theDropItemShorthandMatchesTheShippedInteractionRecipe() {
        JsonObject shipped = readShipped("/data/astralsorcery/recipes/liquid_interaction/liquidstarlight_lava_aquamarine.json");
        JsonObject shippedResult = shipped.getAsJsonObject("result");

        //The {item,count} object is what both RecipeJS#writeOutputItem and JsonHelper#serializeItemStack
        //produce, and what JsonHelper#getItemStack - i.e. ResultDropItem#read - takes back.
        JsonObject stack = new JsonObject();
        stack.addProperty("item", "astralsorcery:aquamarine_sand_ore");
        stack.addProperty("count", 1);

        assertEquals(shippedResult, ASRecipeJson.dropItemResult(stack));
    }

    @Test
    void theSpawnEntityFormUsesTheKeyResultSpawnEntityReads() {
        JsonObject out = ASRecipeJson.spawnEntityResult("minecraft:blaze");
        assertEquals(ASRecipeJson.RESULT_SPAWN_ENTITY, out.get("id").getAsString());
        assertEquals("minecraft:blaze", out.getAsJsonObject("data").get("entityType").getAsString());
    }

    @Test
    void validatingAShippedResultLeavesItUnchanged() {
        JsonObject shipped = readShipped("/data/astralsorcery/recipes/liquid_interaction/liquidstarlight_water_ice.json");
        JsonObject shippedResult = shipped.getAsJsonObject("result");
        assertEquals(shippedResult, ASRecipeJson.validateInteractionResult(shippedResult));
    }

    @Test
    void aMissingDataBlockIsFilledInRatherThanLeftOut() {
        //LiquidInteraction#read calls GsonHelper.getAsJsonObject(ctResult, "data") unconditionally, so a
        //result without it would throw at datapack load with no mention of the script that produced it.
        JsonObject input = new JsonObject();
        input.addProperty("id", ASRecipeJson.RESULT_DROP_ITEM);
        JsonObject out = ASRecipeJson.validateInteractionResult(input);
        assertTrue(out.has("data"));
        assertEquals(0, out.getAsJsonObject("data").size());
    }

    @Test
    void aResultWithoutAnIdIsRejected() {
        JsonObject noId = new JsonObject();
        noId.add("data", new JsonObject());
        assertThrows(JsonSyntaxException.class, () -> ASRecipeJson.validateInteractionResult(noId));
    }

    @Test
    void aResultWithAMalformedIdOrDataIsRejected() {
        JsonObject objectId = new JsonObject();
        objectId.add("id", new JsonObject());
        assertThrows(JsonSyntaxException.class, () -> ASRecipeJson.validateInteractionResult(objectId));

        JsonObject arrayData = new JsonObject();
        arrayData.addProperty("id", ASRecipeJson.RESULT_DROP_ITEM);
        arrayData.addProperty("data", "not an object");
        assertThrows(JsonSyntaxException.class, () -> ASRecipeJson.validateInteractionResult(arrayData));
    }

    private static JsonObject readShipped(String path) {
        try (InputStream in = ASRecipeJsonTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "Missing shipped recipe on the test classpath: " + path);
            return GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
        } catch (Exception e) {
            throw new AssertionError("Could not read " + path, e);
        }
    }
}
