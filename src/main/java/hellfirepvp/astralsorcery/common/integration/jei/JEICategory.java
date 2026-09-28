/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JEICategory
 * Created by HellFirePvP
 * Date: 05.09.2020 / 12:38
 */
//1.16.5 declared "implements IRecipeCategory<T>" (1.16.5 JEICategory.java:30) and identified a category by a
//bare ResourceLocation handed to getUid(). JEI 15 replaced getUid()/getRecipeClass() with a single
//getRecipeType() returning mezz.jei.api.recipe.RecipeType<T>, which carries both, so the id is taken as a
//RecipeType here and the two 1.16.5 accessors are gone rather than kept as unused adapters. The eight
//RecipeType instances live on IntegrationJEI, which is also what registerRecipes/registerRecipeCatalysts/
//registerRecipeTransferHandlers need them for.
//@OnlyIn(Dist.CLIENT) because IRecipeCategory#draw takes a net.minecraft.client.gui.GuiGraphics: every
//implementor of this class is therefore client-only code. Only IntegrationJEI#registerCategories constructs
//them, and JEI instantiates its plugins exclusively from mezz.jei.forge.JustEnoughItemsClient, which
//mezz.jei.forge.JustEnoughItems creates through DistExecutor.safeRunWhenOn(Dist.CLIENT, ..), so a dedicated
//server never reaches any of this.
@OnlyIn(Dist.CLIENT)
public abstract class JEICategory<T extends Recipe<?>> implements IRecipeCategory<T> {

    private final Component locTitle;
    private final RecipeType<T> recipeType;

    public JEICategory(RecipeType<T> recipeType) {
        this(category(recipeType.getUid()), recipeType);
    }

    public JEICategory(String unlocTitle, RecipeType<T> recipeType) {
        // 1.16.5 resolved the key eagerly with I18n.format(unlocTitle) because IRecipeCategory#getTitle
        // returned a String. JEI 15 wants a Component, so the key is wrapped untranslated and resolves at
        // draw time - which additionally survives a language change without rebuilding the categories.
        this.locTitle = Component.translatable(unlocTitle);
        this.recipeType = recipeType;
    }

    protected static String category(ResourceLocation categoryId) {
        return String.format("jei.category.%s.%s", categoryId.getNamespace(), categoryId.getPath());
    }

    protected static List<ItemStack> ingredientStacks(Ingredient ingredient) {
        // 1.16.5: Ingredient#getMatchingStacks()
        return Arrays.asList(ingredient.getItems());
    }

    //1.16.5 took an IGuiFluidStackGroup and called
    //group.init(index, true, x + 1, y + 1, 16, 16, 1000, false, null) (1.16.5 JEICategory.java:52-54).
    //JEI 15 deleted IGuiFluidStackGroup; a fluid slot is an ordinary layout slot with a fluid renderer on it,
    //and the ingredient itself is added by the caller. The +1/+1 is kept for a reason: a 1.16.5
    //IGuiItemStackGroup#init(index, input, x, y) reserved an 18x18 slot at (x, y) and drew the 16x16 stack
    //inside it at (x + 1, y + 1), whereas a fluid slot was positioned at its 16x16 area directly - hence
    //1.16.5's own +1. JEI 15 positions *every* slot by its 16x16 ingredient area (see
    //mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder, whose rect is 16x16 and whose
    //setStandardSlotBackground() draws the 18x18 frame at an offset of -1/-1), so the same +1 now applies to
    //item slots too and every 1.16.5 itemStacks.init(i, role, x, y) becomes addSlot(role, x + 1, y + 1).
    protected static IRecipeSlotBuilder initFluidInput(IRecipeLayoutBuilder builder, int x, int y) {
        return builder.addSlot(RecipeIngredientRole.INPUT, x + 1, y + 1)
                .setFluidRenderer(1000, false, 16, 16);
    }

    //as above for the output variant (group.init(index, false, x + 1, y + 1, 16, 16, 1000, false, null),
    //1.16.5 JEICategory.java:56-58).
    protected static IRecipeSlotBuilder initFluidOutput(IRecipeLayoutBuilder builder, int x, int y) {
        return builder.addSlot(RecipeIngredientRole.OUTPUT, x + 1, y + 1)
                .setFluidRenderer(1000, false, 16, 16);
    }

    public abstract List<T> getRecipes();

    /**
     * The stack shown as this category's catalyst, i.e. the block that performs the craft. 1.16.5 listed the
     * eight stacks inline in IntegrationJEI#registerRecipeCatalysts (1.16.5 IntegrationJEI.java:112-119); it
     * is pulled onto the category here so the catalyst cannot drift away from the icon, which is the same
     * stack, and so registerRecipeCatalysts can walk CATEGORIES exactly like registerRecipes does.
     */
    public abstract ItemStack getCatalyst();

    @Override
    public RecipeType<T> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return this.locTitle;
    }
}
