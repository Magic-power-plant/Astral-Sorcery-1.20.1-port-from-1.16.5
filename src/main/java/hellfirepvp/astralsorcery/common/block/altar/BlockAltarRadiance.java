/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.altar;

//TODO_PORT[AS-PKG-01]: 1.16.5 keeps the shared base at common/block/tile/BlockAltar; this tree already has it
//at common/block/BlockAltar, so the import below points there instead of moving the class.
import hellfirepvp.astralsorcery.common.block.BlockAltar;
import hellfirepvp.astralsorcery.common.block.base.LargeBlock;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.block.util.VoxelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockAltarRadiance
 * Created by HellFirePvP
 * Date: 12.08.2019 / 21:59
 */
public class BlockAltarRadiance extends BlockAltar implements LargeBlock {

    // 1.16.5: AxisAlignedBB -> 1.20.1: AABB
    private static final AABB PLACEMENT_BOX = new AABB(-1, 0, -1, 1, 1, 1);

    private final VoxelShape shape;

    public BlockAltarRadiance() {
        super(AltarType.RADIANCE);
        this.shape = createShape();
    }

    @Override
    public AABB getBlockSpace() {
        return PLACEMENT_BOX;
    }

    @Nullable
    @Override
    // 1.16.5: BlockItemUseContext -> 1.20.1: BlockPlaceContext; getDefaultState -> defaultBlockState
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.canPlaceAt(context) ? this.defaultBlockState() : null;
    }

    protected VoxelShape createShape() {
        // 1.16.5: Block#makeCuboidShape -> 1.20.1: Block#box
        VoxelShape base = Block.box(0, 0, 0, 16, 4, 16);
        VoxelShape pillar = Block.box(4, 4, 4, 12, 8, 12);
        VoxelShape head = Block.box(0, 8, 0, 16, 16, 16);

        // 1.16.5: IBooleanFunction.OR -> 1.20.1: BooleanOp.OR
        return VoxelUtils.combineAll(BooleanOp.OR, base, pillar, head);
    }

    @Override
    // 1.16.5: IBlockReader/ISelectionContext -> 1.20.1: BlockGetter/CollisionContext
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return this.shape;
    }
}
