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
import hellfirepvp.astralsorcery.common.crafting.serializer.LiquidInfusionSerializer;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.blockentity.InfuserBlockEntity;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInfusion
 * Created by HellFirePvP
 * Date: 26.07.2019 / 21:29
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe. Not moved because the package layout is
//a tree-wide decision.
//TODO_PORT[AS-INF-04]: 1.16.5 extends common/crafting/helper/CustomMatcherRecipe (which supplied getId,
//getType, getSerializer, canFit and the empty crafting result) and returns
//RecipeTypesAS.TYPE_INFUSION.getType() / RecipeSerializersAS.LIQUID_INFUSION_SERIALIZER. This tree
//implements vanilla Recipe<Container> directly and resolves both through the DeferredRegister holders,
//because RecipeTypesAS here holds vanilla RecipeTypes rather than 1.16.5's ResolvingRecipeType. The members
//with no 1.16.5 counterpart (findRecipe, the two-argument matches, matches(Container,Level), assemble,
//canCraftInDimensions, getResultItem, getId, createContainer and the nested Serializer shell) are the glue
//that replaces it and are individually annotated below.
public class LiquidInfusion implements Recipe<Container>, GatedRecipe.Progression {

    private final ResourceLocation id;
    private final int craftingTickTime;
    private final Fluid liquidInput;
    private final Ingredient itemInput;
    private final ItemStack output;
    private final float consumptionChance;
    private final boolean consumeMultipleFluids;
    private final boolean acceptChaliceInput;
    private final boolean copyNBTToOutputs;

    public LiquidInfusion(ResourceLocation id, int craftingTickTime, Fluid liquidInput, Ingredient itemInput, ItemStack output,
                          float consumptionChance, boolean consumeMultipleFluids, boolean acceptChaliceInput, boolean copyNBTToOutputs) {
        this.id = id;
        this.craftingTickTime = craftingTickTime;
        this.liquidInput = liquidInput;
        this.itemInput = itemInput;
        this.output = output;
        this.consumptionChance = consumptionChance;
        this.consumeMultipleFluids = consumeMultipleFluids;
        this.acceptChaliceInput = acceptChaliceInput;
        this.copyNBTToOutputs = copyNBTToOutputs;
    }

    @Nullable
    public static LiquidInfusion findRecipe(Level level, InfuserBlockEntity infuser, Player crafter) {
        for (LiquidInfusion recipe : level.getRecipeManager().getAllRecipesFor(RecipeTypesAS.INFUSION.get())) {
            if (recipe.matches(infuser, crafter)) {
                return recipe;
            }
        }
        return null;
    }

    //1.16.5 had exactly one matcher, matches(TileInfuser, PlayerEntity, LogicalSide) - see below. This
    //two-argument form is an addition of this tree; it is called from common/tile/TileInfuser (through
    //findRecipe above) and from the live common/crafting/ActiveLiquidInfusionRecipe, both of which only ever
    //run server-side (common/block/BlockInfuser#use returns before touching the tile on the client), so it
    //forwards to the reference matcher with LogicalSide.SERVER - which restores the 1.16.5 research gate that
    //this tree's version was missing. Its two extra guards - an empty output or an empty input fluid, neither
    //of which a validated recipe can carry - are kept.
    public boolean matches(InfuserBlockEntity infuser, Player crafter) {
        if (crafter == null || this.output.isEmpty() || this.liquidInput == Fluids.EMPTY) {
            return false;
        }
        return this.matches(infuser, crafter, LogicalSide.SERVER);
    }

    //1.16.5 body: 1.16.5 common/crafting/recipe/LiquidInfusion.java:67-88. The fluid check ran through
    //common/util/data/MapStream there: MapStream.of(infuser.getLiquids()).mapKey(pos ->
    //pos.add(infuser.getPos())).allMatch(tpl -> this.liquidInput.equals(tpl.getB())). MapStream is not ported
    //to this tree and common/util/data is not owned by this slice; since the remapped key was never read by
    //the predicate, iterating the values is the identical check. TileInfuser#getLiquids() always reports
    //every liquid offset, so the size comparison the two-argument form used to do was redundant.
    public boolean matches(InfuserBlockEntity infuser, Player crafter, LogicalSide side) {
        if (crafter == null) {
            return false;
        }
        boolean hasProgress;
        if (side.isClient()) {
            hasProgress = this.hasProgressionClient();
        } else {
            hasProgress = this.hasProgressionServer(crafter);
        }
        if (!hasProgress) {
            return false;
        }
        boolean hasFluidInputs = true;
        for (Fluid fluid : infuser.getLiquids().values()) {
            if (!this.liquidInput.equals(fluid)) {
                hasFluidInputs = false;
                break;
            }
        }

        if (!hasFluidInputs) {
            return false;
        }

        return this.itemInput.test(infuser.getItemInput());
    }

    //1.16.5 common/crafting/recipe/LiquidInfusion.java:90-94
    @Nonnull
    @Override
    public ResearchProgression getRequiredProgression() {
        return ResearchProgression.CONSTELLATION;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return !container.isEmpty() && !this.output.isEmpty() && this.itemInput.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemUtils.copyStackWithSize(this.output, this.output.getCount());
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.INFUSER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.INFUSION.get();
    }

    public int getCraftingTickTime() {
        return craftingTickTime;
    }

