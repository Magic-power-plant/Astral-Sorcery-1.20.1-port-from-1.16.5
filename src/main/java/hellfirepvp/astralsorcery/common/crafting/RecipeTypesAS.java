/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeTypesAS
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:50
 */
//TODO_PORT[AS-PKG-01]/TODO_PORT[AS-ALT-04]: 1.16.5 keeps this at common/lib/RecipeTypesAS and declares the
//five fields as its own common/crafting/helper/ResolvingRecipeType, e.g.
//"ResolvingRecipeType<IItemHandler, SimpleAltarRecipe, SimpleAltarRecipeContext> TYPE_ALTAR;"
//(1.16.5 common/lib/RecipeTypesAS.java:27-31). ResolvingRecipeType wrapped vanilla's IRecipeType and added
//findMatchingRecipes(context)/findRecipe(context), which is how 1.16.5 resolved altar/well/infuser recipes.
//This tree instead registers plain vanilla RecipeTypes through a DeferredRegister and every call site
//(TileAltar, TileChalice, the crt/ and jei/ integrations, the journal pages) already resolves recipes through
//RecipeManager with these vanilla types. Converting the holder back to ResolvingRecipeType would require
//changing all of those call sites, which are outside this slice, so the design divergence is recorded here
//and common/crafting/helper/ResolvingRecipeType stays unused - see externalChangesNeeded.
//The registered type ids ("altar", "well", "infusion", "liquid_interaction", "block_transmutation") match the
//1.16.5 field names one-for-one.
public class RecipeTypesAS {

    private RecipeTypesAS() {}

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, AstralSorcery.MODID);

    public static final RegistryObject<RecipeType<AltarRecipe>> ALTAR = RECIPE_TYPES.register("altar", () -> registerRecipeType("altar"));
    public static final RegistryObject<RecipeType<WellLiquefaction>> WELL = RECIPE_TYPES.register("well", () -> registerRecipeType("well"));
    public static final RegistryObject<RecipeType<LiquidInfusion>> INFUSION = RECIPE_TYPES.register("infusion", () -> registerRecipeType("infusion"));
    public static final RegistryObject<RecipeType<LiquidInteraction>> LIQUID_INTERACTION = RECIPE_TYPES.register("liquid_interaction", () -> registerRecipeType("liquid_interaction"));
    public static final RegistryObject<RecipeType<BlockTransmutation>> BLOCK_TRANSMUTATION = RECIPE_TYPES.register("block_transmutation", () -> registerRecipeType("block_transmutation"));

    public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(String identifier) {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return AstralSorcery.MODID + ":" + identifier;
            }
        };
    }
}
