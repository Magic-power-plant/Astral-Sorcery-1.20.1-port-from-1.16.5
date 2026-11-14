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
import hellfirepvp.astralsorcery.common.block.util.VoxelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockAltarDiscovery
 * Created by HellFirePvP
 * Date: 12.08.2019 / 21:58
 */
public class BlockAltarDiscovery extends BlockAltar {

    private final VoxelShape shape;

    public BlockAltarDiscovery() {
        super(AltarType.DISCOVERY);
        this.shape = createShape();
    }

    protected VoxelShape createShape() {
        // 1.16.5: Block#makeCuboidShape -> 1.20.1: Block#box; VoxelShapes#create -> Shapes#box
        VoxelShape base = Block.box(2, 0, 2, 14, 2, 14);
        VoxelShape pillar = Shapes.box(0.25, 0.125, 0.25, 0.75, 9.5 / 16.0, 0.75);
        VoxelShape head = Shapes.box(0, 9.5 / 16.0, 0, 1, 15.5 / 16.0, 1);

        // 1.16.5: IBooleanFunction.OR -> 1.20.1: BooleanOp.OR
        return VoxelUtils.combineAll(BooleanOp.OR, base, pillar, head);
    }

    @Override
    // 1.16.5: IBlockReader/ISelectionContext -> 1.20.1: BlockGetter/CollisionContext
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return this.shape;
    }
}
