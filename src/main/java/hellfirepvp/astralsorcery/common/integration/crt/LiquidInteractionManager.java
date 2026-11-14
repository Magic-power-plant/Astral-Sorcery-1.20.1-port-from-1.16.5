/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.crt;

import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crafting.ResultDropItem;
import hellfirepvp.astralsorcery.common.crafting.ResultSpawnEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInteractionManager
 * Created by Jaredlll08
 * Date: 03.17.2021 / 15:36
 */
//BLOCKED_PORT[AS-INT-03]: 1.16.5 declared
//   @ZenRegister
//   @ZenCodeType.Name("mods.astralsorcery,LiquidInteractionManager")   // sic - comma, not a dot
//   public class LiquidInteractionManager implements IRecipeManager
// (1.16.5 LiquidInteractionManager.java:33-35). No CraftTweaker artifact is on the compile classpath,
// so com.blamejared.crafttweaker.* (CraftTweakerAPI, IRecipeManager, IItemStack, IFluidStack,
// MCEntityType, MCItemStackMutable, ActionAddRecipe, ActionRemoveRecipe) and
// org.openzen.zencode.java.ZenCodeType cannot be imported. Parameter degradation follows the rule
// documented on AltarManager: IFluidStack -> FluidStack and MCEntityType -> EntityType<?> because the
// reference only calls getInternal() on them (which also keeps the two removeRecipe overloads from
// collapsing into the same erasure), while IItemStack stays Object where IItemStack#matches is used.
//TODO_PORT[AS-PKG-01]: 1.16.5 took LiquidInteraction from common.crafting.recipe and
// InteractionResult/ResultDropItem/ResultSpawnEntity from common.crafting.recipe.interaction;
// this tree has all five directly under common.crafting.
public class LiquidInteractionManager {

    /**
     * 1.16.5 signature:
     * void addRecipe(String name, IItemStack output, IFluidStack reactant1, float chanceConsumeReactant1,
     *                IFluidStack reactant2, float chanceConsumeReactant2, int weight)
     */
    public void addRecipe(String name, ItemStack output, FluidStack reactant1, float chanceConsumeReactant1, FluidStack reactant2, float chanceConsumeReactant2, int weight) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5: name = fixRecipeName(name);
        ResourceLocation recipeId = new ResourceLocation(name);
        // 1.16.5: reactant1.getInternal() / reactant2.getInternal() / output.getInternal()
        LiquidInteraction recipe = new LiquidInteraction(recipeId, reactant1, chanceConsumeReactant1, reactant2, chanceConsumeReactant2, weight, ResultDropItem.dropItem(output));
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 (1.16.5 LiquidInteractionManager.java:42):
        //   CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, "Drop Item"));
        // The recipe itself is built exactly as upstream; only handing it to CraftTweaker is missing.
    }

    /**
     * 1.16.5 signature:
     * void addRecipe(String name, MCEntityType output, IFluidStack reactant1, float chanceConsumeReactant1,
     *                IFluidStack reactant2, float chanceConsumeReactant2, int weight)
     */
    public void addRecipe(String name, EntityType<?> output, FluidStack reactant1, float chanceConsumeReactant1, FluidStack reactant2, float chanceConsumeReactant2, int weight) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5: name = fixRecipeName(name);
        ResourceLocation recipeId = new ResourceLocation(name);
        // 1.16.5: ResultSpawnEntity.spawnEntity(output.getInternal())
        LiquidInteraction recipe = new LiquidInteraction(recipeId, reactant1, chanceConsumeReactant1, reactant2, chanceConsumeReactant2, weight, ResultSpawnEntity.spawnEntity(output));
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 (1.16.5 LiquidInteractionManager.java:50):
        //   CraftTweakerAPI.apply(new ActionAddRecipe(this, recipe, "Spawn Entity"));
    }

    /**
     * 1.16.5 @Override of IRecipeManager#removeRecipe(IItemStack)
     */
    public void removeRecipe(Object output) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 body (1.16.5 LiquidInteractionManager.java:55-63):
        //   CraftTweakerAPI.apply(new ActionRemoveRecipe(this, iRecipe -> {
        //       LiquidInteraction recipe = (LiquidInteraction) iRecipe;
        //       InteractionResult result = recipe.getResult();
        //       if(result instanceof ResultDropItem) {
        //           ResultDropItem resultDropItem = (ResultDropItem) result;
        //           return output.matches(new MCItemStackMutable(resultDropItem.getOutput()));
        //       }
        //       return false;
        //   }).describeDefaultRemoval(output));
        // Everything but the CraftTweaker halves is available: LiquidInteraction#getResult and
        // ResultDropItem#getOutput are both ported. IItemStack#matches has no vanilla equivalent, so
        // the parameter has to stay Object here.
    }

    /**
     * 1.16.5 signature: @ZenCodeType.Method void removeRecipe(MCEntityType entityType)
     */
    public void removeRecipe(EntityType<?> entityType) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 body (1.16.5 LiquidInteractionManager.java:68-76):
        //   CraftTweakerAPI.apply(new ActionRemoveRecipe(this, iRecipe -> {
        //       LiquidInteraction recipe = (LiquidInteraction) iRecipe;
        //       InteractionResult result = recipe.getResult();
        //       if(result instanceof ResultSpawnEntity) {
        //           ResultSpawnEntity resultSpawnEntity = (ResultSpawnEntity) result;
        //           return entityType.getInternal() == resultSpawnEntity.getEntityType();
        //       }
        //       return false;
        //   }).describeDefaultRemoval(entityType));
        // With the MCEntityType parameter degraded to EntityType<?> the predicate body would read
        // "entityType == resultSpawnEntity.getEntityType()"; only ActionRemoveRecipe is missing.
    }

    /**
     * 1.16.5 @Override of IRecipeManager#getRecipeType, which returned IRecipeType<LiquidInteraction>
     * via RecipeTypesAS.TYPE_LIQUID_INTERACTION.getType().
     */
    public RecipeType<LiquidInteraction> getRecipeType() {
        return RecipeTypesAS.LIQUID_INTERACTION.get();
    }
}
