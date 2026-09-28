/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTransmutationRecipeSchema
 * Created by HellFirePvP
 * Date: 27.09.2025 / 00:00
 */
//The block transmutation schema, satisfying BlockTransmutation.Serializer -> BlockTransmutationSerializer.
//
//  event.recipes.astralsorcery.block_transmutation(
//      'astralsorcery:starmetal_ore', ['#forge:ores/iron'], 100.0
//  ).display('astralsorcery:starmetal_ore')
//
//  event.recipes.astralsorcery.block_transmutation(
//      'astralsorcery:altar_discovery', ['minecraft:crafting_table'], 60.0
//  ).constellation('astralsorcery:discidia')
public interface BlockTransmutationRecipeSchema {

    //Written as {"block": "<id>"}, which is the object BlockStateHelper#deserializeObject reads. Typed as the
    //json object rather than as a Block so that a shipped recipe's output survives a round trip - see
    //ASRecipeComponents.BLOCK_STATE_OBJECT#normalize.
    RecipeKey<JsonObject> OUTPUT = ASRecipeComponents.BLOCK_STATE_OBJECT.key("output");

    //Each entry is a BlockMatchInformation object; a script may write "<block id>" or "#<block tag>" and it
    //is wrapped. JsonHelper#parseMultipleJsonObjects takes a single object or an array; asArray() always
    //emits the array form, which is what every shipped recipe uses.
    //The serializer throws when the list ends up empty.
    RecipeKey<JsonObject[]> INPUT = ASRecipeComponents.BLOCK_MATCH.asArray().key("input");

    //GsonHelper#getAsFloat with no fallback, so mandatory.
    RecipeKey<Float> STARLIGHT = NumberComponent.ANY_FLOAT.key("starlight");

    //Defaults to new ItemStack(output.getBlock()) in the serializer when absent. allowEmpty() is needed
    //because ItemComponents.OUTPUT#checkEmpty rejects the empty stack this optional default holds.
    RecipeKey<OutputItem> DISPLAY = ItemComponents.OUTPUT.key("display").optional(OutputItem.EMPTY).allowEmpty();

    //Optional; must name a major or dim constellation, the serializer throws on anything else.
    RecipeKey<String> CONSTELLATION = StringComponent.ID.key("constellation").optional("");

    //"properties" on the output blockstate is deliberately unreachable - see ASRecipeJson#blockStateObject
    //for why emitting it would be a no-op that also weakens the match.

    RecipeSchema SCHEMA = new RecipeSchema(OUTPUT, INPUT, STARLIGHT, DISPLAY, CONSTELLATION)
            .constructor(OUTPUT, INPUT, STARLIGHT)
            .constructor(OUTPUT, INPUT, STARLIGHT, DISPLAY)
            .constructor(OUTPUT, INPUT, STARLIGHT, DISPLAY, CONSTELLATION);
}
