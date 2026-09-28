/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builder;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeBuilder;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.serializer.WellRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WellRecipeBuilder
 * Created by HellFirePvP
 * Date: 07.03.2020 / 17:01
 */
public class WellRecipeBuilder extends CustomRecipeBuilder<WellLiquefaction> {

    private final ResourceLocation id;

    private Ingredient input = Ingredient.EMPTY;
    private Fluid output = Fluids.EMPTY;

    private float productionMultiplier = 0.5F;
    private float shatterMultiplier = 15F;
    private Color catalystColor = Color.WHITE;

    private WellRecipeBuilder(ResourceLocation id) {
        this.id = id;
    }

    //1.16.5 signature: builder(ForgeRegistryEntry<?> nameProvider)
    //(1.16.5 common/crafting/builder/WellRecipeBuilder.java:50-52). IForgeRegistryEntry and
    //ForgeRegistryEntry were removed in 1.20.1 along with getRegistryName(), so the registry name has to be
    //looked up through the registry. Every 1.16.5 call site passed a Block or an Item, both of which are
    //ItemLike, so the path is resolved through ForgeRegistries.ITEMS on the item form.
    //TODO_PORT[AS-ALT-08]: no 1.20.1 supertype covers "anything with a registry name" any more; if a call
    // site ever needs a non-ItemLike name provider it needs its own overload.
    public static WellRecipeBuilder builder(ItemLike nameProvider) {
        return new WellRecipeBuilder(AstralSorcery.key(ForgeRegistries.ITEMS.getKey(nameProvider.asItem()).getPath()));
    }

    public static WellRecipeBuilder builder(ResourceLocation id) {
        return new WellRecipeBuilder(id);
    }

    //1.16.5: IItemProvider -> ItemLike, Ingredient.fromItems -> Ingredient.of in 1.20.1.
    public WellRecipeBuilder setItemInput(ItemLike item) {
        this.input = Ingredient.of(item);
        return this;
    }

    //1.16.5: Tag<Item> - the tag wrapper types were replaced by TagKey<Item> in 1.20.1 and Ingredient.fromTag
    //was renamed to Ingredient.of(TagKey).
    public WellRecipeBuilder setItemInput(TagKey<Item> tag) {
        this.input = Ingredient.of(tag);
        return this;
    }

    public WellRecipeBuilder setItemInput(Ingredient input) {
        this.input = input;
        return this;
    }

    public WellRecipeBuilder setLiquidOutput(Fluid output) {
        this.output = output;
        return this;
    }

    public WellRecipeBuilder color(Color color) {
        this.catalystColor = color;
        return this;
    }

    public WellRecipeBuilder productionMultiplier(float multiplier) {
        this.productionMultiplier = multiplier;
        return this;
    }

    public WellRecipeBuilder shatterMultiplier(float multiplier) {
        this.shatterMultiplier = multiplier;
        return this;
    }

    @Nonnull
    @Override
    protected WellLiquefaction validateAndGet() {
        //1.16.5: Ingredient#hasNoMatchingItems() - renamed to isEmpty() in 1.20.1
        if (this.input.isEmpty()) {
            throw new IllegalArgumentException("No valid item for input found!");
        }
        if (this.output == Fluids.EMPTY) {
            throw new IllegalArgumentException("No output fluid defined!");
        }
        return new WellLiquefaction(this.id, this.input, this.output, this.catalystColor, this.productionMultiplier, this.shatterMultiplier);
    }

    @Override
    protected CustomRecipeSerializer<WellLiquefaction> getSerializer() {
        //1.16.5: RecipeSerializersAS.WELL_LIQUEFACTION_SERIALIZER (common/lib). common/lib is not owned by
        //this slice, so the single instance is held on the serializer class itself.
        return WellRecipeSerializer.INSTANCE;
    }
}
