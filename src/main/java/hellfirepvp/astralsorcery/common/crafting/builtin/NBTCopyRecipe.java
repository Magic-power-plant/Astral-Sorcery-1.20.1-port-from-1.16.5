/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
//1.16.5 imported common/util/nbt/NBTHelper; this tree carries two NBTHelper classes
//(common/util/data and common/util/nbt) with an identical deepMerge - the nbt one is the reference's and is
//what the sibling recipe/** files use, so it is the one imported here.
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTCopyRecipe
 * Created by HellFirePvP
 * Date: 26.04.2020 / 08:02
 */
//1.16.5 extended common/crafting/recipe/SimpleAltarRecipe; this tree's builtin recipes extend the divergent
//top-level common/crafting/AltarRecipe, which is a sibling of - not a supertype of - the reference-faithful
//common/crafting/recipe/SimpleAltarRecipe (tracked as TODO_PORT[AS-ALT-03]). Accordingly the 1.16.5
//getOutputs(TileAltar) is getOutputsForAltar(TileAltar) here and the constructor takes the grid/outputs pair
//this tree's AltarRecipe requires.
public class NBTCopyRecipe extends AltarRecipe {

    private static final String KEY_SEARCH_ITEMS = "copy_nbt_from_items_matching";

    private final List<Ingredient> searchIngredients = new ArrayList<>();

    public NBTCopyRecipe(ResourceLocation id, AltarType altarType, AltarRecipeGrid grid, List<ItemStack> outputs, int starlightRequired, int craftingTime) {
        super(id, altarType, grid, outputs, starlightRequired, craftingTime);
    }

    public static NBTCopyRecipe convertToThis(AltarRecipe other) {
        NBTCopyRecipe recipe = new NBTCopyRecipe(other.getId(), other.getAltarType(), other.getGrid(), other.getOutputs(), other.getStarlightRequired(), other.getCraftingTime());
        recipe.copyCommonPropertiesFrom(other);
        return recipe;
    }

    //1.16.5: Ingredient.fromTag(Tag<Item>) - item tags are addressed by TagKey<Item> in 1.20.1 and the
    //factory is Ingredient.of(TagKey).
    public <T extends NBTCopyRecipe> T addNBTCopyMatchIngredient(TagKey<Item> tag) {
        return this.addNBTCopyMatchIngredient(Ingredient.of(tag));
    }

    //1.16.5: Ingredient.fromStacks(ItemStack...) - renamed to Ingredient.of(ItemStack...) in 1.20.1.
    public <T extends NBTCopyRecipe> T addNBTCopyMatchIngredient(ItemStack... items) {
        return this.addNBTCopyMatchIngredient(Ingredient.of(items));
    }

    //1.16.5: Ingredient.fromItems(IItemProvider...) - IItemProvider is ItemLike and the factory
    //Ingredient.of(ItemLike...) in 1.20.1.
    public <T extends NBTCopyRecipe> T addNBTCopyMatchIngredient(ItemLike... items) {
        return this.addNBTCopyMatchIngredient(Ingredient.of(items));
    }

    public <T extends NBTCopyRecipe> T addNBTCopyMatchIngredient(Ingredient ingredient) {
        this.searchIngredients.add(ingredient);
        return (T) this;
    }

    @Override
    public void deserializeAdditionalJson(JsonObject recipeObject) throws JsonSyntaxException {
        super.deserializeAdditionalJson(recipeObject);
        //1.16.5: JSONUtils.getJsonArray - renamed to GsonHelper.getAsJsonArray in 1.20.1.
        JsonArray list = GsonHelper.getAsJsonArray(recipeObject, KEY_SEARCH_ITEMS, new JsonArray());
        for (JsonElement element : list) {
            //1.16.5: Ingredient.deserialize(JsonElement). This tree routes every altar ingredient through
            //common/crafting/helper/WrappedIngredient so that its FluidIngredient extension keeps working;
            //for a plain item ingredient WrappedIngredient.fromJson delegates to Ingredient.fromJson.
            this.searchIngredients.add(WrappedIngredient.fromJson(element).getIngredient());
        }
    }

    @Override
    public void serializeAdditionalJson(JsonObject recipeObject) {
        super.serializeAdditionalJson(recipeObject);

        JsonArray list = new JsonArray();
        for (Ingredient ingredient : this.searchIngredients) {
            //1.16.5: Ingredient#serialize() - toJson() in 1.20.1, reached through WrappedIngredient so the
            //output stays symmetric with deserializeAdditionalJson above.
            list.add(new WrappedIngredient(ingredient).serializeJson());
        }
        recipeObject.add(KEY_SEARCH_ITEMS, list);
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputsForAltar(TileAltar altar) {
        List<ItemStack> outputs = super.getOutputsForAltar(altar);
        List<CompoundTag> foundTags = new ArrayList<>();
        //1.16.5 iterated TileAltar#getInventory() directly (a TileInventory is Iterable<ItemStack>); this
        //tree's inventory is an ItemStackHandlerAS, so the slots are walked explicitly.
        for (int slot = 0; slot < altar.getInventory().getSlots(); slot++) {
            ItemStack existing = altar.getInventory().getStackInSlot(slot);
            for (Ingredient match : this.searchIngredients) {
                if (match.test(existing) && existing.hasTag()) {
                    foundTags.add(existing.getTag().copy());
                }
            }
        }
        for (ItemStack output : outputs) {
            CompoundTag tag = output.getOrCreateTag();
            for (CompoundTag foundTag : foundTags) {
                NBTHelper.deepMerge(tag, foundTag, true);
            }
        }
        return outputs;
    }

    @Override
    public void readRecipeSync(FriendlyByteBuf buffer) {
        super.readRecipeSync(buffer);
        //1.16.5: this.searchIngredients = ByteBufUtils.readList(buf, Ingredient::read). The field is final
        //here, so the list is cleared and refilled - same observable result - and WrappedIngredient.read is
        //used instead of Ingredient.fromNetwork so this tree's FluidIngredient survives the round trip (it
        //mirrors write below). ByteBufUtils.readList returns null for the -1 size marker, which
        //ByteBufUtils.writeCollection only writes for a null collection - impossible here, but guarded.
        this.searchIngredients.clear();
        List<Ingredient> read = ByteBufUtils.readList(buffer, buf -> WrappedIngredient.read(buf).getIngredient());
        if (read != null) {
            this.searchIngredients.addAll(read);
        }
    }

    @Override
    public void writeRecipeSync(FriendlyByteBuf buffer) {
        super.writeRecipeSync(buffer);
        //1.16.5: ByteBufUtils.writeCollection(buf, this.searchIngredients, (b, i) -> i.write(b)); the
        //element codec goes through WrappedIngredient for the FluidIngredient reason above.
        ByteBufUtils.writeCollection(buffer, this.searchIngredients, (buf, ingredient) -> new WrappedIngredient(ingredient).write(buf));
    }
}
