/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.block.tile.BlockFlareLight;
import hellfirepvp.astralsorcery.common.block.tile.BlockTranslucentBlock;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.tile.TileIlluminator;
import hellfirepvp.astralsorcery.common.tile.TileTranslucentBlock;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemIlluminationWand
 * Created by HellFirePvP
 * Date: 28.11.2019 / 20:57
 */
public class ItemIlluminationWand extends Item implements ItemDynamicColor, AlignmentChargeConsumer {

    //1.16.5 used Item#random, the protected static Random on Item; 1.20.1 no longer has that field
    private static final Random RAND = new Random();
    private static final float COST_PER_ILLUMINATION = 650F;
    private static final float COST_PER_FLARE = 300F;

    /**
     * 1.16.5: {@code super(new Properties().maxStackSize(1).group(CommonProxy.ITEM_GROUP_AS))}; in 1.20.1 the
     * properties are handed in by {@code ItemsAS} and creative tabs are data driven, so only the stack size remains.
     */
    public ItemIlluminationWand(Properties properties) {
        super(properties.stacksTo(1));
    }

    //1.16.5: addInformation(...) - renamed to appendHoverText in 1.20.1
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        DyeColor color = getConfiguredColor(stack);
        //1.16.5: mergeStyle(...) - renamed to withStyle in 1.20.1
        tooltip.add(ColorUtils.getTranslation(color).withStyle(ColorUtils.textFormattingForDye(color)));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        if (player.isShiftKeyDown()) {
            return COST_PER_ILLUMINATION;
        } else {
            return COST_PER_FLARE;
        }
    }

    //1.16.5: onItemUse(ItemUseContext) - renamed to useOn(UseOnContext) in 1.20.1
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Direction dir = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (world.isClientSide() || player == null || stack.isEmpty() || !(stack.getItem() instanceof ItemIlluminationWand)) {
            return InteractionResult.SUCCESS;
        }

        BlockState state = world.getBlockState(pos);

        if (player.isShiftKeyDown()) {
            if (state.getBlock() instanceof BlockTranslucentBlock) {
                TileTranslucentBlock tb = MiscUtils.getTileAt(world, pos, TileTranslucentBlock.class, true);
                if (tb != null && (tb.getPlayerUUID() == null || tb.getPlayerUUID().equals(player.getUUID()))) {
                    if (tb.revert()) {
                        SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_UNHIGHLIGHT.getSoundEvent(), SoundSource.BLOCKS, world, pos, 0.6F, 0.9F + RAND.nextFloat() * 0.2F);
                    }
                }
            } else {
                //1.16.5: TileEntity / state.hasTileEntity() - renamed to BlockEntity / hasBlockEntity() in 1.20.1
                BlockEntity tile = MiscUtils.getTileAt(world, pos, BlockEntity.class, true);
                if (tile == null &&
                        !state.hasBlockEntity() &&
                        //1.16.5: player.canPlayerEdit(pos, dir, stack)
                        player.mayUseItemAt(pos, dir, stack) &&
                        //1.16.5: VoxelShapes.fullCube().equals(...) - the class is called Shapes in 1.20.1; as in
                        //1.16.5 this is an identity check against the full cube singleton, not a bounds comparison
                        Shapes.block().equals(world.getBlockState(pos).getShape(world, pos))) {
                    if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_ILLUMINATION, false)) {
                        //1.16.5: setBlockState(pos, state, Constants.BlockFlags.DEFAULT_AND_RERENDER) - the flag
                        //constants moved onto Block, DEFAULT_AND_RERENDER is UPDATE_ALL_IMMEDIATE (11)
                        if (world.setBlock(pos, BlocksAS.TRANSLUCENT_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE)) {
                            SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_HIGHLIGHT.getSoundEvent(), SoundSource.BLOCKS, world, pos, 0.6F, 0.9F + RAND.nextFloat() * 0.2F);
                            TileTranslucentBlock tb = MiscUtils.getTileAt(world, pos, TileTranslucentBlock.class, true);
                            if (tb != null) {
                                tb.setFakedState(state);
                                tb.setOverlayColor(ColorUtils.flareColorFromDye(getConfiguredColor(stack)));
                                tb.setPlayerUUID(player.getUUID());
                            } else {
                                //Abort, we didn't get a tileentity... for some reason.
                                world.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
                            }
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        TileIlluminator illum = MiscUtils.getTileAt(world, pos, TileIlluminator.class, true);
        if (illum != null) {
            illum.onWandUsed(stack);
            SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_LIGHT.getSoundEvent(), SoundSource.BLOCKS, world, pos, 0.6F, 1F);
            return InteractionResult.SUCCESS;
        }

        //1.16.5: ISelectionContext.forEntity(player) - renamed to CollisionContext.of in 1.20.1
        CollisionContext selContext = CollisionContext.of(player);
        BlockPos placePos = pos;
        BlockState placeState = getPlacingState(stack);
        if (!BlockUtils.isReplaceable(world, pos)) {
            //1.16.5: placePos.offset(dir) - renamed to relative in 1.20.1
            placePos = placePos.relative(dir);
        }

        if (!BlockUtils.isReplaceable(world, placePos)) {
            return InteractionResult.SUCCESS;
        }

        if (player.mayUseItemAt(placePos, dir, stack)) {
            if (world.getBlockState(placePos).equals(placeState)) {
                if (world.setBlock(placePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE)) {
                    SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_LIGHT.getSoundEvent(), SoundSource.BLOCKS, world, pos, 0.6F, 1F);
                }
                //1.16.5: placeState.isValidPosition(world, placePos) && world.placedBlockCollides(state, pos, ctx)
            } else if (placeState.canSurvive(world, placePos) &&
                    world.isUnobstructed(placeState, placePos, selContext)) {
                if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_FLARE, false)) {
                    if (world.setBlock(placePos, placeState, Block.UPDATE_ALL_IMMEDIATE)) {
                        SoundHelper.playSoundAround(SoundsAS.ILLUMINATION_WAND_LIGHT.getSoundEvent(), SoundSource.BLOCKS, world, pos, 0.6F, 1F);
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFFFF;
        }
        DyeColor color = getConfiguredColor(stack);
        return ColorUtils.flareColorFromDye(color).getRGB() | 0xFF000000;
    }

    public static void setConfiguredColor(ItemStack stack, DyeColor color) {
        NBTHelper.getPersistentData(stack).putInt("color", color != null ? color.getId() : DyeColor.YELLOW.getId());
    }

    @Nonnull
    public static DyeColor getConfiguredColor(ItemStack stack) {
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (tag.contains("color")) {
            return DyeColor.byId(tag.getInt("color"));
        }
        return DyeColor.YELLOW;
    }

    @Nonnull
    public static BlockState getPlacingState(ItemStack wand) {
        //1.16.5: getDefaultState().with(prop, value) - renamed to defaultBlockState().setValue(...) in 1.20.1
        return BlocksAS.FLARE_LIGHT.get().defaultBlockState().setValue(BlockFlareLight.COLOR, getConfiguredColor(wand));
    }
}
