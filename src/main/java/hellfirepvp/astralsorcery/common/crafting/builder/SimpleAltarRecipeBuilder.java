/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builder;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeTypeHandler;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeBuilder;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.crafting.serializer.SimpleAltarRecipeSerializer;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleAltarRecipeBuilder
 * Created by HellFirePvP
 * Date: 07.03.2020 / 11:14
 */
public class SimpleAltarRecipeBuilder<T extends SimpleAltarRecipe> extends CustomRecipeBuilder<T> {

    private final T recipe;

    private SimpleAltarRecipeBuilder(T recipe) {
        this.recipe = recipe;
    }

    public static TypedRecipeBuilder<SimpleAltarRecipe> builder() {
        return new TypedRecipeBuilder<>(null);
    }

    //BLOCKED_PORT[AS-ALT-03]: 1.16.5 signature (1.16.5 common/crafting/builder/SimpleAltarRecipeBuilder.java:53-55):
    //  public static <T extends SimpleAltarRecipe> TypedRecipeBuilder<T> ofType(AltarRecipeTypeHandler.Type<T> type)
    // This tree's common/crafting/AltarRecipeTypeHandler declares Type<T extends AltarRecipe>, and every one of
    // its seven builtin converters produces a subclass of the divergent common.crafting.AltarRecipe, which is a
    // sibling of - not a supertype of - SimpleAltarRecipe. Type<T extends SimpleAltarRecipe> therefore does not
    // type-check and this.type.convert(recipe) cannot be applied to a SimpleAltarRecipe at all. Until the two
    // recipe hierarchies are merged, only the type key is carried - which is exactly what
    // SimpleAltarRecipe.read already does for the very same reason (see its TODO_PORT[AS-ALT-03]).
    public static TypedRecipeBuilder<SimpleAltarRecipe> ofType(AltarRecipeTypeHandler.Type<?> type) {
        return new TypedRecipeBuilder<>(type.getKey());
    }

    public SimpleAltarRecipeBuilder<T> setInputs(AltarRecipeGrid.Builder gridBuilder) {
        this.recipe.setInputs(gridBuilder.build());
        return this;
    }

    public SimpleAltarRecipeBuilder<T> setFocusConstellation(IConstellation focusConstellation) {
        this.recipe.setFocusConstellation(focusConstellation);
        return this;
    }

    //1.16.5: ITag.INamedTag<Item> - the tag wrapper types were replaced by TagKey<Item> in 1.20.1 and
    //Ingredient.fromTag was renamed to Ingredient.of(TagKey).
    public SimpleAltarRecipeBuilder<T> addRelayInput(TagKey<Item> tag) {
        return this.addRelayInput(Ingredient.of(tag));
    }

    //1.16.5: IItemProvider -> ItemLike, Ingredient.fromItems -> Ingredient.of in 1.20.1.
    public SimpleAltarRecipeBuilder<T> addRelayInput(ItemLike item) {
        return this.addRelayInput(Ingredient.of(item));
    }

    public SimpleAltarRecipeBuilder<T> addRelayInput(Ingredient ingredient) {
        this.recipe.addRelayInput(ingredient);
        return this;
    }

    public SimpleAltarRecipeBuilder<T> addAltarEffect(AltarRecipeEffect effect) {
        this.recipe.addAltarEffect(effect);
        return this;
    }

    public SimpleAltarRecipeBuilder<T> setStarlightRequirement(float percentOfAltarBar) {
        //1.16.5: MathHelper.clamp -> Mth.clamp in 1.20.1
        this.recipe.setStarlightRequirement((int) (this.recipe.getAltarType().getStarlightCapacity() * Mth.clamp(percentOfAltarBar, 0F, 1F)));
        return this;
    }

    public SimpleAltarRecipeBuilder<T> setStarlightRequirement(int requiredStarlight) {
        this.recipe.setStarlightRequirement(requiredStarlight);
        return this;
    }

    public SimpleAltarRecipeBuilder<T> multiplyDuration(float duration) {
        this.recipe.setDuration((int) (this.recipe.getAltarType().getDefaultAltarCraftingDuration() * duration));
        return this;
    }

    public SimpleAltarRecipeBuilder<T> setDuration(int duration) {
        this.recipe.setDuration(duration);
        return this;
    }

