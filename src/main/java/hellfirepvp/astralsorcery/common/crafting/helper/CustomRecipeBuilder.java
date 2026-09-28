package hellfirepvp.astralsorcery.common.crafting.helper;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class CustomRecipeBuilder<R extends Recipe<?>> {

    private static final Map<RecipeType<?>, Set<ResourceLocation>> builtRecipes = new HashMap<>();

    public void build(Consumer<FinishedRecipe> consumerIn) {
        this.build(consumerIn, null);
    }

    public void build(Consumer<FinishedRecipe> consumerIn, @Nullable String directory) {
        R recipe = this.validateAndGet();

        String saveId = recipe.getId().getPath();
        if (directory != null) {
            saveId = directory + "/" + saveId;
        }
        saveId = this.getSerializer().getRegistryName().getPath() + "/" + saveId;
        ResourceLocation id = ResourceLocation.parse(recipe.getId().getNamespace() + ":" + saveId);

        if (!builtRecipes.computeIfAbsent(recipe.getType(), type -> new HashSet<>()).add(id)) {
            throw new IllegalArgumentException("Tried to register recipe with id " + id + " twice for type " + ForgeRegistries.RECIPE_TYPES.getKey(recipe.getType()));
        }
        consumerIn.accept(new WrappedCustomRecipe(recipe, id));
    }

    @Nonnull
    protected abstract R validateAndGet();

    protected abstract CustomRecipeSerializer<R> getSerializer();

    private class WrappedCustomRecipe implements FinishedRecipe {

        private final R recipe;
        private final ResourceLocation id;

        private WrappedCustomRecipe(R recipe, ResourceLocation id) {
            this.recipe = recipe;
            this.id = id;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            CustomRecipeBuilder.this.getSerializer().write(json, this.recipe);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return this.recipe.getSerializer();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return ResourceLocation.parse("");
        }
    }
}
