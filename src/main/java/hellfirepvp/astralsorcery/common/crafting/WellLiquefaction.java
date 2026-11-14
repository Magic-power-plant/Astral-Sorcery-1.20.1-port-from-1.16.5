/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.crafting.recipe.GatedRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import net.minecraft.core.RegistryAccess;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WellLiquefaction
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:30
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe. Not moved because the package layout is
//a tree-wide decision.
//TODO_PORT[AS-ALT-05]: 1.16.5 extends common/crafting/helper/CustomMatcherRecipe (which supplied getId,
//getType, getSerializer, canFit and the EMPTY results) and returns RecipeTypesAS.TYPE_WELL.getType() /
//RecipeSerializersAS.WELL_LIQUEFACTION_SERIALIZER. This tree implements vanilla Recipe<Container> directly
//and resolves both through the DeferredRegister holders, because RecipeTypesAS here holds vanilla
//RecipeTypes rather than 1.16.5's ResolvingRecipeType. The nested Serializer below has no 1.16.5
//counterpart either: 1.16.5's serializer is common/crafting/serializer/WellRecipeSerializer, which this tree
//also carries (unregistered) - see the marker there.
//The GatedRecipe.Progression implementation was missing here, so the well silently ignored the research gate
//1.16.5 applies (1.16.5 common/crafting/recipe/WellLiquefaction.java:34, 56-60); it is restored below.
public class WellLiquefaction implements Recipe<Container>, GatedRecipe.Progression {

    private final ResourceLocation id;
    private final Ingredient input;
    private final Fluid output;
    private final Color catalystColor;
    private final float productionMultiplier;
    private final float shatterMultiplier;

    public WellLiquefaction(ResourceLocation id, Ingredient input, Fluid output, float productionMultiplier, float shatterMultiplier) {
        this(id, input, output, null, productionMultiplier, shatterMultiplier);
    }

    public WellLiquefaction(ResourceLocation id, Ingredient input, Fluid output, @Nullable Color catalystColor, float productionMultiplier, float shatterMultiplier) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.catalystColor = catalystColor;
        this.productionMultiplier = productionMultiplier;
        this.shatterMultiplier = shatterMultiplier;
    }

    @Nonnull
    @Override
    public ResearchProgression getRequiredProgression() {
        return ResearchProgression.BASIC_CRAFT;
    }

    @Nullable
    public static WellLiquefaction findRecipe(Level level, WellLiquefactionContext context) {
        return level.getRecipeManager()
                .getRecipeFor(RecipeTypesAS.WELL.get(), new SimpleContainer(context.getInput()), level)
                .orElse(null);
    }

    public boolean matches(ItemStack input) {
        return this.input.test(input);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return !container.isEmpty() && matches(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
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
        return RecipeSerializersAS.WELL.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.WELL.get();
    }

    @Nonnull
    public Ingredient getInput() {
        return input;
    }

    @Nonnull
    public Fluid getFluidOutput() {
        return output;
    }

    @Nullable
    public Color getCatalystColor() {
        return catalystColor;
    }

    public float getProductionMultiplier() {
        return productionMultiplier;
    }

    public float getShatterMultiplier() {
        return shatterMultiplier;
    }

    public static class Serializer implements RecipeSerializer<WellLiquefaction> {

        @Override
        public WellLiquefaction fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getNonNull(json, "input"));
            Fluid output = readFluid(GsonHelper.getAsString(json, "output"));
            Color catalystColor = null;
            if (json.has("catalystColor")) {
                catalystColor = new Color(GsonHelper.getAsInt(json, "catalystColor"));
            } else if (json.has("color")) {
                catalystColor = new Color(GsonHelper.getAsInt(json, "color"));
            }
            float productionMultiplier = GsonHelper.getAsFloat(json, "productionMultiplier", 1F);
            float shatterMultiplier = GsonHelper.getAsFloat(json, "shatterMultiplier", 1F);
            return new WellLiquefaction(id, input, output, catalystColor, productionMultiplier, shatterMultiplier);
        }

        @Override
        public WellLiquefaction fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            Fluid output = buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
            boolean hasColor = buffer.readBoolean();
            Color catalystColor = hasColor ? new Color(buffer.readInt()) : null;
            float productionMultiplier = buffer.readFloat();
            float shatterMultiplier = buffer.readFloat();
            return new WellLiquefaction(id, input, output, catalystColor, productionMultiplier, shatterMultiplier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, WellLiquefaction recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, recipe.output);
            buffer.writeBoolean(recipe.catalystColor != null);
            if (recipe.catalystColor != null) {
                buffer.writeInt(recipe.catalystColor.getRGB() & 0xFFFFFF);
            }
            buffer.writeFloat(recipe.productionMultiplier);
            buffer.writeFloat(recipe.shatterMultiplier);
        }

        private static Fluid readFluid(String id) {
            ResourceLocation fluidId = ResourceLocation.tryParse(id);
            if (fluidId == null) {
                return Fluids.EMPTY;
            }
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            return fluid == null ? Fluids.EMPTY : fluid;
        }
    }
}
