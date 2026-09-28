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
import hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WellRecipeSerializer
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:29
 */
public class WellRecipeSerializer extends CustomRecipeSerializer<WellLiquefaction> {

    //1.16.5 held the single instance in common/lib/RecipeSerializersAS as WELL_LIQUEFACTION_SERIALIZER and
    //registered it from RegistryRecipeSerializers. common/lib is not owned by this slice, and this tree's
    //common/crafting/RecipeSerializersAS already binds the "astralsorcery:lightwell" serializer id to the
    //divergent WellLiquefaction.Serializer, so the instance lives here and is not registered yet.
    //TODO_PORT[AS-ALT-05]: move to common/lib/RecipeSerializersAS#WELL_LIQUEFACTION_SERIALIZER and register
    // it instead of the nested WellLiquefaction.Serializer.
    public static final WellRecipeSerializer INSTANCE = new WellRecipeSerializer();

    public WellRecipeSerializer() {
        //1.16.5: RecipeSerializersAS.WELL_LIQUEFACTION == AstralSorcery.key("lightwell").
        //This tree registers the same serializer under both "well" and "lightwell"; LIGHTWELL is the id
        //that matches 1.16.5 and the id the shipped data/astralsorcery/recipes/lightwell/*.json use.
        super(RecipeSerializersAS.LIGHTWELL.getId());
    }

    @Override
    public WellLiquefaction fromJson(ResourceLocation recipeId, JsonObject json) {
        //1.16.5: Ingredient.deserialize(JsonElement) - renamed to Ingredient.fromJson in 1.20.1.
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
        String fluidKey = GsonHelper.getAsString(json, "output");
        //1.16.5: new ResourceLocation(String). The single-argument constructor is deprecated in this
        //Forge version; ResourceLocation.parse has identical (throwing) behaviour for malformed ids.
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidKey));
        if (fluid == null) {
            throw new JsonSyntaxException("Unknown fluid: " + fluidKey);
        }
        float productionMultiplier = GsonHelper.getAsFloat(json, "productionMultiplier");
        float shatterMultiplier = GsonHelper.getAsFloat(json, "shatterMultiplier");
        Color color = null;
        if (json.has("color")) {
            color = JsonHelper.getColor(json, "color");
        }
        return new WellLiquefaction(recipeId, input, fluid, color, productionMultiplier, shatterMultiplier);
    }

    @Override
    public WellLiquefaction fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        //1.16.5: Ingredient.read(PacketBuffer) - renamed to Ingredient.fromNetwork in 1.20.1.
        Ingredient input = Ingredient.fromNetwork(buffer);
        //1.16.5 used ByteBufUtils.readRegistryEntry, which resolved the value through
        //GameRegistry.findRegistry(IForgeRegistryEntry#getRegistryType). IForgeRegistryEntry is gone in
        //1.20.1, so the registry name is written/read explicitly and resolved against ForgeRegistries.FLUIDS
        //- identical wire content (a ResourceLocation) and identical result.
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(ByteBufUtils.readResourceLocation(buffer));
        float shatter = buffer.readFloat();
        float production = buffer.readFloat();
        Color color = ByteBufUtils.readOptional(buffer, buf -> new Color(buf.readInt(), true));
        return new WellLiquefaction(recipeId, input, fluid, color, production, shatter);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, WellLiquefaction recipe) {
        //1.16.5: Ingredient#write(PacketBuffer) - renamed to toNetwork in 1.20.1.
        recipe.getInput().toNetwork(buffer);
        //See fromNetwork(): the 1.16.5 ByteBufUtils.writeRegistryEntry is replaced by an explicit registry name.
        ByteBufUtils.writeResourceLocation(buffer, ForgeRegistries.FLUIDS.getKey(recipe.getFluidOutput()));
        buffer.writeFloat(recipe.getShatterMultiplier());
        buffer.writeFloat(recipe.getProductionMultiplier());
        ByteBufUtils.writeOptional(buffer, recipe.getCatalystColor(), (buf, color) -> buf.writeInt(color.getRGB()));
    }

    @Override
    public void write(JsonObject object, WellLiquefaction recipe) {
        //1.16.5: Ingredient#serialize() - renamed to toJson() in 1.20.1.
        object.add("input", recipe.getInput().toJson());
        //1.16.5: Fluid#getRegistryName() - IForgeRegistryEntry is gone in 1.20.1, the registry is asked instead.
        object.addProperty("output", ForgeRegistries.FLUIDS.getKey(recipe.getFluidOutput()).toString());
        object.addProperty("productionMultiplier", recipe.getProductionMultiplier());
        object.addProperty("shatterMultiplier", recipe.getShatterMultiplier());
        object.addProperty("color", recipe.getCatalystColor() == null ? Color.WHITE.getRGB() : recipe.getCatalystColor().getRGB());
    }
}
