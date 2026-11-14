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
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.util.VoxelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockAltarAttunement
 * Created by HellFirePvP
 * Date: 12.08.2019 / 21:59
 */
public class BlockAltarAttunement extends BlockAltar {

    private final VoxelShape shape;

    public BlockAltarAttunement() {
        super(AltarType.ATTUNEMENT);
        this.shape = createShape();
    }

    protected VoxelShape createShape() {
        // 1.16.5: Block#makeCuboidShape -> 1.20.1: Block#box
        VoxelShape base = Block.box(0, 0, 0, 16, 2, 16);
        VoxelShape pillar = Block.box(4, 2, 4, 12, 10, 12);
        VoxelShape head = Block.box(0, 10, 0, 16, 16, 16);

        // 1.16.5: IBooleanFunction.OR -> 1.20.1: BooleanOp.OR
        return VoxelUtils.combineAll(BooleanOp.OR, base, pillar, head);
    }

    @Override
    // 1.16.5: IBlockReader/ISelectionContext -> 1.20.1: BlockGetter/CollisionContext
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return this.shape;
    }
}
