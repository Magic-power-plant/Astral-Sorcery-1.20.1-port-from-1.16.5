/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.level.material.Fluid;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInteractionRecipeSchema
 * Created by HellFirePvP
 * Date: 27.09.2025 / 00:00
 */
//The liquid interaction schema, satisfying LiquidInteraction.Serializer -> LiquidInteraction#read(json).
//
//  event.recipes.astralsorcery.liquid_interaction(
//      'astralsorcery:aquamarine_sand_ore',
//      'astralsorcery:liquid_starlight', 10,
//      'minecraft:lava', 10
//  ).chanceConsumeReactant1(0.5).chanceConsumeReactant2(0.5).weight(1)
//
//The bare item in the first slot is the shorthand for the drop_item result; the long form is
//  .result({ id: 'astralsorcery:spawn_entity', data: { entityType: 'minecraft:blaze' } })
public interface LiquidInteractionRecipeSchema {

    RecipeKey<JsonObject> RESULT = ASRecipeComponents.INTERACTION_RESULT.key("result");

    //Bare fluid id strings with the amounts in separate int fields - LiquidInteraction#read assembles the
    //two FluidStacks out of that pair, which is why a KubeJS fluid component cannot be used here.
    RecipeKey<Fluid> REACTANT_1 = ASRecipeComponents.FLUID_ID.key("reactant1");
    RecipeKey<Integer> REACTANT_1_AMOUNT = NumberComponent.ANY_INT.key("reactant1Amount");
    RecipeKey<Fluid> REACTANT_2 = ASRecipeComponents.FLUID_ID.key("reactant2");
    RecipeKey<Integer> REACTANT_2_AMOUNT = NumberComponent.ANY_INT.key("reactant2Amount");

    //All three are read as mandatory by LiquidInteraction#read, so they carry alwaysWrite: a script may omit
    //them and still get valid json, because the default is written out rather than left to the serializer.
    RecipeKey<Float> CHANCE_CONSUME_REACTANT_1 = NumberComponent.ANY_FLOAT.key("chanceConsumeReactant1").optional(1F).alwaysWrite();
    RecipeKey<Float> CHANCE_CONSUME_REACTANT_2 = NumberComponent.ANY_FLOAT.key("chanceConsumeReactant2").optional(1F).alwaysWrite();
    RecipeKey<Integer> WEIGHT = NumberComponent.ANY_INT.key("weight").optional(1).alwaysWrite();

    //"reactant1Tag"/"reactant2Tag" are deliberately left out. LiquidInteraction#read parses them with
    //TagParser#parseTag, i.e. they are SNBT strings describing the FluidStack's nbt, and no shipped recipe
    //and neither crt manager ever sets one. A script that needs a tagged reactant can use event.custom({..}).

    RecipeSchema SCHEMA = new RecipeSchema(
            RESULT, REACTANT_1, REACTANT_1_AMOUNT, REACTANT_2, REACTANT_2_AMOUNT,
            CHANCE_CONSUME_REACTANT_1, CHANCE_CONSUME_REACTANT_2, WEIGHT)
            .constructor(RESULT, REACTANT_1, REACTANT_1_AMOUNT, REACTANT_2, REACTANT_2_AMOUNT)
            .constructor(RESULT, REACTANT_1, REACTANT_1_AMOUNT, REACTANT_2, REACTANT_2_AMOUNT,
                    CHANCE_CONSUME_REACTANT_1, CHANCE_CONSUME_REACTANT_2)
            .constructor(RESULT, REACTANT_1, REACTANT_1_AMOUNT, REACTANT_2, REACTANT_2_AMOUNT,
                    CHANCE_CONSUME_REACTANT_1, CHANCE_CONSUME_REACTANT_2, WEIGHT);
}
