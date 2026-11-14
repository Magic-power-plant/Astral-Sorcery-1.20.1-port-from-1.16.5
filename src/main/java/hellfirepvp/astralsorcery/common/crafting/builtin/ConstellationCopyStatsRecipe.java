/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builtin;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.ItemStackHandlerAS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConstellationCopyStatsRecipe
 * Created by HellFirePvP
 * Date: 06.08.2020 / 18:16
 */
public class ConstellationCopyStatsRecipe extends ConstellationBaseAverageStatsRecipe {

    private static final String KEY_CONSTELLATION_SLOT = "constellationSlot";

    private int constellationSlot = -1;

    public ConstellationCopyStatsRecipe(ResourceLocation id, AltarType altarType, AltarRecipeGrid grid, List<ItemStack> outputs, int starlightRequired, int craftingTime) {
        super(id, altarType, grid, outputs, starlightRequired, craftingTime);
    }

    public static ConstellationCopyStatsRecipe convertToThis(AltarRecipe other) {
        ConstellationCopyStatsRecipe recipe = new ConstellationCopyStatsRecipe(other.getId(), other.getAltarType(), other.getGrid(), other.getOutputs(), other.getStarlightRequired(), other.getCraftingTime());
        recipe.copyCommonPropertiesFrom(other);
        return recipe;
    }

    public void setConstellationSlot(int constellationSlot) {
        this.constellationSlot = constellationSlot;
    }

    public int getConstellationSlot() {
        return constellationSlot;
    }

    @Override
    public void deserializeAdditionalJson(JsonObject recipeObject) throws JsonSyntaxException {
        super.deserializeAdditionalJson(recipeObject);

        //1.16.5: JSONUtils.hasField/getInt -> GsonHelper.isValidNode/getAsInt in 1.20.1.
        if (GsonHelper.isValidNode(recipeObject, KEY_CONSTELLATION_SLOT)) {
            this.constellationSlot = GsonHelper.getAsInt(recipeObject, KEY_CONSTELLATION_SLOT);
        }
    }

    @Override
    public void serializeAdditionalJson(JsonObject recipeObject) {
        super.serializeAdditionalJson(recipeObject);

        if (this.constellationSlot != -1) {
            recipeObject.addProperty(KEY_CONSTELLATION_SLOT, this.constellationSlot);
        }
    }

    @Nonnull
    @Override
    public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
        ItemStack out = super.getOutputForRender(inventoryContents);
        copyConstellation(out, inventoryContents);
        return out;
    }

    @Nonnull
    @Override
    public List<ItemStack> getOutputsForAltar(AltarBlockEntity altar) {
        List<ItemStack> out = super.getOutputsForAltar(altar);
        out.forEach(stack -> copyConstellation(stack, wrapInventory(altar.getInventory())));
        return out;
    }

    //1.16.5 passed TileAltar#getInventory() (a TileInventory, itself Iterable<ItemStack>) straight into the
    //helper; this tree's inventory is an ItemStackHandlerAS, so it is adapted to an Iterable here. The
    //iteration order is the slot order, which is what the 1.16.5 iterator produced as well - important
    //because copyConstellation prioritises by index.
    private static Iterable<ItemStack> wrapInventory(ItemStackHandlerAS inv) {
        return () -> new java.util.Iterator<ItemStack>() {
            private int index = 0;
            @Override public boolean hasNext() { return index < inv.getSlots(); }
            @Override public ItemStack next() { return inv.getStackInSlot(index++); }
        };
    }

    private void copyConstellation(ItemStack out, Iterable<ItemStack> inventoryContents) {
        if (out.getItem() instanceof ConstellationItem) {
            ConstellationItem iOut = (ConstellationItem) out.getItem();
            if (iOut.getAttunedConstellation(out) == null || iOut.getTraitConstellation(out) == null) {
                //Make a prioritizing iterable with the given index, if possible
                if (this.constellationSlot >= 0) {
                    inventoryContents = Iterables.concat(Lists.newArrayList(Iterables.get(inventoryContents, this.constellationSlot, ItemStack.EMPTY)), inventoryContents);
                }
                for (ItemStack stack : inventoryContents) {
                    if (stack.getItem() instanceof ConstellationItem) {
                        if (iOut.getAttunedConstellation(out) == null) {
                            IWeakConstellation c = ((ConstellationItem) stack.getItem()).getAttunedConstellation(stack);
                            if (c != null) {
                                iOut.setAttunedConstellation(out, c);
                            }
                        }
                        if (iOut.getTraitConstellation(out) == null) {
                            IMinorConstellation c = ((ConstellationItem) stack.getItem()).getTraitConstellation(stack);
                            if (c != null) {
                                iOut.setTraitConstellation(out, c);
                            }
                        }
                    }
                }
            }
        }
    }
}
