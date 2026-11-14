package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.base.LargeBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMisc;
import hellfirepvp.astralsorcery.common.container.ContainerProviders;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.observation.ObservatoryBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

// Reference: 1.16.5 BlockObservatory (ContainerBlock + LargeBlock + CustomItemBlock).
// 1.16.5 harvestTool(PICKAXE)/harvestLevel(1) map to the minecraft:mineable/pickaxe and needs_stone_tool block tags in 1.20.1.
// VERIFY_PORT[AS-MCH-05]: the block keeps the 1.16.5 ContainerBlock default render shape (INVISIBLE); it is only visible once RenderObservatory (AS-CLI-04) renders the tile.
public class BlockObservatory extends BaseEntityBlock implements LargeBlock, CustomItemBlock {

    private static final AABB PLACEMENT_BOX = new AABB(-1, 0, -1, 1, 3, 1);

    public BlockObservatory() {
        super(PropertiesMisc.defaultGoldMachinery()
                .noOcclusion()
                .strength(3F, 4F));
    }

    @Override
    public AABB getBlockSpace() {
        return PLACEMENT_BOX;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.canPlaceAt(context) ? this.defaultBlockState() : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!worldIn.isClientSide()) {
            ObservatoryBlockEntity observatory = MiscUtils.getBlockEntityAt(worldIn, pos, ObservatoryBlockEntity.class, false);
            if (observatory != null && observatory.isUsable() && !player.isShiftKeyDown()) {
                Entity entity = observatory.findRideableObservatoryEntity();
                if (entity != null) {
                    if (player.getVehicle() != entity) {
                        player.startRiding(entity);
                    }
                    if (player instanceof ServerPlayer serverPlayer) {
                        ContainerProviders.ObservatoryProvider provider = new ContainerProviders.ObservatoryProvider(pos);
                        NetworkHooks.openScreen(serverPlayer, provider, provider::writeScreenOpeningData);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ObservatoryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.OBSERVATORY.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof ObservatoryBlockEntity observatory) {
                observatory.tick();
            }
        } : null;
    }
}
