package hellfirepvp.astralsorcery.common.crafting.helper.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.lib.IngredientSerializersAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CrystalIngredient extends Ingredient {

    private final boolean hasToBeAttuned, hasToBeCelestial, canBeAttuned, canBeCelestialCrystal;

    public CrystalIngredient(boolean hasToBeAttuned, boolean hasToBeCelestial) {
        this(hasToBeAttuned, hasToBeCelestial, true, true);
    }

    public CrystalIngredient(boolean hasToBeAttuned, boolean hasToBeCelestial, boolean canBeAttuned, boolean canBeCelestialCrystal) {
        super(getItems(hasToBeAttuned, hasToBeCelestial, canBeAttuned, canBeCelestialCrystal));
        this.hasToBeAttuned = hasToBeAttuned;
        this.hasToBeCelestial = hasToBeCelestial;
        this.canBeAttuned = canBeAttuned;
        this.canBeCelestialCrystal = canBeCelestialCrystal;
    }

    private static Stream<? extends Ingredient.Value> getItems(boolean hasToBeAttuned, boolean hasToBeCelestial, boolean canBeAttuned, boolean canBeCelestialCrystal) {
        if (hasToBeAttuned) {
            canBeAttuned = true;
        }
        if (hasToBeCelestial) {
            canBeCelestialCrystal = true;
        }

        List<Ingredient.Value> values = new ArrayList<>();
        if (hasToBeAttuned) {
            if (hasToBeCelestial) {
                values.add(new Ingredient.ItemValue(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get())));
            } else {
                values.add(new Ingredient.ItemValue(new ItemStack(ItemsAS.ATTUNED_ROCK_CRYSTAL.get())));
                if (canBeCelestialCrystal) {
                    values.add(new Ingredient.ItemValue(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get())));
                }
            }
        } else {
            if (hasToBeCelestial) {
                values.add(new Ingredient.ItemValue(new ItemStack(ItemsAS.CELESTIAL_CRYSTAL.get())));
                if (canBeAttuned) {
                    values.add(new Ingredient.ItemValue(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get())));
                }
            } else {
                values.add(new Ingredient.ItemValue(new ItemStack(ItemsAS.ROCK_CRYSTAL.get())));
                if (canBeCelestialCrystal) {
                    values.add(new Ingredient.ItemValue(new ItemStack(ItemsAS.CELESTIAL_CRYSTAL.get())));
                }
                if (canBeAttuned) {
                    values.add(new Ingredient.ItemValue(new ItemStack(ItemsAS.ATTUNED_ROCK_CRYSTAL.get())));
                    values.add(new Ingredient.ItemValue(new ItemStack(ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get())));
                }
            }
        }
        return values.stream();
    }

    public boolean hasToBeAttuned() {
        return hasToBeAttuned;
    }

    public boolean hasToBeCelestial() {
        return hasToBeCelestial;
    }

    public boolean canBeAttuned() {
        return canBeAttuned;
    }

    public boolean canBeCelestialCrystal() {
        return canBeCelestialCrystal;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", CraftingHelper.getID(IngredientSerializersAS.CRYSTAL_SERIALIZER).toString());
        object.addProperty("hasToBeAttuned", this.hasToBeAttuned());
        object.addProperty("hasToBeCelestial", this.hasToBeCelestial());
        object.addProperty("canBeAttuned", this.canBeAttuned());
        object.addProperty("canBeCelestialCrystal", this.canBeCelestialCrystal());
        return object;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return IngredientSerializersAS.CRYSTAL_SERIALIZER;
    }
}
