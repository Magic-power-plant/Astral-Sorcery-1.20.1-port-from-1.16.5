package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesWood;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.TileTelescope;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Port of 1.16.5 {@code BlockTelescope}. The block itself is invisible and rendered by its block entity renderer;
 * the upper half is occupied by a {@link BlockStructural} of type TELESCOPE.
 */
public class BlockTelescope extends BaseEntityBlock implements CustomItemBlock {

    private static final VoxelShape TELESCOPE = Shapes.create(1D / 16D, 0D / 16D, 1D / 16D, 15D / 16D, 32D / 16D, 15D / 16D);

    public BlockTelescope() {
        super(PropertiesWood.defaultInfusedWood()
                .noOcclusion());
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
                RenderingUtils.playBlockBreakParticles(pos.above(), BlocksAS.TELESCOPE.get().defaultBlockState(), BlocksAS.TELESCOPE.get().defaultBlockState());
                return false;
            }
        });
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return TELESCOPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (world.isClientSide()) {
            AstralSorcery.getProxy().openGui(player, GuiType.TELESCOPE, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        world.setBlock(pos.above(), BlocksAS.STRUCTURAL.get().defaultBlockState().setValue(BlockStructural.BLOCK_TYPE, BlockStructural.BlockType.TELESCOPE), Block.UPDATE_ALL);
        super.setPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (world.isEmptyBlock(pos.above())) {
            world.removeBlock(pos, isMoving);
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // VERIFY_PORT[AS-MCH-05]: 1.16.5 renders the telescope through its TESR (RenderTelescope). The target renderer is
        // still a stub, so the block is invisible until AS-CLI-04 lands.
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTelescope(pos, state);
    }
}
