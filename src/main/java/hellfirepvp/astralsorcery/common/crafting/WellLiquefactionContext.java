/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.astralsorcery.common.tile.TileWell;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WellLiquefactionContext
 * Created by HellFirePvP
 * Date: 01.07.2019 / 00:04
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe next to its three sibling contexts
//(BlockTransmutationContext, LiquidInfusionContext, LiquidInteractionContext), which this tree does have
//under common/crafting/recipe; only this one sits at the top level. Not moved because the package layout is
//a tree-wide decision.
public class WellLiquefactionContext extends RecipeCraftingContext<WellLiquefaction, IItemHandler> {

    private final ItemStack input;

    public WellLiquefactionContext(TileWell well) {
        this(well.getCatalyst());
    }

    public WellLiquefactionContext(ItemStack input) {
        this.input = input;
    }

    public ItemStack getInput() {
        return input;
    }
}
