package hellfirepvp.astralsorcery.common.crafting.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.FluidIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WrappedIngredient {

    private final Ingredient ingredient;

    public WrappedIngredient(@Nonnull Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Nonnull
    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getRandomMatchingStack(long tick) {
        ItemStack[] stacks = this.ingredient.getItems();
        if (stacks.length == 0) {
            return ItemStack.EMPTY;
        }
        return stacks[(int) Math.floorMod(tick, stacks.length)].copy();
    }

    public JsonElement serializeJson() {
        if (ingredient instanceof FluidIngredient fluidIngredient) {
            return fluidIngredient.serializeFluidJson();
        }
        return ingredient.toJson();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(ingredient instanceof FluidIngredient);
        if (ingredient instanceof FluidIngredient fluidIngredient) {
            fluidIngredient.write(buffer);
        } else {
            ingredient.toNetwork(buffer);
        }
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ingredient", serializeJson().toString());
        return tag;
    }

    @Nullable
    public static WrappedIngredient deserialize(CompoundTag tag) {
        if (!tag.contains("ingredient")) {
            return null;
        }
        JsonElement json = JsonParser.parseString(tag.getString("ingredient"));
        return fromJson(json);
    }

    public static WrappedIngredient read(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            return new WrappedIngredient(FluidIngredient.read(buffer));
        }
        return new WrappedIngredient(Ingredient.fromNetwork(buffer));
    }

    public static WrappedIngredient fromJson(JsonElement json) {
        JsonObject object = json.isJsonObject() ? json.getAsJsonObject() : null;
        if (object != null && object.has("type") && "astralsorcery:fluid".equals(object.get("type").getAsString())) {
            return new WrappedIngredient(FluidIngredient.fromJson(object));
        }
        if (object != null && object.has("fluid") && !object.has("item") && !object.has("tag")) {
            return new WrappedIngredient(FluidIngredient.fromJson(object));
        }
        return new WrappedIngredient(Ingredient.fromJson(json));
    }
}