    @Nonnull
    public Fluid getLiquidInput() {
        return liquidInput;
    }

    @Nonnull
    public Ingredient getItemInput() {
        return itemInput;
    }

    @Nonnull
    public ItemStack getOutput(ItemStack itemInput) {
        return ItemUtils.copyStackWithSize(this.output, this.output.getCount());
    }

    //1.16.5 common/crafting/recipe/LiquidInfusion.java:113-117 marks this client-only; its only caller,
    //common/integration/jei/CategoryInfuser, is a JEI category and therefore client-only as well - exactly
    //the arrangement 1.16.5 had.
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
        return ItemUtils.copyStackWithSize(this.output, this.output.getCount());
    }

    public void onRecipeCompletion(InfuserBlockEntity infuser) {}

    public float getConsumptionChance() {
        return Mth.clamp(consumptionChance, 0F, 1F);
    }

    public boolean doesConsumeMultipleFluids() {
        return consumeMultipleFluids;
    }

    public boolean acceptsChaliceInput() {
        return acceptChaliceInput;
    }

    public boolean doesCopyNBTToOutputs() {
        return copyNBTToOutputs;
    }

    public SimpleContainer createContainer() {
        return new SimpleContainer(this.output.copy());
    }

    public static LiquidInfusion read(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        //1.16.5 used ByteBufUtils.readRegistryEntry, which resolved the value through
        //GameRegistry.findRegistry(IForgeRegistryEntry#getRegistryType). IForgeRegistryEntry is gone in
        //1.20.1, so the registry name is written/read explicitly and resolved against ForgeRegistries.FLUIDS
        //- identical wire content (a ResourceLocation) and identical result.
        Fluid fluidIn = ForgeRegistries.FLUIDS.getValue(ByteBufUtils.readResourceLocation(buffer));
        //1.16.5: Ingredient.read(PacketBuffer) - renamed to Ingredient.fromNetwork in 1.20.1.
        Ingredient itemIn = Ingredient.fromNetwork(buffer);
        ItemStack output = ByteBufUtils.readItemStack(buffer);
        float consumptionChance = buffer.readFloat();
        int duration = buffer.readInt();
        boolean consumeMultiple = buffer.readBoolean();
        boolean acceptChalice = buffer.readBoolean();
        boolean copyNBTToOutputs = buffer.readBoolean();
        return new LiquidInfusion(recipeId, duration, fluidIn, itemIn, output, consumptionChance, consumeMultiple, acceptChalice, copyNBTToOutputs);
    }

    public final void write(FriendlyByteBuf buffer) {
        //See read(): the 1.16.5 ByteBufUtils.writeRegistryEntry is replaced by an explicit registry name.
        ByteBufUtils.writeResourceLocation(buffer, ForgeRegistries.FLUIDS.getKey(this.getLiquidInput()));
        //1.16.5: Ingredient#write(PacketBuffer) - renamed to toNetwork in 1.20.1.
        this.getItemInput().toNetwork(buffer);
        ByteBufUtils.writeItemStack(buffer, this.output);
        buffer.writeFloat(this.getConsumptionChance());
        buffer.writeInt(this.getCraftingTickTime());
        buffer.writeBoolean(this.doesConsumeMultipleFluids());
        buffer.writeBoolean(this.acceptsChaliceInput());
        buffer.writeBoolean(this.doesCopyNBTToOutputs());
    }

    public void write(JsonObject object) {
        //1.16.5: Fluid#getRegistryName() - IForgeRegistryEntry is gone in 1.20.1, the registry is asked instead.
        object.addProperty("fluidInput", ForgeRegistries.FLUIDS.getKey(this.getLiquidInput()).toString());
        //1.16.5: Ingredient#serialize() - renamed to toJson() in 1.20.1.
        object.add("input", this.getItemInput().toJson());
        object.add("output", JsonHelper.serializeItemStack(this.output));
        object.addProperty("consumptionChance", this.getConsumptionChance());
        object.addProperty("duration", this.getCraftingTickTime());
        object.addProperty("consumeMultipleFluids", this.doesConsumeMultipleFluids());
        object.addProperty("acceptChaliceInput", this.acceptsChaliceInput());
        object.addProperty("copyNBTToOutputs", this.doesCopyNBTToOutputs());
    }

    /**
     * 1.16.5 registered common/crafting/serializer/LiquidInfusionSerializer directly (through
     * common/lib/RecipeSerializersAS#LIQUID_INFUSION_SERIALIZER). This tree binds the "astralsorcery:infuser"
     * serializer id through the DeferredRegister in common/lib/RecipeSerializersAS, which needs a nested
     * type to reference, so this shell exists and forwards to the ported serializer. There is exactly one
     * implementation of the format.
     * TODO_PORT[AS-ALT-05]: register LiquidInfusionSerializer itself and drop this shell once
     * common/lib/RecipeSerializersAS exists.
     */
    public static class Serializer implements RecipeSerializer<LiquidInfusion> {

        @Override
        public LiquidInfusion fromJson(ResourceLocation id, JsonObject json) {
            return LiquidInfusionSerializer.INSTANCE.fromJson(id, json);
        }

        @Override
        public LiquidInfusion fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return LiquidInfusionSerializer.INSTANCE.fromNetwork(id, buffer);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LiquidInfusion recipe) {
            LiquidInfusionSerializer.INSTANCE.toNetwork(buffer, recipe);
        }
    }
}
