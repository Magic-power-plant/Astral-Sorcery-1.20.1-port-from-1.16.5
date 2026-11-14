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
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.ItemStackHandlerAS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConstellationBaseAverageStatsRecipe
 * Created by HellFirePvP
 * Date: 28.09.2019 / 07:19
 */
public class ConstellationBaseAverageStatsRecipe extends ConstellationBaseItemRecipe {

    public ConstellationBaseAverageStatsRecipe(ResourceLocation id, AltarType altarType, AltarRecipeGrid grid, List<ItemStack> outputs, int starlightRequired, int craftingTime) {
        super(id, altarType, grid, outputs, starlightRequired, craftingTime);
    }

    public static ConstellationBaseAverageStatsRecipe convertToThis(AltarRecipe other) {
        ConstellationBaseAverageStatsRecipe recipe = new ConstellationBaseAverageStatsRecipe(other.getId(), other.getAltarType(), other.getGrid(), other.getOutputs(), other.getStarlightRequired(), other.getCraftingTime());
        recipe.copyCommonPropertiesFrom(other);
        return recipe;
    }

    @Override
    @Nonnull
    public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
        ItemStack out = super.getOutputForRender(inventoryContents);
        setStats(out, inventoryContents);
        return out;
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputsForAltar(AltarBlockEntity altar) {
        List<ItemStack> out = super.getOutputsForAltar(altar);
        out.forEach(stack -> setStats(stack, wrapInventory(altar.getInventory())));
        return out;
    }

    //1.16.5 passed TileAltar#getInventory() (a TileInventory, itself Iterable<ItemStack>) straight into the
    //helper (1.16.5 common/crafting/recipe/altar/builtin/ConstellationBaseAverageStatsRecipe.java:52); this
    //tree's inventory is an ItemStackHandlerAS, so it is adapted to an Iterable over the slots in slot order
    //- the same order 1.16.5's iterator produced.
    private static Iterable<ItemStack> wrapInventory(ItemStackHandlerAS inv) {
        return () -> new java.util.Iterator<ItemStack>() {
            private int index = 0;
            @Override public boolean hasNext() { return index < inv.getSlots(); }
            @Override public ItemStack next() { return inv.getStackInSlot(index++); }
        };
    }

    private void setStats(ItemStack out, Iterable<ItemStack> inventoryContents) {
        if (!(out.getItem() instanceof CrystalAttributeItem)) {
            return;
        }

        int count = 0;
        CrystalAttributes.Builder builder = CrystalAttributes.Builder.newBuilder(true);
        for (ItemStack stack : inventoryContents) {
            if (stack.getItem() instanceof CrystalAttributeItem) {
                CrystalAttributes attr = ((CrystalAttributeItem) stack.getItem()).getAttributes(stack);
                if (attr != null) {
                    builder.addAll(attr);
                    count++;
                }
            }
        }

        CrystalAttributes attr = builder.buildAverage(count);
        if (!attr.isEmpty()) {
            ((CrystalAttributeItem) out.getItem()).setAttributes(out, attr);
        }
    }
}
