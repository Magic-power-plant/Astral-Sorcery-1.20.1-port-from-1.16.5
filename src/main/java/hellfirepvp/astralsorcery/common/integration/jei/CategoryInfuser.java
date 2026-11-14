/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryInfuser
 * Created by HellFirePvP
 * Date: 05.09.2020 / 12:37
 */
@OnlyIn(Dist.CLIENT)
public class CategoryInfuser extends JEICategory<LiquidInfusion> {

    /**
     * The twelve liquid starlight positions around the infuser's item slot, in the order 1.16.5 declared them
     * (1.16.5 CategoryInfuser.java:93-104). These are 1.16.5's raw coordinates; initFluidInput applies the
     * +1/+1 that turns a slot frame position into an ingredient position.
     */
    private static final int[][] FLUID_INPUTS = {
            {30,  57}, {49,  57}, {68,  57},
            {11,  76}, {87,  76},
            {11,  95}, {87,  95},
            {11, 114}, {87, 114},
            {30, 133}, {49, 133}, {68, 133}
    };

    private final IDrawable background, icon;
    private final ItemStack catalyst;

    public CategoryInfuser(IGuiHelper guiHelper) {
        super(IntegrationJEI.CATEGORY_INFUSER);
        this.background = guiHelper.createDrawable(AstralSorcery.key("textures/gui/jei/infuser.png"), 0, 0, 116, 162);
        // 1.16.5: new ItemStack(BlocksAS.INFUSER) - a RegistryObject here - and createDrawableIngredient,
        // which JEI 15 renamed to createDrawableItemStack.
        this.catalyst = new ItemStack(BlocksAS.INFUSER.get());
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

    @Override
    public List<LiquidInfusion> getRecipes() {
        // 1.16.5: return RecipeTypesAS.TYPE_INFUSION.getAllRecipes();
        // This tree lists recipes of a type through the RecipeManager (see
        // common/data/journal/JournalPageLiquidInfusion.java); the copy keeps the result mutable
        // because IntegrationJEI#registerRecipes sorts it.
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mgr.getAllRecipesFor(RecipeTypesAS.INFUSION.get()));
    }

    /**
     * 1.16.5 signature: void setRecipe(IRecipeLayout, LiquidInfusion, IIngredients), fed by a separate
     * setIngredients(LiquidInfusion, IIngredients). JEI 15 deleted IIngredients, so the two merge here:
     * VanillaTypes.ITEM became VanillaTypes.ITEM_STACK (implied by addItemStacks/addItemStack) and
     * VanillaTypes.FLUID became ForgeTypes.FLUID_STACK.
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LiquidInfusion liquidInfusion, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 50, 96)
                .addItemStacks(ingredientStacks(liquidInfusion.getItemInput()));

        // 1.16.5: FluidAttributes.BUCKET_VOLUME -> 1.20.1 FluidType.BUCKET_VOLUME
        FluidStack fInput = new FluidStack(liquidInfusion.getLiquidInput(), FluidType.BUCKET_VOLUME);
        for (int[] pos : FLUID_INPUTS) {
            initFluidInput(builder, pos[0], pos[1])
                    .addIngredient(ForgeTypes.FLUID_STACK, fInput.copy());
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 19)
                .addItemStack(liquidInfusion.getOutputForRender(Collections.emptyList()));
    }
}
