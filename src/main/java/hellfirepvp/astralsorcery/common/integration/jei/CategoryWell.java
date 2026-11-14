/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryInfuser
 * Created by HellFirePvP
 * Date: 05.09.2020 / 12:37
 */
@OnlyIn(Dist.CLIENT)
public class CategoryWell extends JEICategory<WellLiquefaction> {

    private final IDrawable background, icon;
    private final ItemStack catalyst;

    public CategoryWell(IGuiHelper guiHelper) {
        super(IntegrationJEI.CATEGORY_WELL);
        this.background = guiHelper.createDrawable(AstralSorcery.key("textures/gui/jei/lightwell.png"), 0, 0, 116, 54);
        // 1.16.5: new ItemStack(BlocksAS.WELL) - a RegistryObject here - and createDrawableIngredient,
        // which JEI 15 renamed to createDrawableItemStack.
        this.catalyst = new ItemStack(BlocksAS.WELL.get());
        this.icon = guiHelper.createDrawableItemStack(this.catalyst);
    }

    //IRecipeCategory#getBackground() is @Deprecated(forRemoval = true) in JEI 15.62: JEI 19 replaced it with
    //getWidth()/getHeight() plus drawing the background yourself. It is still the abstract-free, supported way
    //to declare a fixed-size custom background on this JEI line and is what sizes the category, so it is kept
    //and the removal warning suppressed rather than pre-emptively reworking the five layouts.
    @SuppressWarnings("removal")
    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public ItemStack getCatalyst() {
        return this.catalyst;
    }

    /**
     * 1.16.5 signature: void draw(WellLiquefaction, MatrixStack, double, double)
     * JEI 15 passes a GuiGraphics (plus an IRecipeSlotsView) instead of the MatrixStack, and IDrawable#draw
     * takes that GuiGraphics.
     */
    @Override
    public void draw(WellLiquefaction recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.icon.draw(guiGraphics, 46, 20);
    }

    @Override
    public List<WellLiquefaction> getRecipes() {
        // 1.16.5: return RecipeTypesAS.TYPE_WELL.getAllRecipes();
        // This tree lists recipes of a type through the RecipeManager; the copy keeps the result
        // mutable because IntegrationJEI#registerRecipes sorts it.
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mgr.getAllRecipesFor(RecipeTypesAS.WELL.get()));
    }

    /**
     * 1.16.5 signature: void setRecipe(IRecipeLayout, WellLiquefaction, IIngredients), fed by a separate
     * setIngredients(WellLiquefaction, IIngredients). JEI 15 deleted IIngredients, so the two merge here;
     * ForgeTypes.FLUID_STACK replaces VanillaTypes.FLUID.
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WellLiquefaction wellLiquefaction, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 19)
                .addItemStacks(ingredientStacks(wellLiquefaction.getInput()));

        // 1.16.5: FluidAttributes.BUCKET_VOLUME -> 1.20.1 FluidType.BUCKET_VOLUME
        initFluidOutput(builder, 94, 18)
                .addIngredient(ForgeTypes.FLUID_STACK, new FluidStack(wellLiquefaction.getFluidOutput(), FluidType.BUCKET_VOLUME));
    }
}
