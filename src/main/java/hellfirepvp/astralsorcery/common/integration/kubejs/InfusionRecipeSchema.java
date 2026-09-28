/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.kubejs;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.BooleanComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.level.material.Fluid;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: InfusionRecipeSchema
 * Created by HellFirePvP
 * Date: 27.09.2025 / 00:00
 */
//The starlight infuser schema, satisfying LiquidInfusion.Serializer -> LiquidInfusionSerializer.
//
//  event.recipes.astralsorcery.infuser(
//      Item.of('minecraft:netherite_scrap', 2), 'minecraft:ancient_debris',
//      'astralsorcery:liquid_starlight', 0.2, 120
//  ).consumeMultipleFluids(true)
//
//NOTE the id: the recipe TYPE is "astralsorcery:infusion", but the json "type" field names the recipe
//SERIALIZER, and common/crafting/RecipeSerializersAS registers that serializer as "astralsorcery:infuser"
//(the id all 26 shipped recipes carry). There is no "astralsorcery:infusion" serializer, so a schema
//registered under that id would resolve to no serializer at all - RecipeSchemaType#getSerializer looks the
//registered id up in the recipe-serializer registry and throws when it misses.
public interface InfusionRecipeSchema {

    RecipeKey<OutputItem> OUTPUT = ItemComponents.OUTPUT.key("output");
    RecipeKey<InputItem> INPUT = ItemComponents.INPUT.key("input");

    //A bare fluid id string; an unknown or empty fluid throws in the serializer.
    RecipeKey<Fluid> FLUID_INPUT = ASRecipeComponents.FLUID_ID.key("fluidInput");

    //Both mandatory in the serializer - GsonHelper#getAsFloat/getAsInt with no fallback - so they are
    //required here rather than defaulted.
    RecipeKey<Float> CONSUMPTION_CHANCE = NumberComponent.ANY_FLOAT.key("consumptionChance");
    RecipeKey<Integer> DURATION = NumberComponent.ANY_INT.key("duration");

    //The serializer's own fallbacks, restated. Left unwritten when the script does not set them, so the
    //fallback stays the single source of truth.
    RecipeKey<Boolean> CONSUME_MULTIPLE_FLUIDS = BooleanComponent.BOOLEAN.key("consumeMultipleFluids").optional(false);
    RecipeKey<Boolean> ACCEPT_CHALICE_INPUT = BooleanComponent.BOOLEAN.key("acceptChaliceInput").optional(true);
    RecipeKey<Boolean> COPY_NBT_TO_OUTPUTS = BooleanComponent.BOOLEAN.key("copyNBTToOutputs").optional(false);

    RecipeSchema SCHEMA = new RecipeSchema(
            OUTPUT, INPUT, FLUID_INPUT, CONSUMPTION_CHANCE, DURATION,
            CONSUME_MULTIPLE_FLUIDS, ACCEPT_CHALICE_INPUT, COPY_NBT_TO_OUTPUTS)
            .constructor(OUTPUT, INPUT, FLUID_INPUT, CONSUMPTION_CHANCE, DURATION)
            .uniqueOutputId(OUTPUT);
}
