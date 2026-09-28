/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei;

//1.16.5 imported hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInteraction; this tree keeps the
//recipe as a top-level class in common/crafting (tracked as TODO_PORT[AS-PKG-01]).
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIInteractionResultHandler
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:47
 */
//1.16.5 declared three methods (1.16.5 common/crafting/recipe/interaction/jei/JEIInteractionResultHandler.java:28-35):
//  addToRecipeLayout(mezz.jei.api.gui.IRecipeLayout, LiquidInteraction, mezz.jei.api.ingredients.IIngredients)
//  addToRecipeIngredients(LiquidInteraction, mezz.jei.api.ingredients.IIngredients)
//  drawRecipe(LiquidInteraction, com.mojang.blaze3d.matrix.MatrixStack, double, double)
//JEI 15 deleted IRecipeLayout and IIngredients: a slot's position and its ingredient list are both declared on
//an IRecipeLayoutBuilder, which is what removed the reason for splitting layout from ingredients. The two
//addTo* methods therefore collapse into one, and drawRecipe takes the GuiGraphics that JEI 15 hands a category
//(GuiGraphics#pose() is the 1.16.5 MatrixStack) rather than the MatrixStack itself.
//The @OnlyIn(Dist.CLIENT) on these abstract members propagates to every implementor, which is intended: both
//parameter types are client/JEI-only and the sole caller is common/integration/jei/CategoryLiquidInteraction.
public abstract class JEIInteractionResultHandler {

    @OnlyIn(Dist.CLIENT)
    public abstract void addToRecipeLayout(IRecipeLayoutBuilder builder, LiquidInteraction recipe);

    @OnlyIn(Dist.CLIENT)
    public abstract void drawRecipe(LiquidInteraction recipe, GuiGraphics guiGraphics, double mouseX, double mouseY);

}
