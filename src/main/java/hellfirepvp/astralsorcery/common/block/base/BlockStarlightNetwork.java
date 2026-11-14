/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base;

import hellfirepvp.astralsorcery.common.blockentity.base.NetworkBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockNetwork
 * Created by HellFirePvP
 * Date: 03.08.2016 / 21:01
 */
public abstract class BlockStarlightNetwork extends BlockInventory {

    protected BlockStarlightNetwork(Properties builder) {
        super(builder);
    }

    @Override
    // 1.16.5: Block#onReplaced(BlockState, World, BlockPos, BlockState, boolean)
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state != newState) {
            NetworkBlockEntity<?> te = MiscUtils.getBlockEntityAt(worldIn, pos, NetworkBlockEntity.class, true);
            if (te != null) {
                te.onBreak();
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}

