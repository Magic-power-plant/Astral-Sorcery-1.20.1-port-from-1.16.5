package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class FluidIngredientSerializer implements IIngredientSerializer<FluidIngredient> {

    @Override
    public FluidIngredient parse(JsonObject json) {
        if (!json.has("fluid")) {
            throw new JsonSyntaxException("Expected an array at 'fluid' or a single fluid defined at key 'fluid'.");
        }

        List<FluidStack> foundFluids = new ArrayList<>();
        JsonElement element = json.get("fluid");
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(e -> {
                if (e.isJsonObject()) {
                    JsonObject object = e.getAsJsonObject();
                    ResourceLocation key = ResourceLocation.parse(GsonHelper.getAsString(object, "fluid"));
                    if (!ForgeRegistries.FLUIDS.containsKey(key)) {
                        throw new JsonSyntaxException("Unknown fluid '" + key + "'");
                    }
                    int amount = 1000;
                    if (object.has("amount")) {
                        amount = GsonHelper.getAsInt(object, "amount");
                    }
                    Fluid fluid = ForgeRegistries.FLUIDS.getValue(key);
                    foundFluids.add(new FluidStack(fluid, amount));
                } else if (e.isJsonPrimitive()) {
                    ResourceLocation key = ResourceLocation.parse(e.getAsString());
                    if (!ForgeRegistries.FLUIDS.containsKey(key)) {
                        throw new JsonSyntaxException("Unknown fluid '" + key + "'");
                    }
                    Fluid fluid = ForgeRegistries.FLUIDS.getValue(key);
                    foundFluids.add(new FluidStack(fluid, 1000));
                } else {
                    throw new JsonSyntaxException("Value at key 'fluid' has to be a fluid name or an array of fluid names or objects containing 'fluid'.");
                }
            });
        } else if (element.isJsonPrimitive()) {
            ResourceLocation key = ResourceLocation.parse(element.getAsString());
            if (!ForgeRegistries.FLUIDS.containsKey(key)) {
                throw new JsonSyntaxException("Unknown fluid '" + key + "'");
            }
            int amount = 1000;
            if (json.has("amount")) {
                amount = GsonHelper.getAsInt(json, "amount");
            }
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(key);
            foundFluids.add(new FluidStack(fluid, amount));
        } else {
            throw new JsonSyntaxException("Value at key 'fluid' has to be a fluid name or an array of fluid names or objects containing 'fluid'.");
        }
        return new FluidIngredient(foundFluids);
    }

    @Override
    public FluidIngredient parse(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<FluidStack> fluids = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            fluids.add(FluidStack.readFromPacket(buffer));
        }
        return new FluidIngredient(fluids);
    }

    @Override
    public void write(FriendlyByteBuf buffer, FluidIngredient ingredient) {
        buffer.writeVarInt(ingredient.getFluids().size());
        for (FluidStack stack : ingredient.getFluids()) {
            stack.writeToPacket(buffer);
        }
    }
}
