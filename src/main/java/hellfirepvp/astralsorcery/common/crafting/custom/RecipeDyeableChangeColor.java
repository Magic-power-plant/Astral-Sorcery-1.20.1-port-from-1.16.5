package hellfirepvp.astralsorcery.common.crafting.custom;

import hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class RecipeDyeableChangeColor implements CraftingRecipe {

    private final ResourceLocation id;
    private final Supplier<RecipeSerializer<?>> serializer;
    private final Item targetItem;
    private final BiConsumer<ItemStack, DyeColor> colorFn;

    public RecipeDyeableChangeColor(ResourceLocation idIn, Supplier<RecipeSerializer<?>> serializer, Item targetItem, BiConsumer<ItemStack, DyeColor> colorFn) {
        this.id = idIn;
        this.serializer = serializer;
        this.targetItem = targetItem;
        this.colorFn = colorFn;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return tryFindValidRecipeAndDye(inv) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        Tuple<DyeColor, ItemStack> itemColorTpl = tryFindValidRecipeAndDye(inv);
        if (itemColorTpl == null) {
            return ItemStack.EMPTY;
        }
        ItemStack out = itemColorTpl.getB().copy();
        out.setCount(1);
        this.colorFn.accept(out, itemColorTpl.getA());
        return out;
    }

    @Nullable
    private Tuple<DyeColor, ItemStack> tryFindValidRecipeAndDye(CraftingContainer inv) {
        ItemStack itemFound = ItemStack.EMPTY;
        DyeColor dyeColorFound = null;
        int nonEmptyItemsFound = 0;

        for (int slot = 0; slot < inv.getContainerSize(); slot++) {
            ItemStack in = inv.getItem(slot);
            if (!in.isEmpty()) {
                nonEmptyItemsFound++;

                if (in.getItem().equals(this.targetItem)) {
                    itemFound = in;
                } else {
                    DyeColor color = DyeColor.getColor(in);
                    if (color != null) {
                        dyeColorFound = color;
                    }
                }
            }
        }

        if (itemFound.isEmpty() || dyeColorFound == null || nonEmptyItemsFound != 2) {
            return null;
        } else {
            return new Tuple<>(dyeColorFound, itemFound);
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    public static class Tuple<A, B> {
        private final A a;
        private final B b;
        public Tuple(A a, B b) { this.a = a; this.b = b; }
        public A getA() { return a; }
        public B getB() { return b; }
    }

    public static class IlluminationWandColorSerializer implements RecipeSerializer<RecipeDyeableChangeColor> {
        @Override
        public RecipeDyeableChangeColor fromJson(ResourceLocation recipeId, com.google.gson.JsonObject json) {
            return null;
        }

        @Override
        public RecipeDyeableChangeColor fromNetwork(ResourceLocation recipeId, net.minecraft.network.FriendlyByteBuf buffer) {
            return null;
        }

        @Override
        public void toNetwork(net.minecraft.network.FriendlyByteBuf buffer, RecipeDyeableChangeColor recipe) {
        }
    }

    public static class GatewayColorSerializer implements RecipeSerializer<RecipeDyeableChangeColor> {
        @Override
        public RecipeDyeableChangeColor fromJson(ResourceLocation recipeId, com.google.gson.JsonObject json) {
            return null;
        }

        @Override
        public RecipeDyeableChangeColor fromNetwork(ResourceLocation recipeId, net.minecraft.network.FriendlyByteBuf buffer) {
            return null;
        }

        @Override
        public void toNetwork(net.minecraft.network.FriendlyByteBuf buffer, RecipeDyeableChangeColor recipe) {
        }
    }
}
