/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.helper.RecipeCraftingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTransmutationContext
 * Created by HellFirePvP
 * Date: 10.10.2019 / 19:28
 */
//1.16.5's IWorld is LevelAccessor in 1.20.1; the field, ctor parameter and getter keep the reference's
//"world" naming. The recipe type parameter is this tree's top-level common/crafting/BlockTransmutation,
//which 1.16.5 keeps in this very package (TODO_PORT[AS-PKG-01]).
public class BlockTransmutationContext extends RecipeCraftingContext<BlockTransmutation, IItemHandler> {

    private final LevelAccessor world;
    private final BlockPos pos;
    private final BlockState state;
    private final IWeakConstellation constellation;

    public BlockTransmutationContext(LevelAccessor world, BlockPos pos, BlockState state, IWeakConstellation constellation) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.constellation = constellation;
    }

    public LevelAccessor getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public IWeakConstellation getConstellation() {
        return constellation;
    }
}
