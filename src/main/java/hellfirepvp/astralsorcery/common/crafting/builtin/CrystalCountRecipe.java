/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builtin;

import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CrystalCountRecipe
 * Created by HellFirePvP
 * Date: 28.09.2019 / 08:33
 */
//1.16.5's parent chain rooted in common/crafting/recipe/SimpleAltarRecipe; here it roots in the divergent
//top-level common/crafting/AltarRecipe (TODO_PORT[AS-ALT-03]), hence getOutputs(TileAltar) is
//getOutputsForAltar(TileAltar).
public class CrystalCountRecipe extends ConstellationBaseAverageStatsRecipe {

    public CrystalCountRecipe(ResourceLocation id, AltarType altarType, AltarRecipeGrid grid, List<ItemStack> outputs, int starlightRequired, int craftingTime) {
        super(id, altarType, grid, outputs, starlightRequired, craftingTime);
    }

    public static CrystalCountRecipe convertToThis(AltarRecipe other) {
        CrystalCountRecipe recipe = new CrystalCountRecipe(other.getId(), other.getAltarType(), other.getGrid(), other.getOutputs(), other.getStarlightRequired(), other.getCraftingTime());
        recipe.copyCommonPropertiesFrom(other);
        return recipe;
    }

    @Nonnull
    @Override
    public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
        ItemStack out = super.getOutputForRender(inventoryContents);
        setAmount(out);
        return out;
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputsForAltar(AltarBlockEntity altar) {
        List<ItemStack> out = super.getOutputsForAltar(altar);
        out.forEach(this::setAmount);
        return out;
    }

    private void setAmount(ItemStack out) {
        if (out.getItem() instanceof CrystalAttributeItem) {
            CrystalAttributes attr = ((CrystalAttributeItem) out.getItem()).getAttributes(out);
            if (attr != null && !attr.isEmpty()) {
                out.setCount(CrystalCalculations.getSizeCraftingAmount(attr));
            }
        }
    }
}
