package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.common.block.base.BlockStarlightNetwork;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesGlass;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileLens;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Port of 1.16.5 {@code BlockLens}. Blockstate property name is {@code against} like upstream so the
 * {@code lens.json} blockstate variants match.
 */
public class BlockLens extends BlockStarlightNetwork implements CustomItemBlock {

    private static final VoxelShape LENS_DOWN =  Shapes.create(2.5D / 16D, 0,          2.5D / 16D, 13.5D / 16D, 14.5D / 16D, 13.5D / 16D);
    private static final VoxelShape LENS_UP =    Shapes.create(2.5D / 16D, 1.5D / 16D, 2.5D / 16D, 13.5D / 16D, 1,           13.5D / 16D);
    private static final VoxelShape LENS_NORTH = Shapes.create(2.5D / 16D, 2.5D / 16D, 0,          13.5D / 16D, 13.5D / 16D, 14.5D / 16D);
    private static final VoxelShape LENS_SOUTH = Shapes.create(2.5D / 16D, 2.5D / 16D, 1.5D / 16D, 13.5D / 16D, 13.5D / 16D, 1);
    private static final VoxelShape LENS_EAST =  Shapes.create(1.5D / 16D, 2.5D / 16D, 2.5D / 16D, 1,           13.5D / 16D, 13.5D / 16D);
    private static final VoxelShape LENS_WEST =  Shapes.create(0,          2.5D / 16D, 2.5D / 16D, 14.5D / 16D, 13.5D / 16D, 13.5D / 16D);

    public static final EnumProperty<Direction> PLACED_AGAINST = EnumProperty.create("against", Direction.class);

    public BlockLens() {
        this(PropertiesGlass.coatedGlass()
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    protected BlockLens(Properties properties) {
        super(properties);
        registerDefaultState(this.getStateDefinition().any().setValue(PLACED_AGAINST, Direction.DOWN));
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        TileLens lens = MiscUtils.getTileAt(world, pos, TileLens.class, true);
        if (lens != null && !world.isClientSide() && !player.isCreative()) {
            if (lens.getColorType() != null) {
                ItemStack drop = lens.getColorType().getStack();
                ItemUtils.dropItemNaturally(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
            }
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide() && player.isShiftKeyDown()) {
            TileLens lens = MiscUtils.getTileAt(world, pos, TileLens.class, true);
            if (lens != null && lens.getColorType() != null) {
                ItemStack drop = lens.getColorType().getStack();
                if (player.getItemInHand(hand).isEmpty()) {
                    player.setItemInHand(hand, drop);
                } else {
                    if (!player.getInventory().add(drop)) {
                        ItemUtils.dropItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                    }
                }
                SoundHelper.playSoundAround(SoundsAS.BLOCK_COLOREDLENS_ATTACH.getSoundEvent(), SoundsAS.BLOCK_COLOREDLENS_ATTACH.getCategory(), world, pos, 0.8F, 1.5F);
                lens.setColorType(null);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PLACED_AGAINST);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(PLACED_AGAINST, context.getClickedFace().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.getValue(PLACED_AGAINST)) {
            case UP:
                return LENS_UP;
            case NORTH:
                return LENS_NORTH;
            case SOUTH:
                return LENS_SOUTH;
            case WEST:
                return LENS_WEST;
            case EAST:
                return LENS_EAST;
            default:
            case DOWN:
                return LENS_DOWN;
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileLens(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityTypesAS.LENS.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof TileLens lens) {
                lens.tick();
            }
        } : null;
    }
}
