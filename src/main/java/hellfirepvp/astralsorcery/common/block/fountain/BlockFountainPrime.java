/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.fountain;

//TODO_PORT[AS-PKG-01]: 1.16.5 keeps BlockFountain at common/block/tile/BlockFountain; this tree already has it at
//common/block/BlockFountain, so the import below points there instead of moving the class.
import hellfirepvp.astralsorcery.common.block.BlockFountain;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.crafting.nojson.fountain.FountainEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockFountainPrime
 * Created by HellFirePvP
 * Date: 31.10.2020 / 16:32
 */
public abstract class BlockFountainPrime extends Block implements CustomItemBlock {

    public BlockFountainPrime() {
        super(PropertiesMarble.defaultMarble()
                .noOcclusion()); //1.16.5: AbstractBlock.Properties#notSolid()
    }

    @Nonnull
    public abstract FountainEffect<?> provideEffect();

    @Override
    //1.16.5: Block#updatePostPlacement(..., IWorld, ...) -> 1.20.1: BlockBehaviour#updateShape(..., LevelAccessor, ...)
    public BlockState updateShape(BlockState state, Direction placedAgainst, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        if (!this.canSurvive(state, world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    //1.16.5: Block#isValidPosition(BlockState, IWorldReader, BlockPos) -> 1.20.1: BlockBehaviour#canSurvive(BlockState, LevelReader, BlockPos)
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.above()).getBlock() instanceof BlockFountain; //1.16.5: BlockPos#up()
    }
}
