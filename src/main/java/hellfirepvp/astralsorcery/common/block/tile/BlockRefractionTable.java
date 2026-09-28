package hellfirepvp.astralsorcery.common.block.tile;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.base.LargeBlock;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesWood;
import hellfirepvp.astralsorcery.common.item.ItemParchment;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileRefractionTable;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Port of 1.16.5 {@code BlockRefractionTable}.
 */
public class BlockRefractionTable extends BaseEntityBlock implements CustomItemBlock, LargeBlock {

    private static final VoxelShape REFRACTION_TABLE = Block.box(-6, 0, -4, 22, 24, 20);
    private static final AABB PLACEMENT_BOX = new AABB(-1, 0, -1, 1, 1, 1);

    public BlockRefractionTable() {
        super(PropertiesWood.defaultInfusedWood()
                .noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return REFRACTION_TABLE;
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
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        for (int i = 0; i < rand.nextInt(3); i++) {
            Vector3 offset = new Vector3(-5.0 / 16.0, 1.505, -3.0 / 16.0);
            int random = rand.nextInt(ColorsAS.REFRACTION_TABLE_COLORS.length);
            if (random >= ColorsAS.REFRACTION_TABLE_COLORS.length / 2) { //0-5 is left, 6-11 is right
                offset.addX(24.0 / 16.0);
            }
            offset.addZ((random % (ColorsAS.REFRACTION_TABLE_COLORS.length / 2)) * (4.0 / 16.0));
            offset.add(rand.nextFloat() * 0.1, 0, rand.nextFloat() * 0.1).add(pos);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.15F + rand.nextFloat() * 0.1F)
                    .color(VFXColorFunction.constant(ColorsAS.REFRACTION_TABLE_COLORS[random]))
                    .setMaxAge(35 + rand.nextInt(30));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            TileRefractionTable tft = MiscUtils.getTileAt(world, pos, TileRefractionTable.class, true);
            if (tft != null) {
                if (player.isShiftKeyDown()) {
                    if (!tft.getInputStack().isEmpty()) {
                        ItemStack remaining = ItemUtils.dropItemToPlayer(player, tft.setInputStack(ItemStack.EMPTY));
                        if (!remaining.isEmpty()) {
                            ItemUtils.dropItemNaturally(world, player.getX(), player.getY(), player.getZ(), remaining);
                        }
                        return InteractionResult.SUCCESS;
                    }
                    if (!tft.getGlassStack().isEmpty()) {
                        ItemStack remaining = ItemUtils.dropItemToPlayer(player, tft.setGlassStack(ItemStack.EMPTY));
                        if (!remaining.isEmpty()) {
                            ItemUtils.dropItemNaturally(world, player.getX(), player.getY(), player.getZ(), remaining);
                        }
                        return InteractionResult.SUCCESS;
                    }
                } else if (!held.isEmpty()) {
                    if (held.getItem() instanceof ItemParchment && tft.getParchmentCount() < 64) {
                        int leftover = tft.addParchment(held.getCount());
                        if (leftover < tft.getParchmentCount()) {
                            if (!player.isCreative()) {
                                held.setCount(leftover);
                                if (held.isEmpty()) {
                                    player.setItemInHand(hand, ItemStack.EMPTY);
                                } else {
                                    player.setItemInHand(hand, held);
                                }
                            }
                        }
                    } else if (TileRefractionTable.isValidGlassStack(held) && tft.getGlassStack().isEmpty()) {
                        ItemStack previous = tft.setGlassStack(ItemUtils.copyStackWithSize(held, 1));
                        if (!previous.isEmpty()) {
                            ItemUtils.dropItemNaturally(world, pos.getX() + 0.5, pos.getY() + 1.8, pos.getZ() + 0.5, previous);
                        }
                        if (!player.isCreative()) {
                            held.shrink(1);
                            if (held.isEmpty()) {
                                player.setItemInHand(hand, ItemStack.EMPTY);
                            } else {
                                player.setItemInHand(hand, held);
                            }
                        }
                        return InteractionResult.PASS;
                    } else if (tft.getInputStack().isEmpty()) {
                        ItemStack previous = tft.setInputStack(ItemUtils.copyStackWithSize(held, 1));
                        if (!previous.isEmpty()) {
                            ItemUtils.dropItemNaturally(world, pos.getX() + 0.5, pos.getY() + 1.8, pos.getZ() + 0.5, previous);
                        }
                        if (!player.isCreative()) {
                            held.shrink(1);
                            if (held.isEmpty()) {
                                player.setItemInHand(hand, ItemStack.EMPTY);
                            } else {
                                player.setItemInHand(hand, held);
                            }
                        }
                    } else {
                        AstralSorcery.getProxy().openGui(player, GuiType.REFRACTION_TABLE, pos);
                    }
                } else {
                    AstralSorcery.getProxy().openGui(player, GuiType.REFRACTION_TABLE, pos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileRefractionTable te = MiscUtils.getTileAt(world, pos, TileRefractionTable.class, true);
            if (te != null && !world.isClientSide()) {
                te.dropContents();
            }
        }

        super.onRemove(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // VERIFY_PORT[AS-MCH-07]: 1.16.5 renders the refraction table through its TESR (RenderRefractionTable); the
        // target uses the block model until AS-CLI-04 lands.
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileRefractionTable(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == TileEntityTypesAS.REFRACTION_TABLE.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof TileRefractionTable table) {
                table.tick();
            }
        } : null;
    }
}
