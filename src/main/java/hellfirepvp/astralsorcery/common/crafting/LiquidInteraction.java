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
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.crafting.serializer.LiquidInteractionSerializer;
import hellfirepvp.astralsorcery.common.blockentity.ChaliceBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInteraction
 * Created by HellFirePvP
 * Date: 29.10.2020 / 20:44
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe and the InteractionResult classes in
//common/crafting/recipe/interaction. Not moved because the package layout is a tree-wide decision.
//TODO_PORT[AS-ALT-05]: 1.16.5 extends common/crafting/helper/CustomMatcherRecipe (which supplied getId,
//getType, getSerializer, canFit and the empty crafting result) and returns
//RecipeTypesAS.TYPE_LIQUID_INTERACTION.getType() / RecipeSerializersAS.LIQUID_INTERACTION_SERIALIZER. This
//tree implements vanilla Recipe<Container> directly and resolves both through the DeferredRegister holders,
//because RecipeTypesAS here holds vanilla RecipeTypes rather than 1.16.5's ResolvingRecipeType. The members
//with no 1.16.5 counterpart (matches(Container,Level), assemble, canCraftInDimensions, getResultItem, getId,
//the two chance getters and the nested Serializer shell) are the glue that replaces it.
public class LiquidInteraction implements Recipe<Container> {

    private static final Random rand = new Random();

    private final ResourceLocation id;
    private final FluidStack reactant1;
    private final FluidStack reactant2;
    private final float chanceConsumeReactant1;
    private final float chanceConsumeReactant2;
    private final int weight;
    private final InteractionResult result;

    public LiquidInteraction(ResourceLocation id, FluidStack reactant1, float chanceConsumeReactant1,
                             FluidStack reactant2, float chanceConsumeReactant2, int weight, InteractionResult result) {
        this.id = id;
        this.reactant1 = reactant1;
        this.chanceConsumeReactant1 = chanceConsumeReactant1;
        this.reactant2 = reactant2;
        this.chanceConsumeReactant2 = chanceConsumeReactant2;
        this.weight = weight;
        this.result = result;
    }

    public boolean matches(FluidStack input1, FluidStack input2) {
        return (input1.containsFluid(this.reactant1) && input2.containsFluid(this.reactant2)) ||
                (input2.containsFluid(this.reactant2) && input1.containsFluid(this.reactant1));
    }

    //Restored 1.16.5's body (1.16.5 common/crafting/recipe/LiquidInteraction.java:74-107). The previous
    //version routed both branches through a private consumeMatched(..) helper and replaced the
    //drain(.., SIMULATE) probes with its own containsFluid(..) check whose tag comparison accepted any input
    //tag when the reactant had none - FluidStack#containsFluid requires the tags to match, so recipes keyed
    //on a tagless fluid could be consumed by a tagged one.
    public boolean consumeInputs(ChaliceBlockEntity chalice1, ChaliceBlockEntity chalice2) {
        FluidStack contained1 = chalice1.getTank().getFluid();
        FluidStack contained2 = chalice2.getTank().getFluid();
        if (!this.matches(contained1, contained2)) {
            return false;
        }
        if (contained1.containsFluid(this.reactant1) && contained2.containsFluid(this.reactant2)) {
            FluidStack drained1 = chalice1.getTank().drain(this.reactant1, IFluidHandler.FluidAction.SIMULATE);
            FluidStack drained2 = chalice2.getTank().drain(this.reactant2, IFluidHandler.FluidAction.SIMULATE);
            if (drained1.containsFluid(this.reactant1) && drained2.containsFluid(this.reactant2)) {
                if (rand.nextFloat() < this.chanceConsumeReactant1) {
                    chalice1.getTank().drain(this.reactant1, IFluidHandler.FluidAction.EXECUTE);
                }
                if (rand.nextFloat() < this.chanceConsumeReactant2) {
                    chalice2.getTank().drain(this.reactant2, IFluidHandler.FluidAction.EXECUTE);
                }
                return true;
            }
        }
        if (contained1.containsFluid(this.reactant2) && contained2.containsFluid(this.reactant1)) {
            FluidStack drained1 = chalice1.getTank().drain(this.reactant2, IFluidHandler.FluidAction.SIMULATE);
            FluidStack drained2 = chalice2.getTank().drain(this.reactant1, IFluidHandler.FluidAction.SIMULATE);
            if (drained1.containsFluid(this.reactant2) && drained2.containsFluid(this.reactant1)) {
                if (rand.nextFloat() < this.chanceConsumeReactant1) {
                    chalice2.getTank().drain(this.reactant1, IFluidHandler.FluidAction.EXECUTE);
                }
                if (rand.nextFloat() < this.chanceConsumeReactant2) {
                    chalice1.getTank().drain(this.reactant2, IFluidHandler.FluidAction.EXECUTE);
                }
                return true;
            }
        }
        return false;
    }

