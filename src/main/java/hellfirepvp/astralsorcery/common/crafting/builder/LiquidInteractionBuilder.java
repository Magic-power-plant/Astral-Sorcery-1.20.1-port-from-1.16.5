/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builder;

import hellfirepvp.astralsorcery.AstralSorcery;
//1.16.5 imported hellfirepvp.astralsorcery.common.crafting.recipe.interaction.InteractionResult here; this
//tree keeps InteractionResult next to the recipe in common/crafting (tracked as TODO_PORT[AS-PKG-01]).
import hellfirepvp.astralsorcery.common.crafting.InteractionResult;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeBuilder;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.serializer.LiquidInteractionSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInteractionBuilder
 * Created by HellFirePvP
 * Date: 29.10.2020 / 21:41
 */
public class LiquidInteractionBuilder extends CustomRecipeBuilder<LiquidInteraction> {

    private final ResourceLocation id;

    private FluidStack reactant1 = FluidStack.EMPTY, reactant2 = FluidStack.EMPTY;
    private float chanceConsume1 = 1F, chanceConsume2 = 1F;
    private int weight = 1;
    private InteractionResult result = null;

    private LiquidInteractionBuilder(ResourceLocation id) {
        this.id = id;
    }

    //1.16.5 signature: builder(ForgeRegistryEntry<?> nameProvider)
    //(1.16.5 common/crafting/builder/LiquidInteractionBuilder.java:44-46). IForgeRegistryEntry and
    //ForgeRegistryEntry were removed in 1.20.1 along with getRegistryName(), so the registry name has to be
    //looked up through the registry. Every 1.16.5 call site passed a Block or an Item, both of which are
    //ItemLike, so the path is resolved through ForgeRegistries.ITEMS on the item form.
    //TODO_PORT[AS-ALT-08]: no 1.20.1 supertype covers "anything with a registry name" any more; if a call
    // site ever needs a non-ItemLike name provider it needs its own overload.
    public static LiquidInteractionBuilder builder(ItemLike nameProvider) {
        return new LiquidInteractionBuilder(AstralSorcery.key(ForgeRegistries.ITEMS.getKey(nameProvider.asItem()).getPath()));
    }

    public static LiquidInteractionBuilder builder(ResourceLocation id) {
        return new LiquidInteractionBuilder(id);
    }

    public LiquidInteractionBuilder setReactant1(FluidStack reactant1) {
        this.reactant1 = reactant1;
        return this;
    }

    public LiquidInteractionBuilder setReactant2(FluidStack reactant2) {
        this.reactant2 = reactant2;
        return this;
    }

    public LiquidInteractionBuilder setChanceConsumeReactant1(float chanceConsume1) {
        this.chanceConsume1 = chanceConsume1;
        return this;
    }

    public LiquidInteractionBuilder setChanceConsumeReactant2(float chanceConsume2) {
        this.chanceConsume2 = chanceConsume2;
        return this;
    }

    public LiquidInteractionBuilder setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public LiquidInteractionBuilder setResult(InteractionResult result) {
        this.result = result;
        return this;
    }

    @Nonnull
    @Override
    protected LiquidInteraction validateAndGet() {
        if (this.reactant1.isEmpty()) {
            throw new IllegalArgumentException("The 1st reactant must not be empty!");
        }
        if (this.reactant2.isEmpty()) {
            throw new IllegalArgumentException("The 2nd reactant must not be empty!");
        }
        if (this.weight <= 0) {
            throw new IllegalArgumentException("Weight has to be positive!");
        }
        if (this.result == null) {
            throw new IllegalArgumentException("A result must be defined!");
        }
        return new LiquidInteraction(this.id,
                this.reactant1, this.chanceConsume1,
                this.reactant2, this.chanceConsume2,
                this.weight, this.result);
    }

    @Override
    protected CustomRecipeSerializer<LiquidInteraction> getSerializer() {
        //1.16.5: RecipeSerializersAS.LIQUID_INTERACTION_SERIALIZER (common/lib). common/lib is not owned by
        //this slice, so the single instance is held on the serializer class itself.
        return LiquidInteractionSerializer.INSTANCE;
    }
}
