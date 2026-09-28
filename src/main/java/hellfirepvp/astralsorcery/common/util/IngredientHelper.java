package hellfirepvp.astralsorcery.common.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;

public class IngredientHelper {

    @OnlyIn(Dist.CLIENT)
    public static ItemStack getRandomVisibleStack(Ingredient ingredient) {
        return getRandomVisibleStack(ingredient, 0);
    }

    @OnlyIn(Dist.CLIENT)
    public static ItemStack getRandomVisibleStack(Ingredient ingredient, long tick) {
        List<ItemStack> applicable = getVisibleItemStacks(ingredient);
        if (applicable.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int mod = (int) ((tick / 20L) % applicable.size());
        return applicable.get(Mth.clamp(mod, 0, applicable.size() - 1));
    }

    @OnlyIn(Dist.CLIENT)
    public static List<ItemStack> getVisibleItemStacks(Ingredient ingredient) {
        if (ingredient.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(ingredient.getItems());
    }

    @Nullable
    public static TagKey<Item> guessTag(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0) {
            return null;
        }
        List<TagKey<Item>> applicableTags = new ArrayList<>();
        ItemStack first = stacks[0];
        for (TagKey<Item> key : first.getItem().builtInRegistryHolder().tags().toList()) {
            Optional<net.minecraft.core.HolderSet.Named<Item>> tag = BuiltInRegistries.ITEM.getTag(key);
            if (tag.isEmpty()) {
                continue;
            }

            boolean containsAllItems = true;
            for (net.minecraft.core.Holder<Item> holder : tag.get()) {
                if (!ingredient.test(new ItemStack(holder.value()))) {
                    containsAllItems = false;
                    break;
                }
            }
            if (containsAllItems) {
                applicableTags.add(key);
            }
        }

        return applicableTags.stream()
                .max(Comparator.comparingInt(tag -> {
                    Optional<net.minecraft.core.HolderSet.Named<Item>> t = BuiltInRegistries.ITEM.getTag(tag);
                    return t.map(holderSet -> (int) holderSet.stream().count()).orElse(0);
                }))
                .orElse(null);
    }

}
