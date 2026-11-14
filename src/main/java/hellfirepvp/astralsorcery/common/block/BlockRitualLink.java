package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesGlass;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.RitualLinkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Port of 1.16.5 {@code BlockRitualLink}.
 */
public class BlockRitualLink extends BaseEntityBlock implements CustomItemBlock {

    private static final VoxelShape RITUAL_LINK = Shapes.create(6D / 16D, 2D / 16D, 6D / 16D, 10D / 16D, 14D / 16D, 10D / 16D);

    public BlockRitualLink() {
        super(PropertiesGlass.coatedGlass()
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return RITUAL_LINK;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualLinkBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.RITUAL_LINK.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof RitualLinkBlockEntity link) {
                link.tick();
            }
        } : null;
    }
}
