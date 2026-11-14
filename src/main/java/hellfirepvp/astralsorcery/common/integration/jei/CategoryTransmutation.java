/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.crafting.helper.RecipeHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
public class CategoryTransmutation extends JEICategory<BlockTransmutation> {

    private final IDrawable background, icon;
    private final ItemStack catalyst;

    public CategoryTransmutation(IGuiHelper guiHelper) {
        super(IntegrationJEI.CATEGORY_TRANSMUTATION);
        this.background = guiHelper.createDrawable(AstralSorcery.key("textures/gui/jei/transmutation.png"), 0, 0, 116, 54);
        // 1.16.5: new ItemStack(BlocksAS.LENS) - a RegistryObject here - and createDrawableIngredient,
        // which JEI 15 renamed to createDrawableItemStack.
        this.catalyst = new ItemStack(BlocksAS.LENS.get());
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
    public List<BlockTransmutation> getRecipes() {
        // 1.16.5: return RecipeTypesAS.TYPE_BLOCK_TRANSMUTATION.getAllRecipes();
        // This tree lists recipes of a type through the RecipeManager (see
        // common/data/journal/JournalPageBlockTransmutation.java); the copy keeps the result mutable
        // because IntegrationJEI#registerRecipes sorts it.
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mgr.getAllRecipesFor(RecipeTypesAS.BLOCK_TRANSMUTATION.get()));
    }

    /**
     * 1.16.5 signature: void setRecipe(IRecipeLayout, BlockTransmutation, IIngredients), fed by a separate
     * setIngredients(BlockTransmutation, IIngredients). JEI 15 deleted IIngredients, so the two merge here.
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BlockTransmutation transmutation, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 18)
                .addItemStacks(transmutation.getInputDisplay());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .addItemStack(transmutation.getOutputDisplay());
    }
}
