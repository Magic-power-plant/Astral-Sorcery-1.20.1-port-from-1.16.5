/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.crt;

import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: InfusionManager
 * Created by Jaredlll08
 * Date: 03.17.2021 / 15:36
 */
//BLOCKED_PORT[AS-INT-03]: 1.16.5 declared
//   @ZenRegister
//   @ZenCodeType.Name("mods.astralsorcery.InfusionManager")
//   public class InfusionManager implements IRecipeManager
// (1.16.5 InfusionManager.java:32-34). No CraftTweaker artifact is on the compile classpath, so
// com.blamejared.crafttweaker.* (CraftTweakerAPI, IRecipeManager, IIngredient, IItemStack,
// MCItemStackMutable, ActionAddRecipe, ActionRemoveRecipe) and org.openzen.zencode.java.ZenCodeType
// cannot be imported. Parameter degradation follows the rule documented on AltarManager: IItemStack
// becomes ItemStack and IIngredient becomes Ingredient where the reference only unwraps them, and
// stays Object where an IItemStack-only method (matches) is called.
// 1.20.1 note: net.minecraft.fluid.Fluid -> net.minecraft.world.level.material.Fluid.
//TODO_PORT[AS-PKG-01]: 1.16.5 took LiquidInfusion from common.crafting.recipe; this tree has it
// directly under common.crafting.
public class InfusionManager {

    /**
     * 1.16.5 signature:
     * void addRecipe(String name, IItemStack itemOutput, IIngredient itemInput, Fluid liquidInput,
     *                int craftingTickTime, float consumptionChance, boolean consumeMultipleFluids,
     *                boolean acceptChaliceInput, boolean copyNBTToOutputs)
     */
    public void addRecipe(String name, ItemStack itemOutput, Ingredient itemInput, Fluid liquidInput, int craftingTickTime, float consumptionChance, boolean consumeMultipleFluids, boolean acceptChaliceInput, boolean copyNBTToOutputs) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5: name = fixRecipeName(name);
        ResourceLocation recipeId = new ResourceLocation(name);
        // 1.16.5: itemInput.asVanillaIngredient() / itemOutput.getInternal()
        LiquidInfusion recipe = new LiquidInfusion(recipeId, craftingTickTime, liquidInput, itemInput, itemOutput, consumptionChance, consumeMultipleFluids, acceptChaliceInput, copyNBTToOutputs);
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 (1.16.5 InfusionManager.java:41):
        //   CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe));
        // The recipe itself is built exactly as upstream; only handing it to CraftTweaker is missing.
    }

    /**
     * 1.16.5 @Override of IRecipeManager#removeRecipe(IItemStack)
     */
    public void removeRecipe(Object output) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 body (1.16.5 InfusionManager.java:46-53):
        //   CraftTweakerAPI.apply(new ActionRemoveRecipe(this, iRecipe -> {
        //
        //       if(iRecipe instanceof LiquidInfusion) {
        //           LiquidInfusion recipe = (LiquidInfusion) iRecipe;
        //           return output.matches(new MCItemStackMutable(recipe.getOutput(ItemStack.EMPTY)));
        //       }
        //       return false;
        //   }, action -> "Removing \"" + action.getRecipeTypeName() + "\" recipes with output: " + output + "\""));
        // LiquidInfusion#getOutput(ItemStack) is ported; IItemStack#matches and MCItemStackMutable
        // have no vanilla equivalent, which is why the parameter has to stay Object here.
    }

    /**
     * 1.16.5 @Override of IRecipeManager#getRecipeType, which returned IRecipeType<LiquidInfusion>
     * via RecipeTypesAS.TYPE_INFUSION.getType().
     */
    public RecipeType<LiquidInfusion> getRecipeType() {
        return RecipeTypesAS.INFUSION.get();
    }
}
