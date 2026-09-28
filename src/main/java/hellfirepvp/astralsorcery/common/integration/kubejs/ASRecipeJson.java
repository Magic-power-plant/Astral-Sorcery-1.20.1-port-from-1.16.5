/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ASRecipeJson
 * Created by HellFirePvP
 * Date: 27.09.2025 / 00:00
 */
//The json-shape half of the KubeJS integration, deliberately split out of ASRecipeComponents.
//
//Nothing in here imports dev.latvian.mods.** or net.minecraft.**. That is load-bearing twice over:
// - kubejs-forge is a compileOnly dependency, so it is NOT on testCompileClasspath. A test that touched a
//   KubeJS type would not compile, and a test that merely loaded ASRecipeComponents would initialise
//   RegistryInfo.FLUID and therefore BuiltInRegistries, which this harness cannot bootstrap.
// - Keeping the mapping pure makes the one thing that can silently produce json the shipped serializers
//   reject - the field layout - directly unit-testable. See
//   src/test/java/hellfirepvp/astralsorcery/common/integration/kubejs/ASRecipeJsonTest.java.
//
//Every method below emits exactly the shape the REGISTERED serializer reads. The registered serializer is
//not always the one under common/crafting/serializer/ - see IntegrationKubeJS for the id map.
public final class ASRecipeJson {

    //common/crafting/InteractionResultRegistry registers exactly these two result ids.
    public static final String RESULT_DROP_ITEM = "astralsorcery:drop_item";
    public static final String RESULT_SPAWN_ENTITY = "astralsorcery:spawn_entity";

    private ASRecipeJson() {}

    /**
     * Wraps a bare block id into the object common/util/block/BlockStateHelper#deserializeObject expects,
     * which is what BlockTransmutationSerializer reads out of "output"
     * (common/crafting/serializer/BlockTransmutationSerializer.java:72).
     * <p>
     * "properties" is intentionally not emitted. BlockStateHelper#isMissingStateInformation(JsonObject)
     * returns {@code serialized.has("properties")} - inverted - so deserializeObject short-circuits and
     * ignores the property list entirely. That is verbatim 1.16.5 behaviour, not a port regression, so a
     * schema that emitted "properties" would be writing a field that is guaranteed to be discarded while
     * also flipping the match to non-exact.
     */
    public static JsonObject blockStateObject(JsonElement blockId) {
        JsonObject out = new JsonObject();
        out.add("block", requireString(blockId, "block id"));
        return out;
    }

    /**
     * Maps one block-transmutation input spec onto a BlockMatchInformation object
     * (common/util/block/BlockMatchInformation#read). A leading '#' selects the block-tag form, which is
     * the same convention vanilla and KubeJS use everywhere else for "tag rather than id".
     */
    public static JsonObject blockMatch(JsonElement spec) {
        String raw = requireString(spec, "block match").getAsString();
        JsonObject out = new JsonObject();
        if (raw.startsWith("#")) {
            String tag = raw.substring(1);
            if (tag.isEmpty()) {
                throw new JsonSyntaxException("Block transmutation input '#' has no tag after the '#'");
            }
            out.addProperty("tag", tag);
        } else {
            out.addProperty("block", raw);
        }
        return out;
    }

    /**
     * Builds the drop_item form of a liquid-interaction result around an already-serialised item stack.
     * common/crafting/ResultDropItem#read reads the stack back out of "data"/"output" through
     * JsonHelper#getItemStack, which accepts both the string and the {item,count,nbt} object form.
     */
    public static JsonObject dropItemResult(JsonElement outputStack) {
        JsonObject data = new JsonObject();
        data.add("output", outputStack);
        return interactionResult(RESULT_DROP_ITEM, data);
    }

    /**
     * Builds the spawn_entity form. common/crafting/ResultSpawnEntity#read wants a plain entity-type id
     * under "data"/"entityType".
     */
    public static JsonObject spawnEntityResult(String entityTypeId) {
        JsonObject data = new JsonObject();
        data.addProperty("entityType", entityTypeId);
        return interactionResult(RESULT_SPAWN_ENTITY, data);
    }

    public static JsonObject interactionResult(String id, JsonObject data) {
        JsonObject out = new JsonObject();
        out.addProperty("id", id);
        out.add("data", data);
        return out;
    }

    /**
     * Checks that a script-supplied liquid-interaction result already has the {"id":..,"data":{..}} shape
     * common/crafting/LiquidInteraction#read(ResourceLocation, JsonObject) demands, and fills in an empty
     * "data" object when it is missing. Failing here produces a message naming the script's own field;
     * letting it through would instead surface as a datapack parse error with no link back to the script.
     */
    public static JsonObject validateInteractionResult(JsonObject result) {
        if (!result.has("id")) {
            throw new JsonSyntaxException("A liquid interaction result needs an 'id' - one of "
                    + RESULT_DROP_ITEM + ", " + RESULT_SPAWN_ENTITY);
        }
        JsonElement id = result.get("id");
        if (!id.isJsonPrimitive() || !id.getAsJsonPrimitive().isString()) {
            throw new JsonSyntaxException("A liquid interaction result 'id' has to be a string, got: " + id);
        }
        JsonObject out = new JsonObject();
        out.add("id", id);
        JsonElement data = result.get("data");
        if (data == null || data.isJsonNull()) {
            out.add("data", new JsonObject());
        } else if (data.isJsonObject()) {
            out.add("data", data);
        } else {
            throw new JsonSyntaxException("A liquid interaction result 'data' has to be an object, got: " + data);
        }
        return out;
    }

    private static JsonElement requireString(JsonElement element, String what) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            throw new JsonSyntaxException("Expected " + what + " to be a string, got: " + element);
        }
        return element;
    }
}
