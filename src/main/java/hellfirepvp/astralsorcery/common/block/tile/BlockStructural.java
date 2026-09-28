package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Port of 1.16.5 {@code BlockStructural}: an invisible helper block occupying the upper half of multi-block-tall
 * machines (currently only the telescope). Property name {@code blocktype} matches the blockstate JSON.
 */
public class BlockStructural extends Block {

    public static final EnumProperty<BlockType> BLOCK_TYPE = EnumProperty.create("blocktype", BlockType.class);

    private static final VoxelShape STRUCT_TELESCOPE = Shapes.create(1D / 16D, -16D / 16D, 1D / 16D, 15D / 16D, 16D / 16D, 15D / 16D);

    public BlockStructural() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .noLootTable());

        this.registerDefaultState(this.getStateDefinition().any().setValue(BLOCK_TYPE, BlockType.TELESCOPE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BLOCK_TYPE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.getValue(BLOCK_TYPE)) {
            case TELESCOPE:
                return STRUCT_TELESCOPE;
            default:
                return super.getShape(state, worldIn, pos, context);
        }
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        switch (state.getValue(BLOCK_TYPE)) {
            case TELESCOPE:
                return SoundType.WOOD;
            default:
                return super.getSoundType(state, world, pos, entity);
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        switch (state.getValue(BLOCK_TYPE)) {
            case TELESCOPE:
                return BlockType.TELESCOPE.getSupportedState().getDestroyProgress(player, world, pos.below());
            default:
                return super.getDestroyProgress(state, player, world, pos);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
                EventFlags.PLAY_BLOCK_BREAK_EFFECTS.executeWithFlag(() -> {
                    switch (state.getValue(BLOCK_TYPE)) {
                        case TELESCOPE:
                            manager.destroy(pos.below(), BlocksAS.TELESCOPE.get().defaultBlockState());
                            break;
                        default:
                            break;
                    }
                });
                return true;
            }

            @Override
            public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
                if (target instanceof BlockHitResult blockHit) {
                    EventFlags.PLAY_BLOCK_BREAK_EFFECTS.executeWithFlag(() -> {
                        switch (state.getValue(BLOCK_TYPE)) {
                            case TELESCOPE:
                                manager.crack(blockHit.getBlockPos().below(), blockHit.getDirection());
                                break;
                            default:
                                break;
                        }
                    });
                }
                return true;
            }
        });
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult rayTraceResult) {
        switch (state.getValue(BLOCK_TYPE)) {
            case TELESCOPE:
                if (world.isClientSide()) {
                    AstralSorcery.getProxy().openGui(entity, GuiType.TELESCOPE, pos.below());
                }
                return InteractionResult.SUCCESS;
            default:
                return super.use(state, world, pos, entity, hand, rayTraceResult);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        switch (state.getValue(BLOCK_TYPE)) {
            case TELESCOPE:
                return BlockType.TELESCOPE.getSupportedState().getDrops(builder);
            default:
                return new java.util.ArrayList<>();
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        switch (state.getValue(BLOCK_TYPE)) {
            case TELESCOPE:
                return BlockType.TELESCOPE.getSupportedState().getCloneItemStack(target, world, pos.below(), player);
            default:
                return super.getCloneItemStack(state, target, world, pos, player);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        switch (state.getValue(BLOCK_TYPE)) {
            case TELESCOPE:
                if (world.isEmptyBlock(pos.below())) {
                    world.removeBlock(pos, isMoving);
                }
                return;
            default:
                break;
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        if (!(world instanceof LevelWriter writer)) {
            return;
        }
        switch (state.getValue(BLOCK_TYPE)) {
            case TELESCOPE:
                if (world.isEmptyBlock(pos.below())) {
                    writer.removeBlock(pos, false);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    public enum BlockType implements StringRepresentable {

        DUMMY(Blocks.AIR::defaultBlockState),
        TELESCOPE(() -> BlocksAS.TELESCOPE.get().defaultBlockState());

        private final java.util.function.Supplier<BlockState> supportedState;

        BlockType(java.util.function.Supplier<BlockState> supportedState) {
            this.supportedState = supportedState;
        }

        public BlockState getSupportedState() {
            return supportedState.get();
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return this.getSerializedName();
        }
    }
}
