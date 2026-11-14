package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageAltarRecipe;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageRecipe;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageText;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderablePage;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.crafting.helper.RecipeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class JournalPageRecipe implements JournalPage {

    private final Supplier<Recipe<?>> recipeProvider;

    private JournalPageRecipe(Supplier<Recipe<?>> recipeProvider) {
        this.recipeProvider = recipeProvider;
    }

    public static JournalPageRecipe fromName(ResourceLocation recipeId) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }

            Recipe<?> recipe = mgr.getAllRecipesFor(RecipeTypesAS.ALTAR.get()).stream()
                    .filter(r -> r.getId().equals(recipeId))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }

            recipe = mgr.getAllRecipesFor(RecipeType.CRAFTING).stream()
                    .filter(r -> r.getId().equals(recipeId))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }
            return null;
        });
    }

    public static JournalPageRecipe fromOutputPreferAltarRecipes(Predicate<ItemStack> outputTest) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }

            Recipe<?> recipe = mgr.getAllRecipesFor(RecipeTypesAS.ALTAR.get()).stream()
                    .filter(r -> outputTest.test(r.getOutputForRender(Collections.emptyList())))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }

            recipe = mgr.getAllRecipesFor(RecipeType.CRAFTING).stream()
                    .filter(r -> outputTest.test(r.getResultItem(null)))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }
            return null;
        });
    }

    public static JournalPageRecipe fromOutputPreferVanillaRecipes(Predicate<ItemStack> outputTest) {
        return new JournalPageRecipe(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }

            Recipe<?> recipe = mgr.getAllRecipesFor(RecipeType.CRAFTING).stream()
                    .filter(r -> outputTest.test(r.getResultItem(null)))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }

            recipe = mgr.getAllRecipesFor(RecipeTypesAS.ALTAR.get()).stream()
                    .filter(r -> outputTest.test(r.getOutputForRender(Collections.emptyList())))
                    .findFirst()
                    .orElse(null);
            if (recipe != null) {
                return recipe;
            }
            return null;
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        Recipe<?> recipe = this.recipeProvider.get();
        if (recipe instanceof AltarRecipe) {
            return new RenderPageAltarRecipe(node, nodePage, (AltarRecipe) recipe);
        } else if (recipe != null) {
            return RenderPageRecipe.fromRecipe(node, nodePage, recipe);
        } else {
            return new RenderPageText("astralsorcery.journal.recipe.removalinfo");
        }
    }
}
