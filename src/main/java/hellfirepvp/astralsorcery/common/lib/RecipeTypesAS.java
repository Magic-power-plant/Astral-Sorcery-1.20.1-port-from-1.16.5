/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;

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
public final class RecipeTypesAS {

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
