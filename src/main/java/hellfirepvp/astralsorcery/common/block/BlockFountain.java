/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesWood;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.fountain.FountainBlockEntity;
import hellfirepvp.astralsorcery.common.block.util.VoxelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockFountain
 * Created by HellFirePvP
 * Date: 29.10.2020 / 19:54
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this class at common/block/tile/BlockFountain; this tree already had it at
//common/block/BlockFountain, so it stays here rather than being moved mid-port.
//1.16.5: ContainerBlock -> 1.20.1: BaseEntityBlock
public class BlockFountain extends BaseEntityBlock implements CustomItemBlock {

    private final VoxelShape shape;

    public BlockFountain() {
        super(PropertiesWood.defaultInfusedWood());

        this.shape = createShape();
    }

    //1.16.5: Block.makeCuboidShape -> 1.20.1: Block.box; IBooleanFunction -> BooleanOp
    protected VoxelShape createShape() {
        VoxelShape m1 = Block.box(0, 10, 0, 16, 16, 16);
        VoxelShape m2 = Block.box(4, 6, 4, 12, 10, 12);
        VoxelShape m3 = Block.box(2, 0, 2, 14, 4, 14);
        VoxelShape m4 = Block.box(0, 4, 0, 16, 6, 16);

        return VoxelUtils.combineAll(BooleanOp.OR, m1, m2, m3, m4);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    //1.16.5: BlockRenderType#getRenderType -> 1.20.1: RenderShape#getRenderShape. BaseEntityBlock defaults to
    //INVISIBLE, so the reference's explicit MODEL is still required here.
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    //1.16.5: ContainerBlock#createNewTileEntity(IBlockReader) -> 1.20.1: EntityBlock#newBlockEntity(BlockPos, BlockState)
    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FountainBlockEntity(pos, state);
    }

    //No 1.16.5 counterpart: TileFountain implemented ITickableTileEntity, which 1.20.1 replaced with a
    //BlockEntityTicker handed out by the owning block. This forwards to the very same TileFountain#tick().
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.FOUNTAIN.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof FountainBlockEntity fountain) {
                fountain.tick();
            }
        } : null;
    }
}
