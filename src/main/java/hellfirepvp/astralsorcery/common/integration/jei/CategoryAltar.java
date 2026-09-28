/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.common.block.BlockAltar;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryAltarDiscovery
 * Created by HellFirePvP
 * Date: 05.09.2020 / 14:16
 */
//TODO_PORT[AS-ALT-03]: 1.16.5 typed this category over common/crafting/recipe/SimpleAltarRecipe. In this tree
//that class is the reference-faithful but *dead* half of the altar-recipe split documented on
//common/crafting/AltarRecipe: nothing constructs a SimpleAltarRecipe, common/crafting/RecipeSerializersAS
//binds "astralsorcery:altar" to AltarRecipeSerializer, and every one of the 117 altar recipes the datapack
//loads is therefore an AltarRecipe. A category typed over SimpleAltarRecipe would show nothing at all, so this
//is typed over AltarRecipe and switches back to SimpleAltarRecipe when the two halves are merged.
//1.16.5 field/method renames that follow from that: SimpleAltarRecipe#getInputs() is AltarRecipe#getGrid(),
//and addRelayInput/getRelayInputs deal in WrappedIngredient rather than Ingredient.
@OnlyIn(Dist.CLIENT)
public class CategoryAltar extends JEICategory<AltarRecipe> {

    private final IDrawable background, icon;
    private final ItemStack catalyst;
    private final AltarType altarType;

    public CategoryAltar(RecipeType<AltarRecipe> recipeType, String textureRef, BlockAltar altarRef, IGuiHelper guiHelper) {
        super(recipeType);
        this.background = guiHelper.createDrawable(AstralSorcery.key(String.format("textures/gui/jei/%s.png", textureRef)), 0, 0, 116, 162);
        // 1.16.5: guiHelper.createDrawableIngredient(ItemStack) - renamed to createDrawableItemStack in JEI 15.
        this.catalyst = new ItemStack(altarRef);
        this.icon = guiHelper.createDrawableItemStack(this.catalyst);
        this.altarType = altarRef.getAltarType();
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

    public AltarType getAltarType() {
        return altarType;
    }

    @Override
    public List<AltarRecipe> getRecipes() {
        // 1.16.5 (1.16.5 CategoryAltar.java:78):
        //   return RecipeTypesAS.TYPE_ALTAR.getRecipes(recipe -> recipe.getAltarType().equals(this.getAltarType()));
        // TYPE_ALTAR was a ResolvingRecipeType in common/lib whose getRecipes(Predicate) filtered every recipe
        // registered under the altar recipe type. This tree registers the altar type through the
        // DeferredRegister in common/crafting/RecipeTypesAS as RecipeType<AltarRecipe>, so the same set comes
        // out of the RecipeManager; the copy keeps the result mutable because IntegrationJEI#registerRecipes
        // sorts it.
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        List<AltarRecipe> recipes = new ArrayList<>();
        if (mgr == null) {
            return recipes;
        }
        for (AltarRecipe recipe : mgr.getAllRecipesFor(RecipeTypesAS.ALTAR.get())) {
            if (recipe.getAltarType().equals(this.getAltarType())) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    /**
     * 1.16.5 signature: void draw(SimpleAltarRecipe, MatrixStack, double, double)
     * JEI 15 passes a GuiGraphics (plus an IRecipeSlotsView) instead of the MatrixStack; the PoseStack the
     * renderer wants is guiGraphics.pose().
     */
    @Override
    public void draw(AltarRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe.getFocusConstellation() != null) {
            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            IConstellation cst = recipe.getFocusConstellation();
            RenderingConstellationUtils.renderConstellationIntoGUI(Color.BLACK, cst, guiGraphics.pose(),
                    0, 0, 0,
                    50, 50, 1.2F,
                    () -> 0.9F, true, false);
            RenderSystem.disableBlend();
        }
    }

    /**
     * The grid indices this recipe actually occupies, ascending, i.e. exactly the slots
     * {@link #setRecipe} turns into {@link RecipeIngredientRole#INPUT} slots and in the same order.
     * {@link TieredAltarRecipeTransferHandler} maps the n-th of these onto the n-th altar container slot,
     * so the two must not drift apart - which is why the iteration lives here, next to the layout.
     * <p>
     * 1.16.5 declared all 25 grid positions as input slots and filtered the empty ones back out inside the
     * transfer handler (1.16.5 TieredAltarRecipeTransferHandler.java:97-110). JEI 15's own transfer plumbing
     * refuses a recipe view that declares more input slots than the container has crafting slots
     * (mezz.jei.library.transfer.BasicRecipeTransferHandler#validateRecipeView), and a T1 recipe drawn in a
     * 5x5 grid would declare 25 against the Luminous Crafting Table's 9, so the empty positions are skipped
     * up front instead. Nothing is lost visually: the slot frames are part of the category background.
     */
    public static List<Integer> occupiedGridSlots(AltarRecipe recipe) {
        AltarRecipeGrid grid = recipe.getGrid();
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < AltarRecipeGrid.MAX_INVENTORY_SIZE; slot++) {
            if (!grid.getIngredient(slot).isEmpty()) {
                slots.add(slot);
            }
        }
        return slots;
    }

    /**
     * 1.16.5 signature: void setRecipe(IRecipeLayout, SimpleAltarRecipe, IIngredients), fed by a separate
     * setIngredients(SimpleAltarRecipe, IIngredients). JEI 15 deleted IIngredients and setIngredients: the
     * ingredient lists are declared on the slots themselves, so the two 1.16.5 methods merge into this one.
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AltarRecipe altarRecipe, IFocusGroup focuses) {
        AltarRecipeGrid grid = altarRecipe.getGrid();

        int step = 19;
        int xOffset = 11;
        int yOffset = 57;
        for (int slot : occupiedGridSlots(altarRecipe)) {
            int xx = slot % AltarRecipeGrid.GRID_SIZE;
            int yy = slot / AltarRecipeGrid.GRID_SIZE;
            builder.addSlot(RecipeIngredientRole.INPUT, xOffset + step * xx + 1, yOffset + step * yy + 1)
                    .addItemStacks(ingredientStacks(grid.getIngredient(slot)));
        }

        // 1.16.5: MathHelper -> Mth in 1.20.1; clamp/floor keep their names.
        int centerX = 49;
        int centerY = 95;
        List<WrappedIngredient> relayInputs = altarRecipe.getRelayInputs();
        int additional = relayInputs.size();
        for (int i = 0; i < additional; i++) {
            double part = ((double) i) / ((double) additional) * 2.0 * Math.PI; //Shift by half a period
            part = Mth.clamp(part, 0, 2.0 * Math.PI);
            part += Math.PI;
            double xAdd = Math.sin(part) * 60.0;
            double yAdd = Math.cos(part) * 60.0;
            builder.addSlot(RecipeIngredientRole.INPUT, Mth.floor(centerX + xAdd) + 1, Mth.floor(centerY + yAdd) + 1)
                    .addItemStacks(ingredientStacks(relayInputs.get(i).getIngredient()));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 49, 19)
                .addItemStack(altarRecipe.getOutputForRender(Collections.emptyList()));
    }
}
