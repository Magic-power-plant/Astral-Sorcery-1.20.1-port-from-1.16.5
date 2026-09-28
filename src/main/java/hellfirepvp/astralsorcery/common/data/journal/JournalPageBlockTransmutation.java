package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageBlockTransmutation;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageText;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderablePage;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class JournalPageBlockTransmutation implements JournalPage {

    private final Supplier<BlockTransmutation> recipeProvider;

    private JournalPageBlockTransmutation(Supplier<BlockTransmutation> recipeProvider) {
        this.recipeProvider = recipeProvider;
    }

    public static JournalPageBlockTransmutation fromOutput(Predicate<ItemStack> outputTest) {
        return new JournalPageBlockTransmutation(() -> {
            RecipeManager mgr = RecipeHelper.getRecipeManager();
            if (mgr == null) {
                throw new IllegalStateException("Not connected to a server, but calling GUI code?");
            }
            return mgr.getAllRecipesFor(RecipeTypesAS.BLOCK_TRANSMUTATION.get()).stream()
                    .filter(r -> outputTest.test(r.getOutputDisplay()))
                    .findFirst()
                    .orElse(null);
        });
    }

    @Override
    public RenderablePage buildRenderPage(ResearchNode node, int page) {
        BlockTransmutation recipe = this.recipeProvider.get();
        if (recipe != null) {
            return new RenderPageBlockTransmutation(node, page, recipe);
        } else {
            return new RenderPageText("astralsorcery.journal.recipe.removalinfo");
        }
    }
}
