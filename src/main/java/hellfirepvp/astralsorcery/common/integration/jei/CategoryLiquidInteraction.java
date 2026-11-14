/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei.JEIInteractionResultRegistry;
import hellfirepvp.astralsorcery.common.integration.IntegrationJEI;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.crafting.helper.RecipeHelper;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryLiquidInteraction
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:31
 */
@OnlyIn(Dist.CLIENT)
public class CategoryLiquidInteraction extends JEICategory<LiquidInteraction> {

    private static final DecimalFormat FORMAT_CHANCE = new DecimalFormat("0.00");

    private final IDrawable background, icon;
    private final ItemStack catalyst;

    public CategoryLiquidInteraction(IGuiHelper guiHelper) {
        super(IntegrationJEI.CATEGORY_LIQUID_INTERACTION);
        this.background = guiHelper.createDrawable(AstralSorcery.key("textures/gui/jei/interaction.png"), 0, 0, 112, 54);
        // 1.16.5: new ItemStack(BlocksAS.CHALICE) - a RegistryObject here - and createDrawableIngredient,
        // which JEI 15 renamed to createDrawableItemStack.
        this.catalyst = new ItemStack(BlocksAS.CHALICE.get());
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
    public List<LiquidInteraction> getRecipes() {
        // 1.16.5: return RecipeTypesAS.TYPE_LIQUID_INTERACTION.getAllRecipes();
        // This port has no lib/RecipeTypesAS ResolvingRecipeType; the tree lists recipes of a type
        // through the RecipeManager instead (see common/tile/TileChalice.java:179). The result is
        // copied into an ArrayList because RecipeManager#getAllRecipesFor hands out an immutable
        // list and IntegrationJEI#registerRecipes sorts what this returns.
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mgr.getAllRecipesFor(RecipeTypesAS.LIQUID_INTERACTION.get()));
    }

    /**
     * 1.16.5 signature: void draw(LiquidInteraction, MatrixStack, double, double)
     * JEI 15 passes a GuiGraphics (plus an IRecipeSlotsView) instead of the MatrixStack.
     */
    @Override
    public void draw(LiquidInteraction recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.icon.draw(guiGraphics, 3, 36);
        this.icon.draw(guiGraphics, 93, 36);

        JEIInteractionResultRegistry.get(recipe.getResult().getId())
                .ifPresent(handler -> handler.drawRecipe(recipe, guiGraphics, mouseX, mouseY));

        // 1.16.5: FluidAttributes.BUCKET_VOLUME -> 1.20.1 net.minecraftforge.fluids.FluidType.BUCKET_VOLUME
        FluidStack testMatch1 = new FluidStack(recipe.getReactant1(), FluidType.BUCKET_VOLUME);
        FluidStack testMatch2 = new FluidStack(recipe.getReactant2(), FluidType.BUCKET_VOLUME);
        // 1.16.5 (CategoryLiquidInteraction.java:88-89):
        //   LiquidInteractionContext ctx = new LiquidInteractionContext(testMatch1, testMatch2);
        //   Collection<LiquidInteraction> sameInteractions = RecipeTypesAS.TYPE_LIQUID_INTERACTION.findMatchingRecipes(ctx);
        // common/crafting/helper/ResolvingRecipeType#findMatchingRecipes exists in this tree but still
        // returns an empty list, and nothing constructs a ResolvingRecipeType for the interaction type,
        // so the match is done directly against LiquidInteraction#matches(FluidStack, FluidStack) -
        // exactly what the context predicate resolved to in 1.16.5 (cf. TileChalice.java:179-183).
        Collection<LiquidInteraction> sameInteractions = new ArrayList<>();
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr != null) {
            for (LiquidInteraction interaction : mgr.getAllRecipesFor(RecipeTypesAS.LIQUID_INTERACTION.get())) {
                if (interaction.matches(testMatch1, testMatch2)) {
                    sameInteractions.add(interaction);
                }
            }
        }
        if (!sameInteractions.isEmpty()) {
            int totalWeight = sameInteractions.stream().mapToInt(LiquidInteraction::getWeight).sum();
            float perc = ((float) recipe.getWeight() / totalWeight) * 100;

            // 1.16.5: Minecraft#fontRenderer -> font, FontRenderer -> Font,
            //         new TranslationTextComponent(..) -> Component.translatable(..),
            //         IFormattableTextComponent -> MutableComponent,
            //         fr.getStringPropertyWidth(txt) -> fr.width(txt),
            //         fr.func_243248_b(matrixStack, txt, x, y, color) (draw without shadow)
            //           -> guiGraphics.drawString(font, txt, x, y, color, false)
            Font fr = Minecraft.getInstance().font;
            MutableComponent txt = Component.translatable("jei.astralsorcery.tip.chance", FORMAT_CHANCE.format(perc));
            int width = fr.width(txt);
            guiGraphics.drawString(fr, txt, 74 - width, 44, 0x333333, false);
        }
    }

    /**
     * 1.16.5 signature: void setRecipe(IRecipeLayout, LiquidInteraction, IIngredients), fed by a separate
     * setIngredients(LiquidInteraction, IIngredients) that also called the result handler's
     * addToRecipeIngredients. JEI 15 deleted IIngredients and with it the reason for the two-step split, so
     * setIngredients/addToRecipeIngredients are gone and everything is declared on the layout builder:
     * ForgeTypes.FLUID_STACK replaces VanillaTypes.FLUID and the 1.16.5 fluid-slot arguments
     * (x + 1, y + 1, 16, 16, recipe.getReactantN().getAmount(), false, null) become an explicit
     * setFluidRenderer on the slot, because the reactant amount is the capacity the bar is scaled against
     * rather than JEICategory#initFluidInput's flat 1000.
     */
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LiquidInteraction recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 19)
                .setFluidRenderer(recipe.getReactant1().getAmount(), false, 16, 16)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.getReactant1());
        builder.addSlot(RecipeIngredientRole.INPUT, 93, 19)
                .setFluidRenderer(recipe.getReactant2().getAmount(), false, 16, 16)
                .addIngredient(ForgeTypes.FLUID_STACK, recipe.getReactant2());

        JEIInteractionResultRegistry.get(recipe.getResult().getId())
                .ifPresent(handler -> handler.addToRecipeLayout(builder, recipe));
    }
}