    public FluidStack getReactant1() {
        return reactant1;
    }

    public FluidStack getReactant2() {
        return reactant2;
    }

    //No 1.16.5 counterpart and no caller in this tree either; kept because removing public API another agent
    //may be wiring up is riskier than leaving two trivial getters in place.
    public float getChanceConsumeReactant1() {
        return chanceConsumeReactant1;
    }

    public float getChanceConsumeReactant2() {
        return chanceConsumeReactant2;
    }

    public InteractionResult getResult() {
        return result;
    }

    public int getWeight() {
        return weight;
    }

    @Nullable
    public static LiquidInteraction pickRecipe(Collection<LiquidInteraction> recipes) {
        //Restored 1.16.5's delegation to common/util/MiscUtils#getWeightedRandomEntry (present in this tree
        //with identical cumulative-weight semantics); the previous inline loop duplicated it.
        return MiscUtils.getWeightedRandomEntry(recipes, rand, interaction -> interaction.weight);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
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
        if (this.result instanceof ResultDropItem dropItem) {
            return dropItem.getOutput();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.LIQUID_INTERACTION.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.LIQUID_INTERACTION.get();
    }

    public static LiquidInteraction read(ResourceLocation recipeId, JsonObject json) {
        //1.16.5: JSONUtils.getString -> GsonHelper.getAsString, new ResourceLocation(String) ->
        //ResourceLocation.parse (the single-argument constructor is deprecated in this Forge version and
        //parse throws the very same ResourceLocationException on a malformed id).
        String fluidKey1 = GsonHelper.getAsString(json, "reactant1");
        Fluid reactant1 = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidKey1));
        if (reactant1 == null) {
            throw new JsonSyntaxException("Unknown fluid: " + fluidKey1);
        }
        int amount1 = GsonHelper.getAsInt(json, "reactant1Amount");
        CompoundTag tag1 = null;
        //1.16.5: JSONUtils.hasField -> GsonHelper.isValidNode in 1.20.1
        if (GsonHelper.isValidNode(json, "reactant1Tag")) {
            String jsonTag1 = GsonHelper.getAsString(json, "reactant1Tag");
            try {
                //1.16.5: JsonToNBT.getTagFromJson -> TagParser.parseTag in 1.20.1
                tag1 = TagParser.parseTag(jsonTag1);
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid Json: " + jsonTag1);
            }
        }
        FluidStack r1 = new FluidStack(reactant1, amount1, tag1);

