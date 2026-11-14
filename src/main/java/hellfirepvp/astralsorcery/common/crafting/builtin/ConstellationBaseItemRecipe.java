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
 * Class: ConstellationBaseItemRecipe
 * Created by HellFirePvP
 * Date: 28.09.2019 / 07:12
 */
//See NBTCopyRecipe: 1.16.5 extended common/crafting/recipe/SimpleAltarRecipe, this tree extends the divergent
//top-level common/crafting/AltarRecipe (TODO_PORT[AS-ALT-03]), so getOutputs(TileAltar) is
//getOutputsForAltar(TileAltar) here.
public class ConstellationBaseItemRecipe extends AltarRecipe {

    private static final String KEY_CONSTELLATION = "constellation";

    private IConstellation constellation = null;

    public ConstellationBaseItemRecipe(ResourceLocation id, AltarType altarType, AltarRecipeGrid grid, List<ItemStack> outputs, int starlightRequired, int craftingTime) {
        super(id, altarType, grid, outputs, starlightRequired, craftingTime);
    }

    public static ConstellationBaseItemRecipe convertToThis(AltarRecipe other) {
        ConstellationBaseItemRecipe recipe = new ConstellationBaseItemRecipe(other.getId(), other.getAltarType(), other.getGrid(), other.getOutputs(), other.getStarlightRequired(), other.getCraftingTime());
        recipe.copyCommonPropertiesFrom(other);
        return recipe;
    }

    @Override
    public void deserializeAdditionalJson(JsonObject recipeObject) throws JsonSyntaxException {
        super.deserializeAdditionalJson(recipeObject);
        //1.16.5: JSONUtils.hasField/getString -> JsonObject#has and GsonHelper.getAsString in 1.20.1, and
        //RegistriesAS.CONSTELLATIONS.getValue(name) -> ConstellationRegistry.getConstellation(name)
        //(common/lib/RegistriesAS is not present in this tree).
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

    public void setConstellation(IConstellation constellation) {
        this.constellation = constellation;
    }

    public IConstellation getConstellation() {
        return constellation;
    }

    //1.16.5 additionally marks this @OnlyIn(Dist.CLIENT) (1.16.5
    //common/crafting/recipe/altar/builtin/ConstellationBaseItemRecipe.java:82-88). It is deliberately not
    //annotated here: this tree's AltarRecipe#getOutputForRender is plain common code and is reached from
    //common/data/journal/JournalPageRecipe, so annotating the override would silently drop the constellation
    //again on a dedicated server instead of merely being unreachable.
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
        //1.16.5: ByteBufUtils.writeOptional(buf, cst, ByteBufUtils::writeRegistryEntry) - writeRegistryEntry
        //resolved the registry through IForgeRegistryEntry, which no longer exists in 1.20.1, so the
        //constellation is identified by its registry name; the wire layout (boolean + ResourceLocation) is
        //the same one ByteBufUtils.writeOptional produced.
        IConstellation constellation = this.getConstellation();
        buffer.writeBoolean(constellation != null);
        if (constellation != null) {
            buffer.writeResourceLocation(constellation.getRegistryName());
        }
    }

    @Override
    public void readRecipeSync(FriendlyByteBuf buffer) {
        super.readRecipeSync(buffer);
        //1.16.5 assigned the result of ByteBufUtils.readOptional unconditionally, i.e. an absent value also
        //cleared a previously set constellation - kept by clearing explicitly in the else branch.
        if (buffer.readBoolean()) {
            this.setConstellation(ConstellationRegistry.getConstellation(buffer.readResourceLocation()));
        } else {
            this.setConstellation(null);
        }
    }
}
