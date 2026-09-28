/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base.template;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockFoliageTemplate
 * Created by HellFirePvP
 * Date: 21.07.2019 / 09:21
 */
public abstract class BlockFoliageTemplate extends Block implements CustomItemBlock, IPlantable {

    public BlockFoliageTemplate(Block.Properties properties) {
        super(properties);
    }

    //1.16.5: IBlockReader -> 1.20.1: BlockGetter
    protected abstract boolean isValidGround(BlockState state, BlockGetter worldIn, BlockPos pos);

    @Override
    //1.16.5: Block#updatePostPlacement(..., IWorld, ...) -> 1.20.1: BlockBehaviour#updateShape(..., LevelAccessor, ...)
    public BlockState updateShape(BlockState state, Direction dir, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
        //1.16.5: BlockState#isValidPosition -> 1.20.1: BlockState#canSurvive
        if (!state.canSurvive(world, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, dir, facingState, world, pos, facingPos);
    }

    @Override
    //1.16.5: Block#isValidPosition(BlockState, IWorldReader, BlockPos) -> 1.20.1: BlockBehaviour#canSurvive(BlockState, LevelReader, BlockPos)
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos blockpos = pos.below(); //1.16.5: BlockPos#down()
        if (state.getBlock() == this) {
            //Forge's IForgeBlockState#canSustainPlant keeps its 1.16.5 signature apart from IBlockReader -> BlockGetter
            return world.getBlockState(blockpos).canSustainPlant(world, blockpos, Direction.UP, this);
        }
        return this.isValidGround(world.getBlockState(blockpos), world, blockpos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, BlockGetter p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }

    @Override
    //1.16.5: IPlantable#getPlant(IBlockReader, BlockPos) -> 1.20.1: IPlantable#getPlant(BlockGetter, BlockPos)
    public BlockState getPlant(BlockGetter world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) {
            return this.defaultBlockState(); //1.16.5: getDefaultState()
        }
        return state;
    }
}
