/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.api.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConstellationItemRecipe
 * Created by HellFirePvP
 * Date: 28.09.2019 / 06:48
 */
public class ConstellationItemRecipe extends AltarRecipe {

    private static final String KEY_CONSTELLATION_ATTUNE = "attuned_constellation";
    private static final String KEY_CONSTELLATION_TRAIT = "trait_constellation";

    private IWeakConstellation attunedConstellation = null;
    private IMinorConstellation traitConstellation = null;

    public ConstellationItemRecipe(ResourceLocation id, AltarType altarType, AltarRecipeGrid grid, List<ItemStack> outputs, int starlightRequired, int craftingTime) {
        super(id, altarType, grid, outputs, starlightRequired, craftingTime);
    }

    public static ConstellationItemRecipe convertToThis(AltarRecipe other) {
        ConstellationItemRecipe recipe = new ConstellationItemRecipe(other.getId(), other.getAltarType(), other.getGrid(), other.getOutputs(), other.getStarlightRequired(), other.getCraftingTime());
        recipe.copyCommonPropertiesFrom(other);
        return recipe;
    }

    @Override
    public void deserializeAdditionalJson(JsonObject recipeObject) throws JsonSyntaxException {
        super.deserializeAdditionalJson(recipeObject);
        //1.16.5: JSONUtils.hasField/getString -> JsonObject#has and GsonHelper.getAsString, and
        //RegistriesAS.CONSTELLATIONS.getValue -> ConstellationRegistry.getConstellation.
        if (recipeObject.has(KEY_CONSTELLATION_ATTUNE)) {
            IConstellation constellation = ConstellationRegistry.getConstellation(ResourceLocation.tryParse(GsonHelper.getAsString(recipeObject, KEY_CONSTELLATION_ATTUNE)));
            if (constellation instanceof IWeakConstellation weakConstellation) {
                this.setAttunedConstellation(weakConstellation);
            }
        }
        if (recipeObject.has(KEY_CONSTELLATION_TRAIT)) {
            IConstellation constellation = ConstellationRegistry.getConstellation(ResourceLocation.tryParse(GsonHelper.getAsString(recipeObject, KEY_CONSTELLATION_TRAIT)));
            if (constellation instanceof IMinorConstellation minorConstellation) {
                this.setTraitConstellation(minorConstellation);
            }
        }
    }

    @Override
    public void serializeAdditionalJson(JsonObject recipeObject) {
        super.serializeAdditionalJson(recipeObject);

        if (this.getAttunedConstellation() != null) {
            recipeObject.addProperty(KEY_CONSTELLATION_ATTUNE, this.getAttunedConstellation().getRegistryName().toString());
        }
        if (this.getTraitConstellation() != null) {
            recipeObject.addProperty(KEY_CONSTELLATION_TRAIT, this.getTraitConstellation().getRegistryName().toString());
        }
    }

    public void setAttunedConstellation(IWeakConstellation attunedConstellation) {
        this.attunedConstellation = attunedConstellation;
    }

    public void setTraitConstellation(IMinorConstellation traitConstellation) {
        this.traitConstellation = traitConstellation;
    }

    public IWeakConstellation getAttunedConstellation() {
        return attunedConstellation;
    }

    public IMinorConstellation getTraitConstellation() {
        return traitConstellation;
    }

    //1.16.5 marks this @OnlyIn(Dist.CLIENT); see ConstellationBaseItemRecipe#getOutputForRender for why the
    //annotation is deliberately not carried over in this tree.
    @Override
    @Nonnull
    public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
        ItemStack output = super.getOutputForRender(inventoryContents);
        this.setConstellations(output);
        return output;
    }

    @Override
    @Nonnull
    public List<ItemStack> getOutputsForAltar(AltarBlockEntity altar) {
        List<ItemStack> outputs = super.getOutputsForAltar(altar);
        outputs.forEach(this::setConstellations);
        return outputs;
    }

    private void setConstellations(ItemStack output) {
        if (output.getItem() instanceof ConstellationItem constellationItem) {
            if (this.getAttunedConstellation() != null) {
                constellationItem.setAttunedConstellation(output, this.getAttunedConstellation());
            }
            if (this.getTraitConstellation() != null) {
                constellationItem.setTraitConstellation(output, this.getTraitConstellation());
            }
        }
    }

    @Override
    public void writeRecipeSync(FriendlyByteBuf buffer) {
        super.writeRecipeSync(buffer);
        //1.16.5: two ByteBufUtils.writeOptional(buf, cst, ByteBufUtils::writeRegistryEntry) calls;
        //IForgeRegistryEntry is gone in 1.20.1, so each constellation is identified by its registry name.
        //Wire layout (boolean + ResourceLocation, twice) is unchanged.
        IWeakConstellation attunedConstellation = this.getAttunedConstellation();
        buffer.writeBoolean(attunedConstellation != null);
        if (attunedConstellation != null) {
            buffer.writeResourceLocation(attunedConstellation.getRegistryName());
        }
        IMinorConstellation traitConstellation = this.getTraitConstellation();
        buffer.writeBoolean(traitConstellation != null);
        if (traitConstellation != null) {
            buffer.writeResourceLocation(traitConstellation.getRegistryName());
        }
    }

    @Override
    public void readRecipeSync(FriendlyByteBuf buffer) {
        super.readRecipeSync(buffer);
        //1.16.5 assigned both optionals unconditionally, so an absent value clears the field - the explicit
        //reset below keeps that. The instanceof narrowing is new: 1.16.5's readRegistryEntry was generic and
        //returned the field type directly, while a registry lookup by name here can hand back any
        //IConstellation, so the value is only kept if it actually has the required sub-type.
        this.attunedConstellation = null;
        if (buffer.readBoolean()) {
            IConstellation constellation = ConstellationRegistry.getConstellation(buffer.readResourceLocation());
            if (constellation instanceof IWeakConstellation weakConstellation) {
                this.setAttunedConstellation(weakConstellation);
            }
        }
        this.traitConstellation = null;
        if (buffer.readBoolean()) {
            IConstellation constellation = ConstellationRegistry.getConstellation(buffer.readResourceLocation());
            if (constellation instanceof IMinorConstellation minorConstellation) {
                this.setTraitConstellation(minorConstellation);
            }
        }
    }
}
