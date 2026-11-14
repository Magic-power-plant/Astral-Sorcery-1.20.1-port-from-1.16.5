/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.kubejs;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.level.material.Fluid;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WellRecipeSchema
 * Created by HellFirePvP
 * Date: 27.09.2025 / 00:00
 */
//The lightwell schema, satisfying WellLiquefaction.Serializer.
//
//  event.recipes.astralsorcery.lightwell('minecraft:lava', 'minecraft:magma_block')
//      .productionMultiplier(0.7).shatterMultiplier(40.0).color(-51956)
//
//Registered under BOTH "astralsorcery:lightwell" and "astralsorcery:well", because
//common/lib/RecipeSerializersAS binds the same serializer to both ids. "lightwell" is the canonical one:
//it is the id all eleven shipped recipes carry and the only one 1.16.5 ever registered.
public interface WellRecipeSchema {

    //A bare fluid id string - NOT a KubeJS fluid component, which would write {"fluid":..,"amount":..} and
    //WellLiquefaction.Serializer#readFluid would then read the whole object as a string and yield EMPTY.
    RecipeKey<Fluid> OUTPUT = ASRecipeComponents.FLUID_ID.key("output");

    //Read with GsonHelper#getNonNull + Ingredient.fromJson, so the standard ingredient json is what is wanted.
    RecipeKey<InputItem> INPUT = ItemComponents.INPUT.key("input");

    //Not alwaysWrite: an absent colour means "no catalyst colour" (the field is @Nullable on the recipe),
    //which is a different thing from the colour 0, so the default must stay unwritten.
    RecipeKey<Integer> COLOR = NumberComponent.ANY_INT.key("color").alt("catalystColor").optional(0);

    //alwaysWrite: the registered serializer defaults these to 1.0F, but the reference port queued to replace
    //it (common/crafting/serializer/WellRecipeSerializer) reads them as mandatory.
    RecipeKey<Float> PRODUCTION_MULTIPLIER = NumberComponent.ANY_FLOAT.key("productionMultiplier").optional(1F).alwaysWrite();
    RecipeKey<Float> SHATTER_MULTIPLIER = NumberComponent.ANY_FLOAT.key("shatterMultiplier").optional(1F).alwaysWrite();

    static RecipeSchema create() {
        return new RecipeSchema(OUTPUT, INPUT, COLOR, PRODUCTION_MULTIPLIER, SHATTER_MULTIPLIER)
                .constructor(OUTPUT, INPUT)
                .constructor(OUTPUT, INPUT, COLOR)
                .constructor(OUTPUT, INPUT, COLOR, PRODUCTION_MULTIPLIER)
                .constructor(OUTPUT, INPUT, COLOR, PRODUCTION_MULTIPLIER, SHATTER_MULTIPLIER);
    }
}
