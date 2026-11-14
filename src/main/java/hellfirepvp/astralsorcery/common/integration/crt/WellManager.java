/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.crt;

import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WellManager
 * Created by Jaredlll08
 * Date: 03.17.2021 / 15:36
 */
//BLOCKED_PORT[AS-INT-03]: 1.16.5 declared
//   @ZenRegister
//   @ZenCodeType.Name("mods.astralsorcery.WellManager")
//   public class WellManager implements IRecipeManager
// (1.16.5 WellManager.java:33-35). No CraftTweaker artifact is on the compile classpath, so
// com.blamejared.crafttweaker.* (CraftTweakerAPI, IRecipeManager, IIngredient, IItemStack,
// ActionAddRecipe, ActionRemoveRecipe, ExpandFluid) and org.openzen.zencode.java.ZenCodeType cannot
// be imported. Parameter degradation follows the rule documented on AltarManager: IIngredient becomes
// the Ingredient it unwraps to, IItemStack stays Object. @ZenCodeType.OptionalInt(0x00FF55FF) has no
// stand-in, so the default has to be supplied by the caller until the annotation is back.
//TODO_PORT[AS-PKG-01]: 1.16.5 took WellLiquefaction from common.crafting.recipe; this tree has it
// directly under common.crafting.
public class WellManager {

    /**
     * 1.16.5 signature:
     * void addRecipe(String name, Fluid output, IIngredient input, float productionMultiplier,
     *                float shatterMultiplier, @ZenCodeType.OptionalInt(0x00FF55FF) int color)
     */
    public void addRecipe(String name, Fluid output, Ingredient input, float productionMultiplier, float shatterMultiplier, int color) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5: name = fixRecipeName(name);
        ResourceLocation recipeId = new ResourceLocation(name);
        // 1.16.5: input.asVanillaIngredient()
        WellLiquefaction recipe = new WellLiquefaction(recipeId, input, output, new Color(color, true), productionMultiplier, shatterMultiplier);
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 (1.16.5 WellManager.java:42):
        //   CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe));
        // The recipe itself is built exactly as upstream (java.awt.Color is still what
        // WellLiquefaction takes in 1.20.1); only handing it to CraftTweaker is missing.
    }

    /**
     * 1.16.5 @Override of IRecipeManager#removeRecipe(IItemStack)
     */
    public void removeRecipe(Object output) {

        throw new UnsupportedOperationException("Cannot remove Astral Sorcery Well Liquefaction recipes by IItemStacks, use the Fluid method instead!");
    }

    /**
     * 1.16.5 signature: @ZenCodeType.Method void removeRecipe(Fluid output)
     */
    public void removeRecipe(Fluid output) {

        //BLOCKED_PORT[AS-INT-03]: 1.16.5 body (1.16.5 WellManager.java:54-62):
        //   CraftTweakerAPI.apply(new ActionRemoveRecipe(this, iRecipe -> {
        //
        //       if(iRecipe instanceof WellLiquefaction) {
        //           WellLiquefaction recipe = (WellLiquefaction) iRecipe;
        //
        //           return output == recipe.getFluidOutput();
        //       }
        //       return false;
        //   }, action -> "Removing Well Liquefaction recipes that output " + ExpandFluid.getCommandString(output)));
        // The predicate is portable as written - WellLiquefaction#getFluidOutput is ported - only
        // ActionRemoveRecipe and ExpandFluid.getCommandString are missing.
    }

    /**
     * 1.16.5 @Override of IRecipeManager#getRecipeType, which returned IRecipeType<WellLiquefaction>
     * via RecipeTypesAS.TYPE_WELL.getType().
     */
    public RecipeType<WellLiquefaction> getRecipeType() {

        return RecipeTypesAS.WELL.get();
    }

}
