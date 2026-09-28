package hellfirepvp.astralsorcery.common.crafting.helper;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ResolvingRecipeType<C extends IItemHandler, T extends IHandlerRecipe<C>, R extends RecipeCraftingContext<T, C>> {

    private final ResourceLocation id;
    private final Class<T> baseClass;
    private final BiPredicate<T, R> matchFct;
    private final RecipeType<T> type;

    public ResolvingRecipeType(String name, Class<T> baseClass, BiPredicate<T, R> matchFct) {
        this(ResourceLocation.tryParse(name), baseClass, matchFct);
    }

    public ResolvingRecipeType(ResourceLocation id, Class<T> baseClass, BiPredicate<T, R> matchFct) {
        this.id = id;
        this.baseClass = baseClass;
        this.matchFct = matchFct;
        this.type = new RecipeType<T>() {
            @Override
            public String toString() {
                return ResolvingRecipeType.this.id.getPath();
            }
        };
    }

    @Nonnull
    public List<T> getAllRecipes() {
        return Collections.emptyList();
    }

    @Nonnull
    public List<T> getRecipes(Predicate<T> test) {
        return this.getAllRecipes().stream()
                .filter(test)
                .collect(Collectors.toList());
    }

    public final Class<T> getBaseClass() {
        return baseClass;
    }

    public RecipeType<T> getType() {
        return type;
    }

    public ResourceLocation getRegistryName() {
        return id;
    }

    @Nullable
    public T findRecipe(R context) {
        return null;
    }

    @Nonnull
    public List<T> findMatchingRecipes(R context) {
        return Collections.emptyList();
    }
}
