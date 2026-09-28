/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.MapRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.util.TinyMap;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarRecipeSchema
 * Created by HellFirePvP
 * Date: 27.09.2025 / 00:00
 */
//The "astralsorcery:altar" schema. The serializer this has to satisfy is common/crafting/AltarRecipeSerializer
//(NOT common/crafting/serializer/SimpleAltarRecipeSerializer, which is the reference-faithful port and is
//still unregistered - see TODO_PORT[AS-ALT-05] there).
//
//  event.recipes.astralsorcery.altar(
//      [Item.of('astralsorcery:marble_pillar', 2)],
//      ['_____', '__A__', '__A__', '_____', '_____'],
//      { A: 'astralsorcery:marble_raw' }
//  ).starlight(100).duration(40).effects(['astralsorcery:built_in_effect_discovery_central_beam'])
//
//Not exposed, deliberately: Astral Sorcery's own ingredient types. Four of the shipped altar recipes key on
//{"type":"astralsorcery:crystal", ..} or {"type":"astralsorcery:fluid", ..}, which AltarRecipeGrid
//#deserializeIngredient dispatches on. KubeJS' InputItem cannot carry those - it wraps a vanilla Ingredient -
//so a script that needs one has to fall back to event.custom({..}) with the raw json. Emitting a broken
//approximation instead would be worse.
public interface AltarRecipeSchema {

    //JsonHelper#getItemStack accepts both a single stack and an array, and AltarRecipeSerializer#readOutputs
    //branches on that. asArray() (rather than asArrayOrSelf()) always writes the array form, which is what
    //all 117 shipped altar recipes use.
    RecipeKey<OutputItem[]> OUTPUT = ItemComponents.OUTPUT.asArray().key("output");

    //AltarRecipeGrid#deserialize reads "pattern" with GsonHelper#getAsJsonArray, so it must stay an array.
    RecipeKey<String[]> PATTERN = StringComponent.NON_EMPTY.asArray().key("pattern");

    //Exactly the component vanilla's own shaped schema uses for its pattern key, so '#tag', 'item:id',
    //Item.of(..) and an ingredient array all work per entry.
    RecipeKey<TinyMap<Character, InputItem>> KEY = MapRecipeComponent.ITEM_PATTERN_KEY.key("key");

    //alwaysWrite: the registered serializer defaults both of these, but the reference serializer that is
    //queued to replace it makes them mandatory. Writing the default keeps script-built json valid either way.
    RecipeKey<Integer> ALTAR_TYPE = ASRecipeComponents.ALTAR_TYPE.key("altar_type").alt("altarType").optional(0).alwaysWrite();
    RecipeKey<Integer> STARLIGHT = NumberComponent.ANY_INT.key("starlight").alt("starlightRequired").optional(0).alwaysWrite();

    //NOT alwaysWrite. When "duration" is absent AltarRecipeSerializer falls back to
    //altarType.getDefaultAltarCraftingDuration() - 100/200/400/600 per tier - which is a better default than
    //any constant a RecipeOptional could supply, because RecipeOptional only sees the schema, not the recipe.
    RecipeKey<Integer> DURATION = NumberComponent.ANY_INT.key("duration").alt("craftingTime").optional(0);

    RecipeKey<String[]> EFFECTS = StringComponent.ID.asArray().key("effects").optional(new String[0]);

    //Read back through WrappedIngredient#fromJson, which falls through to Ingredient.fromJson for everything
    //that is not an astralsorcery fluid ingredient - so a plain item/tag ingredient array is accepted.
    RecipeKey<InputItem[]> RELAY_INPUTS = ItemComponents.INPUT.asArray().key("relay_inputs").optional(new InputItem[0]);

    RecipeKey<String> FOCUS_CONSTELLATION = StringComponent.ID.key("focus_constellation").optional("");

    //41 of the shipped altar recipes name a recipe class; the ids are the keys of
    //common/crafting/AltarRecipeTypeHandler (astralsorcery:attunement_upgrade, astralsorcery:nbt_copy, ..).
    //An unknown id is NOT an error there - convert() falls back to the plain AltarRecipe - so a typo
    //degrades to a normal recipe rather than failing the datapack.
    RecipeKey<String> RECIPE_CLASS = StringComponent.ID.key("recipe_class").optional("");

    //"options" is only read by AltarRecipe#deserializeAdditionalJson, which each recipe_class overrides, so
    //its shape belongs to the recipe class and not to the altar format. Passed through verbatim.
    RecipeKey<JsonObject> OPTIONS = ASRecipeComponents.RAW_JSON_OBJECT.key("options").optional(new JsonObject());

    RecipeSchema SCHEMA = new RecipeSchema(
            OUTPUT, PATTERN, KEY, ALTAR_TYPE, STARLIGHT, DURATION,
            EFFECTS, RELAY_INPUTS, FOCUS_CONSTELLATION, RECIPE_CLASS, OPTIONS)
            .constructor(OUTPUT, PATTERN, KEY)
            .constructor(OUTPUT, PATTERN, KEY, ALTAR_TYPE)
            .constructor(OUTPUT, PATTERN, KEY, ALTAR_TYPE, STARLIGHT)
            .constructor(OUTPUT, PATTERN, KEY, ALTAR_TYPE, STARLIGHT, DURATION)
            .uniqueOutputArrayId(OUTPUT);
}
