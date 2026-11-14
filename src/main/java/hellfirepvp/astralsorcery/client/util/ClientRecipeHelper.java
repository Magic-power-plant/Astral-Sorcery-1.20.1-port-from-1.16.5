package hellfirepvp.astralsorcery.client.util;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.function.Predicate;
import hellfirepvp.astralsorcery.common.crafting.helper.RecipeHelper;

public final class ClientRecipeHelper {
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static AltarRecipe findAltarRecipeResult(Predicate<ItemStack> outputPredicate) {
        RecipeManager manager = RecipeHelper.getRecipeManager();
        if (manager == null) {
            return null;
        }
        for (AltarRecipe recipe : manager.getAllRecipesFor(RecipeTypesAS.ALTAR.get())) {
            if (outputPredicate.test(recipe.getResult())) {
                return recipe;
            }
        }
        return null;
    }

}