        String fluidKey2 = GsonHelper.getAsString(json, "reactant2");
        Fluid reactant2 = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidKey2));
        if (reactant2 == null) {
            throw new JsonSyntaxException("Unknown fluid: " + fluidKey2);
        }
        int amount2 = GsonHelper.getAsInt(json, "reactant2Amount");
        CompoundTag tag2 = null;
        if (GsonHelper.isValidNode(json, "reactant2Tag")) {
            String jsonTag2 = GsonHelper.getAsString(json, "reactant2Tag");
            try {
                tag2 = TagParser.parseTag(jsonTag2);
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid Json: " + jsonTag2);
            }
        }
        FluidStack r2 = new FluidStack(reactant2, amount2, tag2);

        float chance1 = GsonHelper.getAsFloat(json, "chanceConsumeReactant1");
        float chance2 = GsonHelper.getAsFloat(json, "chanceConsumeReactant2");
        int weight = GsonHelper.getAsInt(json, "weight");

        JsonObject ctResult = GsonHelper.getAsJsonObject(json, "result");
        ResourceLocation id = ResourceLocation.parse(GsonHelper.getAsString(ctResult, "id"));
        InteractionResult result = InteractionResultRegistry.create(id);
        if (result == null) {
            //1.16.5 joined the known keys through org.apache.logging.log4j.util.Strings#join; String.join
            //produces the identical message without pulling in the log4j utility class.
            throw new JsonSyntaxException("Unknown result type: " + id.toString() +
                    "; expected one of " + String.join(", ", InteractionResultRegistry.getKeysAsStrings()));
        }
        JsonObject resultData = GsonHelper.getAsJsonObject(ctResult, "data");
        result.read(resultData);

        return new LiquidInteraction(recipeId, r1, chance1, r2, chance2, weight, result);
    }

    public final void write(JsonObject object) {
        //1.16.5: Fluid#getRegistryName() - IForgeRegistryEntry is gone in 1.20.1, the registry is asked instead.
        object.addProperty("reactant1", ForgeRegistries.FLUIDS.getKey(this.reactant1.getFluid()).toString());
        object.addProperty("reactant1Amount", this.reactant1.getAmount());
        if (this.reactant1.hasTag()) {
            object.addProperty("reactant1Tag", this.reactant1.getTag().toString());
        }
        object.addProperty("reactant2", ForgeRegistries.FLUIDS.getKey(this.reactant2.getFluid()).toString());
        object.addProperty("reactant2Amount", this.reactant2.getAmount());
        if (this.reactant2.hasTag()) {
            object.addProperty("reactant2Tag", this.reactant2.getTag().toString());
        }
        object.addProperty("chanceConsumeReactant1", this.chanceConsumeReactant1);
        object.addProperty("chanceConsumeReactant2", this.chanceConsumeReactant2);
        object.addProperty("weight", this.weight);

        JsonObject ctResult = new JsonObject();
        ctResult.addProperty("id", this.result.getId().toString());
        JsonObject resultObj = new JsonObject();
        this.result.write(resultObj);
        ctResult.add("data", resultObj);
        object.add("result", ctResult);
    }

    @Nullable
    public static LiquidInteraction read(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        //1.16.5 used ByteBufUtils.read/writeFluidStack. This tree's common/util/data/ByteBufUtils has no
        //FluidStack helpers; Forge's FriendlyByteBuf extension methods readFluidStack/writeFluidStack encode
        //the very same (fluid, amount, tag) triple, so they stand in for them on both ends.
        FluidStack reactant1 = buffer.readFluidStack();
        FluidStack reactant2 = buffer.readFluidStack();
        float chanceConsumeReactant1 = buffer.readFloat();
        float chanceConsumeReactant2 = buffer.readFloat();
        int weight = buffer.readInt();
        ResourceLocation key = ResourceLocation.parse(ByteBufUtils.readString(buffer));
        InteractionResult result = InteractionResultRegistry.create(key);
        if (result == null) {
            return null;
        }
        result.read(buffer);
        return new LiquidInteraction(recipeId, reactant1, chanceConsumeReactant1, reactant2, chanceConsumeReactant2, weight, result);
    }

    public final void write(FriendlyByteBuf buffer) {
        //See read(): Forge's FriendlyByteBuf FluidStack extensions replace ByteBufUtils.write/readFluidStack.
        buffer.writeFluidStack(this.reactant1);
        buffer.writeFluidStack(this.reactant2);
        buffer.writeFloat(this.chanceConsumeReactant1);
        buffer.writeFloat(this.chanceConsumeReactant2);
        buffer.writeInt(this.weight);
        ByteBufUtils.writeString(buffer, this.result.getId().toString());
        this.result.write(buffer);
    }

    /**
     * 1.16.5 registered common/crafting/serializer/LiquidInteractionSerializer directly (through
     * common/lib/RecipeSerializersAS#LIQUID_INTERACTION_SERIALIZER). This tree binds the
     * "astralsorcery:liquid_interaction" serializer id through the DeferredRegister in
     * common/lib/RecipeSerializersAS, which needs a nested type to reference, so this shell exists and
     * forwards to the ported serializer. There is exactly one implementation of the format.
     * TODO_PORT[AS-ALT-05]: register LiquidInteractionSerializer itself and drop this shell once
     * common/lib/RecipeSerializersAS exists.
     */
    public static class Serializer implements RecipeSerializer<LiquidInteraction> {

        @Override
        public LiquidInteraction fromJson(ResourceLocation id, JsonObject json) {
            return LiquidInteractionSerializer.INSTANCE.fromJson(id, json);
        }

        @Override
        public LiquidInteraction fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            //1.16.5 let the null from LiquidInteraction.read propagate
            //(1.16.5 common/crafting/recipe/LiquidInteraction.java:215) because CustomRecipeSerializer#read
            //was declared @Nullable. Vanilla's RecipeManager dereferences whatever fromNetwork returns, so
            //the unknown-result case is turned into an explicit failure here rather than a later NPE.
            LiquidInteraction recipe = LiquidInteractionSerializer.INSTANCE.fromNetwork(id, buffer);
            if (recipe == null) {
                throw new IllegalArgumentException("Unknown liquid interaction result type for recipe " + id);
            }
            return recipe;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LiquidInteraction recipe) {
            LiquidInteractionSerializer.INSTANCE.toNetwork(buffer, recipe);
        }
    }
}
