package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class CrystalIngredientSerializer implements IIngredientSerializer<CrystalIngredient> {

    @Override
    public CrystalIngredient parse(JsonObject json) {
        boolean hasToBeAttuned = GsonHelper.getAsBoolean(json, "hasToBeAttuned", false);
        boolean hasToBeCelestial = GsonHelper.getAsBoolean(json, "hasToBeCelestial", false);
        boolean canBeAttuned = GsonHelper.getAsBoolean(json, "canBeAttuned", true);
        boolean canBeCelestialCrystal = GsonHelper.getAsBoolean(json, "canBeCelestialCrystal", true);
        return new CrystalIngredient(hasToBeAttuned, hasToBeCelestial, canBeAttuned, canBeCelestialCrystal);
    }

    @Override
    public CrystalIngredient parse(FriendlyByteBuf buffer) {
        return new CrystalIngredient(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer, CrystalIngredient ingredient) {
        buffer.writeBoolean(ingredient.hasToBeAttuned());
        buffer.writeBoolean(ingredient.hasToBeCelestial());
        buffer.writeBoolean(ingredient.canBeAttuned());
        buffer.writeBoolean(ingredient.canBeCelestialCrystal());
    }
}