    public SimpleAltarRecipeBuilder<T> addOutput(ItemLike output) {
        this.addOutput(new ItemStack(output));
        return this;
    }

    public SimpleAltarRecipeBuilder<T> addOutput(ItemStack output) {
        this.recipe.addOutput(output);
        return this;
    }

    public SimpleAltarRecipeBuilder<T> modify(Consumer<T> recipeFn) {
        recipeFn.accept(this.recipe);
        return this;
    }

    @Nonnull
    @Override
    protected T validateAndGet() {
        AltarType type = this.recipe.getAltarType();
        this.recipe.getInputs().validate(type);

        if (!type.isThisGEThan(AltarType.RADIANCE)) {
            if (this.recipe.getRelayInputs().size() > 0) {
                throw new IllegalArgumentException("Cannot make a altar recipe require relay inputs, if the recipe isn't for a T4 altar or higher.");
            }
            if (this.recipe.getFocusConstellation() != null) {
                throw new IllegalArgumentException("Cannot make a altar recipe require a constellation focus, if the recipe isn't for a T4 altar or higher.");
            }
        }
        if (this.recipe.getDuration() <= 0) {
            throw new IllegalArgumentException("Cannot make a altar recipe with 0 or less ticks duration!");
        }
        if (this.recipe.getStarlightRequirement() > type.getStarlightCapacity()) {
            throw new IllegalArgumentException("Cannot make a recipe require more starlight than the capacity of the corresponding altar allows for.");
        }

        return this.recipe;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CustomRecipeSerializer<T> getSerializer() {
        //1.16.5: RecipeSerializersAS.ALTAR_RECIPE_SERIALIZER (common/lib). common/lib is not owned by this
        //slice and this tree's common/crafting/RecipeSerializersAS already binds the "altar" serializer id to
        //the divergent AltarRecipeSerializer, so the single instance is held on the serializer class itself.
        return (CustomRecipeSerializer<T>) SimpleAltarRecipeSerializer.INSTANCE;
    }

    public static class TypedRecipeBuilder<T extends SimpleAltarRecipe> {

        //1.16.5 held the AltarRecipeTypeHandler.Type<T> itself here - see the BLOCKED_PORT[AS-ALT-03] note on
        //ofType for why only its key can be carried in this tree.
        @Nullable
        private final ResourceLocation type;

        private TypedRecipeBuilder(@Nullable ResourceLocation type) {
            this.type = type;
        }

        //1.16.5 signature: createRecipe(ForgeRegistryEntry<?> nameProvider, AltarType altarType)
        //(1.16.5 common/crafting/builder/SimpleAltarRecipeBuilder.java:158-160). IForgeRegistryEntry and
        //ForgeRegistryEntry were removed in 1.20.1 along with getRegistryName(), so the registry name has to
        //be looked up through the registry. Every 1.16.5 call site passed an Item or a Block, both of which
        //are ItemLike, so the path is resolved through ForgeRegistries.ITEMS on the item form.
        //TODO_PORT[AS-ALT-08]: no 1.20.1 supertype covers "anything with a registry name" any more; if a call
        // site ever needs a non-ItemLike name provider it needs its own overload.
        public SimpleAltarRecipeBuilder<T> createRecipe(ItemLike nameProvider, AltarType altarType) {
            return this.createRecipe(AstralSorcery.key(ForgeRegistries.ITEMS.getKey(nameProvider.asItem()).getPath()), altarType);
        }

        @SuppressWarnings("unchecked")
        public SimpleAltarRecipeBuilder<T> createRecipe(ResourceLocation recipeId, AltarType altarType) {
            SimpleAltarRecipe recipe = new SimpleAltarRecipe(recipeId, altarType);
            //1.16.5 ran the recipe through AltarRecipeTypeHandler.DEFAULT.convert(recipe) when no type was
            //given; this tree's AltarRecipeTypeHandler.DEFAULT converter is Function.identity(), so the
            //untyped branch is the identity either way. The typed branch is blocked - see
            //BLOCKED_PORT[AS-ALT-03] on ofType - and only records the key, as SimpleAltarRecipe.read does.
            T converted = (T) recipe;
            if (this.type != null) {
                converted.setCustomRecipeType(this.type);
            }
            return new SimpleAltarRecipeBuilder<>(converted);
        }
    }
}
