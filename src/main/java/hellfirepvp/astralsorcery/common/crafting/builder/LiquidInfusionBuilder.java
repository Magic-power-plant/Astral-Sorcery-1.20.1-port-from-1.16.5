/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builder;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeBuilder;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.serializer.LiquidInfusionSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInfusionBuilder
 * Created by HellFirePvP
 * Date: 07.03.2020 / 17:02
 */
public class LiquidInfusionBuilder extends CustomRecipeBuilder<LiquidInfusion> {

    private final ResourceLocation id;

    private Fluid liquidInput = null;
    private Ingredient itemInput = Ingredient.EMPTY;
    private ItemStack output = ItemStack.EMPTY;
    private int craftingTickTime = 200;

    private float consumptionChance = 0.3F;
    private boolean consumeMultipleFluids = false;
    private boolean acceptChaliceInput = true;
    private boolean copyNBTToOutputs = false;

    private LiquidInfusionBuilder(ResourceLocation id) {
        this.id = id;
    }

    //1.16.5 signature: builder(ForgeRegistryEntry<?> nameProvider)
    //(1.16.5 common/crafting/builder/LiquidInfusionBuilder.java:53-55). IForgeRegistryEntry and
    //ForgeRegistryEntry were removed in 1.20.1 along with getRegistryName(), so the registry name has to be
    //looked up through the registry. Every 1.16.5 call site passed a Block or an Item, both of which are
    //ItemLike, so the path is resolved through ForgeRegistries.ITEMS on the item form.
    //TODO_PORT[AS-ALT-08]: no 1.20.1 supertype covers "anything with a registry name" any more; if a call
    // site ever needs a non-ItemLike name provider it needs its own overload.
    public static LiquidInfusionBuilder builder(ItemLike nameProvider) {
        return new LiquidInfusionBuilder(AstralSorcery.key(ForgeRegistries.ITEMS.getKey(nameProvider.asItem()).getPath()));
    }

    public static LiquidInfusionBuilder builder(ResourceLocation id) {
        return new LiquidInfusionBuilder(id);
    }

    public LiquidInfusionBuilder setLiquidInput(Fluid liquidInput) {
        this.liquidInput = liquidInput;
        return this;
    }

    //1.16.5: IItemProvider -> ItemLike, Ingredient.fromItems -> Ingredient.of in 1.20.1.
    public LiquidInfusionBuilder setItemInput(ItemLike item) {
        this.itemInput = Ingredient.of(item);
        return this;
    }

    //1.16.5: ITag.INamedTag<Item> - the tag wrapper types were replaced by TagKey<Item> in 1.20.1 and
    //Ingredient.fromTag was renamed to Ingredient.of(TagKey).
    public LiquidInfusionBuilder setItemInput(TagKey<Item> tag) {
        this.itemInput = Ingredient.of(tag);
        return this;
    }

    public LiquidInfusionBuilder setItemInput(Ingredient input) {
        this.itemInput = input;
        return this;
    }

    public LiquidInfusionBuilder setOutput(ItemLike output) {
        return this.setOutput(new ItemStack(output));
    }

    public LiquidInfusionBuilder setOutput(ItemStack output) {
        this.output = output.copy();
        return this;
    }

    public LiquidInfusionBuilder multiplyDuration(float multiplier) {
        this.craftingTickTime *= multiplier;
        return this;
    }

    public LiquidInfusionBuilder setDuration(int craftingTickTime) {
        this.craftingTickTime = craftingTickTime;
        return this;
    }

    public LiquidInfusionBuilder setFluidConsumptionChance(float consumptionChance) {
        this.consumptionChance = consumptionChance;
        return this;
    }

    public LiquidInfusionBuilder setConsumeMultipleFluids(boolean consumeMultipleFluids) {
        this.consumeMultipleFluids = consumeMultipleFluids;
        return this;
    }

    public LiquidInfusionBuilder setAcceptChaliceInput(boolean acceptChaliceInput) {
        this.acceptChaliceInput = acceptChaliceInput;
        return this;
    }

    public LiquidInfusionBuilder setCopyNBTToOutputs(boolean copyNBTToOutputs) {
        this.copyNBTToOutputs = copyNBTToOutputs;
        return this;
    }

    @Nonnull
    @Override
    protected LiquidInfusion validateAndGet() {
        if (this.liquidInput == null) {
            throw new IllegalArgumentException("No fluid input defined!");
        }
        //1.16.5: Ingredient#hasNoMatchingItems() - renamed to isEmpty() in 1.20.1
        if (this.itemInput.isEmpty()) {
            throw new IllegalArgumentException("No valid item for input found!");
        }
        if (this.output.isEmpty()) {
            throw new IllegalArgumentException("No output item defined!");
        }
        if (this.craftingTickTime <= 0) {
            throw new IllegalArgumentException("No duration defined!");
        }
        return new LiquidInfusion(this.id, craftingTickTime, liquidInput, itemInput, output, consumptionChance, consumeMultipleFluids, acceptChaliceInput, copyNBTToOutputs);
    }

    @Override
    protected CustomRecipeSerializer<LiquidInfusion> getSerializer() {
        //1.16.5: RecipeSerializersAS.LIQUID_INFUSION_SERIALIZER (common/lib). common/lib is not owned by this
        //slice, so the single instance is held on the serializer class itself.
        return LiquidInfusionSerializer.INSTANCE;
    }
}
