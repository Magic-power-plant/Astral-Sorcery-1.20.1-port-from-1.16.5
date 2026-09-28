package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class FluidIngredient extends Ingredient {

    private final List<FluidStack> fluids;
    private ItemStack[] itemArray = null;

    public FluidIngredient(List<FluidStack> fluids) {
        super(Stream.empty());
        this.fluids = fluids;
    }

    public FluidIngredient(FluidStack... fluids) {
        this(Arrays.asList(fluids));
    }

    public List<FluidStack> getFluids() {
        return fluids;
    }

    @Override
    public ItemStack[] getItems() {
        if (itemArray == null) {
            List<ItemStack> stacks = new ArrayList<>();
            for (FluidStack fluid : fluids) {
                stacks.add(FluidUtil.getFilledBucket(fluid));
            }
            itemArray = stacks.toArray(ItemStack[]::new);
        }
        return itemArray;
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        FluidStack contained = FluidUtil.getFluidContained(input).orElse(FluidStack.EMPTY);
        if (contained.isEmpty()) {
            return false;
        }
        for (FluidStack target : fluids) {
            if (contained.containsFluid(target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return fluids.isEmpty();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public JsonElement serializeFluidJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", "astralsorcery:fluid");
        JsonArray array = new JsonArray();
        for (FluidStack stack : fluids) {
            JsonObject fluid = new JsonObject();
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
            if (id != null) {
                fluid.addProperty("fluid", id.toString());
                fluid.addProperty("amount", stack.getAmount());
                array.add(fluid);
            }
        }
        object.add("fluid", array);
        return object;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(fluids.size());
        for (FluidStack stack : fluids) {
            buffer.writeFluidStack(stack);
        }
    }

    public static FluidIngredient read(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<FluidStack> fluids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            fluids.add(buffer.readFluidStack());
        }
        return new FluidIngredient(fluids);
    }

    public static FluidIngredient fromJson(JsonObject json) {
        if (!json.has("fluid")) {
            throw new JsonSyntaxException("Expected 'fluid' for fluid ingredient");
        }
        List<FluidStack> fluids = new ArrayList<>();
        JsonElement element = json.get("fluid");
        if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                fluids.add(readFluid(entry));
            }
        } else {
            int amount = GsonHelper.getAsInt(json, "amount", FluidType.BUCKET_VOLUME);
            fluids.add(readFluid(element, amount));
        }
        return new FluidIngredient(fluids);
    }

    private static FluidStack readFluid(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            return readFluid(object.get("fluid"), GsonHelper.getAsInt(object, "amount", FluidType.BUCKET_VOLUME));
        }
        return readFluid(element, FluidType.BUCKET_VOLUME);
    }

    private static FluidStack readFluid(JsonElement element, int amount) {
        ResourceLocation id = ResourceLocation.tryParse(GsonHelper.convertToString(element, "fluid"));
        var fluid = id == null ? null : BuiltInRegistries.FLUID.get(id);
        if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
            throw new JsonSyntaxException("Unknown fluid '" + id + "'");
        }
        return new FluidStack(fluid, amount);
    }
}
