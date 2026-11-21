/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleAltarRecipeSerializer
 * Created by HellFirePvP
 * Date: 12.08.2019 / 19:45
 */
public class SimpleAltarRecipeSerializer extends CustomRecipeSerializer<SimpleAltarRecipe> {

    //1.16.5 held the single instance in common/lib/RecipeSerializersAS as ALTAR_RECIPE_SERIALIZER and
    //registered it from RegistryRecipeSerializers. common/lib is not owned by this slice and this tree's
    //common/lib/RecipeSerializersAS already binds the "astralsorcery:altar" serializer id to the
    //divergent AltarRecipeSerializer, so the instance lives here and is not registered yet.
    //TODO_PORT[AS-ALT-05]: move to common/lib/RecipeSerializersAS#ALTAR_RECIPE_SERIALIZER and register it
    // once the divergent common/crafting/AltarRecipe is retired in favour of SimpleAltarRecipe.
    public static final SimpleAltarRecipeSerializer INSTANCE = new SimpleAltarRecipeSerializer();

    public SimpleAltarRecipeSerializer() {
        super(RecipeSerializersAS.ALTAR.getId());
    }

    @Override
    public SimpleAltarRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        int typeId = GsonHelper.getAsInt(json, "altar_type");
        AltarType type = MiscUtils.getEnumEntry(AltarType.class, typeId);
        int duration = GsonHelper.getAsInt(json, "duration");
        int starlightRequirement = GsonHelper.getAsInt(json, "starlight");

        AltarRecipeGrid grid = AltarRecipeGrid.deserialize(type, json);
        grid.validate(type);

        SimpleAltarRecipe recipe = new SimpleAltarRecipe(recipeId, type, duration, starlightRequirement, grid);
        if (GsonHelper.isValidNode(json, "recipe_class")) {
            ResourceLocation key = parseId(GsonHelper.getAsString(json, "recipe_class"));
            //TODO_PORT[AS-ALT-03]: 1.16.5 also swapped the recipe instance here
            // (1.16.5 common/crafting/serializer/SimpleAltarRecipeSerializer.java:58):
            //   recipe = AltarRecipeTypeHandler.convert(recipe, key);
            // See SimpleAltarRecipe#read for why this tree's AltarRecipeTypeHandler cannot convert a
            // SimpleAltarRecipe: its converters are all typed against the divergent common.crafting.AltarRecipe.
            recipe.setCustomRecipeType(key);
        }

        if (GsonHelper.isArrayNode(json, "output")) {
            JsonArray outputArray = GsonHelper.getAsJsonArray(json, "output");
            for (int i = 0; i < outputArray.size(); i++) {
                recipe.addOutput(JsonHelper.getItemStack(outputArray.get(i), String.format("output[%s]", i)));
            }
        } else {
            recipe.addOutput(JsonHelper.getItemStack(json, "output"));
        }

        JsonObject recipeOptions = new JsonObject();
        if (GsonHelper.isValidNode(json, "options")) {
            recipeOptions = GsonHelper.getAsJsonObject(json, "options");
        }
        recipe.deserializeAdditionalJson(recipeOptions);

        if (GsonHelper.isValidNode(json, "focus_constellation")) {
            ResourceLocation key = parseId(GsonHelper.getAsString(json, "focus_constellation"));
            IConstellation cst = ConstellationRegistry.getConstellation(key);
            if (cst == null) {
                throw new JsonSyntaxException("Unknown constellation " + key.toString());
            }
            recipe.setFocusConstellation(cst);
        }

        if (GsonHelper.isValidNode(json, "relay_inputs")) {
            JsonArray relayIngredients = GsonHelper.getAsJsonArray(json, "relay_inputs");
            for (int i = 0; i < relayIngredients.size(); i++) {
                JsonElement element = relayIngredients.get(i);
                //1.16.5: Ingredient.deserialize(JsonElement) / Ingredient#hasNoMatchingItems()
                //1.20.1: Ingredient.fromJson(JsonElement) / Ingredient#isEmpty()
                Ingredient ingredient = Ingredient.fromJson(element);
                if (!ingredient.isEmpty()) {
                    recipe.addRelayInput(ingredient);
                } else {
                    AstralSorcery.LOG.warn("Skipping relay_inputs[" + i + "] for recipe " + recipeId + " as the ingredient has no matching items!");
                    //1.16.5: JSONUtils.toString(JsonElement) - renamed to GsonHelper.getType(JsonElement)
                    AstralSorcery.LOG.warn("Ingredient skipped: " + GsonHelper.getType(element));
                }
            }
        }

        if (GsonHelper.isValidNode(json, "effects")) {
            JsonArray effectNames = GsonHelper.getAsJsonArray(json, "effects");
            for (int i = 0; i < effectNames.size(); i++) {
                JsonElement element = effectNames.get(i);
                ResourceLocation effectKey = parseId(GsonHelper.convertToString(element, "effects[" + i + "]"));
                AltarRecipeEffect effect = RegistriesAS.ALTAR_RECIPE_EFFECTS.getValue(effectKey);
                if (effect == null) {
                    throw new JsonSyntaxException("No altar effect for name " + effectKey + "! (Found at: effects[" + i + "])");
                }
                recipe.addAltarEffect(effect);
            }
        }

        return recipe;
    }

    @Override
    public SimpleAltarRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return SimpleAltarRecipe.read(recipeId, buffer);
    }

    @Override
    public void write(JsonObject object, SimpleAltarRecipe recipe) {
        recipe.write(object);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, SimpleAltarRecipe recipe) {
        recipe.write(buffer);
    }

    //1.16.5 used 'new ResourceLocation(String)', which threw a ResourceLocationException on a malformed id.
    //The single-argument constructor is deprecated in this Forge version; ResourceLocation.parse is the
    //replacement and throws the very same ResourceLocationException, so the failure mode is preserved.
    private static ResourceLocation parseId(String key) {
        return ResourceLocation.parse(key);
    }
}
