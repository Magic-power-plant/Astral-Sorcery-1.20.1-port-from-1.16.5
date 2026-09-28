/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei;

//1.16.5 imported hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction and
//hellfirepvp.astralsorcery.common.crafting.recipe.interaction.{InteractionResult, ResultDropItem}; this tree
//keeps all three as top-level classes in common/crafting (tracked as TODO_PORT[AS-PKG-01]).
import hellfirepvp.astralsorcery.common.crafting.InteractionResult;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.ResultDropItem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIHandlerDropItem
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:50
 */
public class JEIHandlerDropItem extends JEIInteractionResultHandler {

    //1.16.5 split this across two methods
    //(1.16.5 common/crafting/recipe/interaction/jei/JEIHandlerDropItem.java:35-56):
    //  public void addToRecipeLayout(IRecipeLayout recipeLayout, LiquidInteraction recipe, IIngredients ingredients) {
    //      IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
    //      itemStacks.init(2, false, 47, 18);
    //      itemStacks.set(ingredients);
    //  }
    //  public void addToRecipeIngredients(LiquidInteraction recipe, IIngredients ingredients) {
    //      ImmutableList.Builder<List<ItemStack>> itemOutputs = ImmutableList.builder();
    //      InteractionResult result = recipe.getResult();
    //      if (result instanceof ResultDropItem) {
    //          itemOutputs.add(Lists.newArrayList(((ResultDropItem) result).getOutput()));
    //      }
    //      ingredients.setOutputLists(VanillaTypes.ITEM, itemOutputs.build());
    //  }
    //JEI 15 declares position and contents on the same slot, so they are one method here; the slot moves from
    //(47, 18) to (48, 19) for the reason spelled out on common/integration/jei/JEICategory#initFluidInput
    //(JEI 15 positions a slot by its 16x16 ingredient area, 1.16.5 by its 18x18 frame). Note that 1.16.5 set
    //the output list unconditionally - an empty list when the result is not a ResultDropItem - while the slot
    //was created unconditionally too; skipping the slot entirely in that case is equivalent and avoids an
    //empty output slot, and in practice JEIInteractionResultRegistry only routes drop_item results here.
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addToRecipeLayout(IRecipeLayoutBuilder builder, LiquidInteraction recipe) {
        InteractionResult result = recipe.getResult();
        if (result instanceof ResultDropItem dropItem) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 48, 19)
                    .addItemStack(dropItem.getOutput());
        }
    }

    //1.16.5 signature: drawRecipe(LiquidInteraction recipe, MatrixStack renderStack, double mouseX,
    //double mouseY) (1.16.5 .../JEIHandlerDropItem.java:58-61). The 1.16.5 body is empty; nothing is lost.
    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawRecipe(LiquidInteraction recipe, GuiGraphics guiGraphics, double mouseX, double mouseY) {
    }
}
