/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.serializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInfusionSerializer
 * Created by HellFirePvP
 * Date: 26.07.2019 / 21:30
 */
public class LiquidInfusionSerializer extends CustomRecipeSerializer<LiquidInfusion> {

    //1.16.5 held the single instance in common/lib/RecipeSerializersAS as LIQUID_INFUSION_SERIALIZER.
    //common/lib is not owned by this slice, and this tree's common/crafting/RecipeSerializersAS binds the
    //"astralsorcery:infuser" id to the nested LiquidInfusion.Serializer shell, which forwards to this
    //instance - so there is still exactly one implementation of the format.
    //TODO_PORT[AS-ALT-05]: move to common/lib/RecipeSerializersAS#LIQUID_INFUSION_SERIALIZER and register
    // this instance directly instead of through the nested shell.
    public static final LiquidInfusionSerializer INSTANCE = new LiquidInfusionSerializer();

    public LiquidInfusionSerializer() {
        super(RecipeSerializersAS.INFUSER.getId());
    }

    @Override
    public LiquidInfusion fromJson(ResourceLocation recipeId, JsonObject json) {
        //1.16.5: new ResourceLocation(String) / JSONUtils.getString - the single-argument constructor is
        //deprecated in this Forge version, ResourceLocation.parse throws the same ResourceLocationException.
        ResourceLocation fluidKey = ResourceLocation.parse(GsonHelper.getAsString(json, "fluidInput"));
        Fluid fluidInput = ForgeRegistries.FLUIDS.getValue(fluidKey);
        if (fluidInput == null || fluidInput == Fluids.EMPTY) {
            throw new JsonSyntaxException("Unknown fluid: " + fluidKey);
        }

        //1.16.5: CraftingHelper.getIngredient(JsonElement). In 1.20.1 Forge patches the vanilla
        //Ingredient.fromJson entry point to dispatch on the "type" key for custom ingredient serializers,
        //so it is the direct equivalent (and the path CrystalIngredient/FluidIngredient rely on here).
        Ingredient input = Ingredient.fromJson(json.get("input"));
        ItemStack output = JsonHelper.getItemStack(json.get("output"), "output");
        float consumptionChance = GsonHelper.getAsFloat(json, "consumptionChance");
        int duration = GsonHelper.getAsInt(json, "duration");

        boolean consumeMultipleFluids = GsonHelper.getAsBoolean(json, "consumeMultipleFluids", false);
        boolean acceptChaliceInput = GsonHelper.getAsBoolean(json, "acceptChaliceInput", true);
        boolean copyNBTToOutputs = GsonHelper.getAsBoolean(json, "copyNBTToOutputs", false);
        return new LiquidInfusion(recipeId, duration, fluidInput, input, output, consumptionChance, consumeMultipleFluids, acceptChaliceInput, copyNBTToOutputs);
    }

    @Override
    public LiquidInfusion fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return LiquidInfusion.read(recipeId, buffer);
    }

    @Override
    public void write(JsonObject object, LiquidInfusion recipe) {
        recipe.write(object);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, LiquidInfusion recipe) {
        recipe.write(buffer);
    }
}
