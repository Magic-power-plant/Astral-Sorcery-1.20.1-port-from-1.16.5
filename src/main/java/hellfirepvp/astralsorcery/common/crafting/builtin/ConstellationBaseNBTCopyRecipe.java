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
import hellfirepvp.astralsorcery.common.constellation.ConstellationBaseItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
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
 * Class: ConstellationBaseNBTCopyRecipe
 * Created by HellFirePvP
 * Date: 26.04.2020 / 09:49
 */
public class ConstellationBaseNBTCopyRecipe extends NBTCopyRecipe {

    private static final String KEY_CONSTELLATION = "constellation";

    private IConstellation constellation = null;

    public ConstellationBaseNBTCopyRecipe(ResourceLocation id, AltarType altarType, AltarRecipeGrid grid, List<ItemStack> outputs, int starlightRequired, int craftingTime) {
        super(id, altarType, grid, outputs, starlightRequired, craftingTime);
    }

    public static ConstellationBaseNBTCopyRecipe convertToThis(AltarRecipe other) {
        ConstellationBaseNBTCopyRecipe recipe = new ConstellationBaseNBTCopyRecipe(other.getId(), other.getAltarType(), other.getGrid(), other.getOutputs(), other.getStarlightRequired(), other.getCraftingTime());
        recipe.copyCommonPropertiesFrom(other);
        return recipe;
    }

    @Override
    public void deserializeAdditionalJson(JsonObject recipeObject) throws JsonSyntaxException {
        super.deserializeAdditionalJson(recipeObject);
        //See ConstellationBaseItemRecipe: JSONUtils -> GsonHelper and RegistriesAS.CONSTELLATIONS ->
        //ConstellationRegistry.
        if (recipeObject.has(KEY_CONSTELLATION)) {
            ResourceLocation constellationName = ResourceLocation.tryParse(GsonHelper.getAsString(recipeObject, KEY_CONSTELLATION));
            IConstellation constellation = ConstellationRegistry.getConstellation(constellationName);
            if (constellation != null) {
                this.setConstellation(constellation);
            }
        }
    }

    @Override
    public void serializeAdditionalJson(JsonObject recipeObject) {
        super.serializeAdditionalJson(recipeObject);

        if (this.getConstellation() != null) {
            recipeObject.addProperty(KEY_CONSTELLATION, this.getConstellation().getRegistryName().toString());
        }
    }

    public ConstellationBaseNBTCopyRecipe setConstellation(IConstellation constellation) {
        this.constellation = constellation;
        return this;
    }

    public IConstellation getConstellation() {
        return constellation;
    }

    //1.16.5 marks this @OnlyIn(Dist.CLIENT); see ConstellationBaseItemRecipe#getOutputForRender for why the
    //annotation is deliberately not carried over in this tree.
    @Override
    @Nonnull
    public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
        ItemStack out = super.getOutputForRender(inventoryContents);
        setConstellations(out);
        return out;
    }

    @Override
    @Nonnull
    public List<ItemStack> getOutputsForAltar(AltarBlockEntity altar) {
        List<ItemStack> out = super.getOutputsForAltar(altar);
        out.forEach(this::setConstellations);
        return out;
    }

    private void setConstellations(ItemStack out) {
        if (out.getItem() instanceof ConstellationBaseItem constellationBaseItem) {
            if (this.getConstellation() != null) {
                constellationBaseItem.setConstellation(out, this.getConstellation());
            }
        }
    }

    @Override
    public void writeRecipeSync(FriendlyByteBuf buffer) {
        super.writeRecipeSync(buffer);
        //See ConstellationBaseItemRecipe#writeRecipeSync: ByteBufUtils.writeOptional +
        //writeRegistryEntry -> boolean + registry name, IForgeRegistryEntry being gone in 1.20.1.
        IConstellation constellation = this.getConstellation();
        buffer.writeBoolean(constellation != null);
        if (constellation != null) {
            buffer.writeResourceLocation(constellation.getRegistryName());
        }
    }

    @Override
    public void readRecipeSync(FriendlyByteBuf buffer) {
        super.readRecipeSync(buffer);
        //1.16.5 assigned the optional unconditionally, so an absent value clears the field.
        if (buffer.readBoolean()) {
            this.setConstellation(ConstellationRegistry.getConstellation(buffer.readResourceLocation()));
        } else {
            this.setConstellation(null);
        }
    }
}
