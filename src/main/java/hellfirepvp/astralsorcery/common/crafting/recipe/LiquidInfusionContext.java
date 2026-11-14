/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.astralsorcery.common.blockentity.infusion.InfuserBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandler;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInfusionContext
 * Created by HellFirePvP
 * Date: 26.07.2019 / 21:29
 */
//The infuser field/ctor/getter were typed LiquidInfusion here, i.e. the recipe rather than the tile; 1.16.5
//types all three TileInfuser (1.16.5 common/crafting/recipe/LiquidInfusionContext.java:26-38) and the class
//is only referenced from within crafting/recipe, so the reference type is restored.
//1.16.5's recipe type parameter is its common/crafting/recipe/LiquidInfusion; this tree's live infusion
//recipe is the divergent top-level common/crafting/LiquidInfusion (TODO_PORT[AS-INF-04]).
public class LiquidInfusionContext extends RecipeCraftingContext<LiquidInfusion, IItemHandler> {

    private final InfuserBlockEntity infuser;
    private final Player crafter;
    private final LogicalSide side;

    public LiquidInfusionContext(InfuserBlockEntity infuser, Player crafter, LogicalSide side) {
        this.infuser = infuser;
        this.crafter = crafter;
        this.side = side;
    }

    public InfuserBlockEntity getInfuser() {
        return infuser;
    }

    public Player getCrafter() {
        return crafter;
    }

    public LogicalSide getSide() {
        return side;
    }
}
