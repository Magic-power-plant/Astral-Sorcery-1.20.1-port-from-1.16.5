/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.wand;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingOverlayUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import hellfirepvp.astralsorcery.client.util.draw.BufferDecoratorBuilder;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage;
import hellfirepvp.astralsorcery.common.item.base.client.ItemHeldRender;
import hellfirepvp.astralsorcery.common.item.base.client.ItemOverlayRender;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.collection.MapStream;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.block.util.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.block.util.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.fml.LogicalSide;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemExchangeWand
 * Created by HellFirePvP
 * Date: 28.02.2020 / 21:04
 */
public class ItemExchangeWand extends Item implements ItemBlockStorage, ItemOverlayRender, ItemHeldRender, AlignmentChargeConsumer {

    private static final float COST_PER_EXCHANGE = 5F;

    /**
     * 1.16.5 built its own {@code Properties().maxStackSize(1).group(CommonProxy.ITEM_GROUP_AS)} in the constructor;
     * in 1.20.1 the properties come from {@code ItemsAS} (creative tabs are data driven now), so the stack size is
     * re-applied here to keep the item unstackable no matter what the registration passes in.
     */
    public ItemExchangeWand(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(getSizeMode(stack).getDisplay().withStyle(ChatFormatting.GOLD));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 0;
    }

    /**
     * 1.16.5 declared {@code getHarvestLevel(stack, ToolType, player, state) = 3} and
     * {@code getToolTypes(stack) = {PICKAXE, AXE, SHOVEL}} so the wand could harvest whatever it exchanges.
     * 1.20.1 dropped harvest levels and {@code ToolType} entirely: the tier check is expressed by
     * {@link #isCorrectToolForDrops} (which the wand always answers with {@code true}, just like 1.16.5's
     * {@code canHarvestBlock}), and the "acts like a pickaxe/axe/shovel" part becomes the three dig tool actions.
     * Only the *_DIG actions are claimed - 1.16.5 got no log-stripping/path-making from its tool types either,
     * as those were implemented by AxeItem/ShovelItem rather than by the tool type itself.
     */
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.PICKAXE_DIG.equals(toolAction) ||
                ToolActions.AXE_DIG.equals(toolAction) ||
                ToolActions.SHOVEL_DIG.equals(toolAction);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState blockIn) {
        return true;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        BlockHitResult hitResult = MiscUtils.rayTraceLookBlock(player, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
        if (hitResult == null) {
            return 0F;
        }
        return getPlaceStates(player, player.level(), hitResult.getBlockPos(), stack).size() * COST_PER_EXCHANGE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean renderInHand(ItemStack stack, PoseStack renderStack, float pTicks) {
        BlockHitResult hitResult = MiscUtils.rayTraceLookBlock(Minecraft.getInstance().player, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE);
        if (hitResult == null) {
            return true;
        }
        Level world = Minecraft.getInstance().level;
        BlockPos at = hitResult.getBlockPos();
        Map<BlockPos, BlockState> placeStates = getPlaceStates(Minecraft.getInstance().player, world, at, stack);
        if (placeStates.isEmpty()) {
            return true;
        }

        //1.16.5: RenderSystem.enableTexture(); - the fixed-function texture toggle doesn't exist in 1.20.1
        BlockAtlasTexture.getInstance().bindTexture();

        int[] fullBright = new int[] { 15, 15 };
        BufferDecoratorBuilder decorator = BufferDecoratorBuilder.withLightmap((skyLight, blockLight) -> fullBright);
        Vector3 offset = RenderingVectorUtils.getStandardTranslationRemovalVector(pTicks);

        RenderSystem.enableBlend();
        Blending.ADDITIVEDARK.apply();
        RenderSystem.disableDepthTest();
        //1.16.5: RenderSystem.disableAlphaTest(); - gone in 1.20.1 (alpha discard lives in the core shaders)

        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.BLOCK, buf -> {
            placeStates.forEach((pos, state) -> {
                renderStack.pushPose();
                renderStack.translate(pos.getX() - offset.getX() + 0.1F, pos.getY() - offset.getY() + 0.1F, pos.getZ() - offset.getZ() + 0.1F);
                renderStack.scale(0.8F, 0.8F, 0.8F);
                RenderingUtils.renderSimpleBlockModel(state, renderStack, decorator.decorate(buf), pos, null, false);
                renderStack.popPose();
            });
        });

        //1.16.5: RenderSystem.enableAlphaTest(); - see above
        RenderSystem.enableDepthTest();
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean renderOverlay(PoseStack renderStack, ItemStack stack, float pTicks) {
        List<Tuple<ItemStack, Integer>> foundStacks = getInventoryMatchingItemStacks(Minecraft.getInstance().player, stack);
        RenderingOverlayUtils.renderDefaultItemDisplay(renderStack, foundStacks);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        if (world.isClientSide() || !(player instanceof ServerPlayer) || stack.isEmpty()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            ItemBlockStorage.storeBlockState(stack, world, pos);
            return InteractionResult.SUCCESS;
        }

        // availableStacks should already contain enough to fill whatever placeStates has precalculated
        Map<BlockPos, BlockState> placeStates = getPlaceStates(player, world, pos, stack);
        Map<BlockState, Tuple<ItemStack, Integer>> availableStacks = MapStream.of(getInventoryMatching(player, stack))
                .filter(tpl -> placeStates.containsValue(tpl.getA()))
                .collect(Collectors.toMap(Tuple::getA, Tuple::getB));

        for (BlockPos placePos : placeStates.keySet()) {
            BlockState stateToPlace = placeStates.get(placePos);
            Tuple<ItemStack, Integer> availableStack = availableStacks.get(stateToPlace);
            if (availableStack == null) {
                continue;
            }

            ItemStack extractable = ItemUtils.copyStackWithSize(availableStack.getA(), 1);
            boolean canExtract = player.isCreative();
            if (!canExtract) {
                if (ItemUtils.consumeFromPlayerInventory(player, stack, extractable, true)) {
                    canExtract = true;
                }
            }
            if (!canExtract) {
                continue;
            }

            BlockState prevState = world.getBlockState(placePos);
            if ((player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, stack, extractable, true)) &&
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_EXCHANGE, false) &&
                    ((ServerPlayer) player).gameMode.destroyBlock(placePos) &&
                    MiscUtils.canPlayerPlaceBlockPos(player, stateToPlace, placePos, Direction.UP) &&
                    (player.isCreative() || ItemUtils.consumeFromPlayerInventory(player, stack, extractable, false)) &&
                    world.setBlockAndUpdate(placePos, stateToPlace)) {
                PktPlayEffect ev = new PktPlayEffect(PktPlayEffect.Type.BLOCK_EFFECT)
                        .addData(buf -> {
                            ByteBufUtils.writePos(buf, placePos);
                            ByteBufUtils.writeBlockState(buf, prevState);
                        });
                //1.16.5: PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(world, placePos, 32));
                PacketChannel.sendToAllAround(ev, world, Vec3.atLowerCornerOf(placePos), 32);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack held = playerIn.getItemInHand(handIn);
        if (playerIn.isShiftKeyDown()) {
            SizeMode nextMode = getSizeMode(held).next();
            setSizeMode(held, nextMode);
            playerIn.displayClientMessage(nextMode.getDisplay(), true);
        }
        return InteractionResultHolder.success(held);
    }

    @Nonnull
    private Map<BlockPos, BlockState> getPlaceStates(Player placer, Level world, BlockPos origin, ItemStack refStack) {
        Map<BlockState, Tuple<ItemStack, Integer>> tplStates = getInventoryMatching(placer, refStack);
        BlockState atState = world.getBlockState(origin);
        SizeMode mode = getSizeMode(refStack);
        Map<BlockPos, BlockState> placeables = Maps.newHashMap();

        BlockState match = BlockUtils.getMatchingState(tplStates.keySet(), atState);
        if (match != null && tplStates.size() <= 1) {
            return placeables; //If trying to replace a block with its identical block.
        }
        float hardness = atState.getDestroySpeed(world, origin);
        //TODO_PORT[AS-WND-03]: hellfirepvp.astralsorcery.common.data.config.entry.WandsConfig doesn't exist in the
        // 1.20.1 config package yet (owned by another slice); 1.16.5 read
        // WandsConfig.CONFIG.exchangeWandMaxHardness.get() here (1.16.5 ItemExchangeWand.java:255).
        // Until that config entry is ported, the 1.16.5 default (-1 = no hardness limit) is used verbatim.
        int cfgHardness = -1;
        if (hardness == -1 || (cfgHardness != -1 && hardness > cfgHardness)) {
            return placeables; //Don't break/exchange too hard or unbreakable blocks.
        }

        int totalItems = 0;
        if (placer.isCreative()) {
            totalItems = Integer.MAX_VALUE;
        } else {
            for (Tuple<ItemStack, Integer> amountTpl : tplStates.values()) {
                totalItems += (amountTpl.getB() == -1 ? 500_000 : amountTpl.getB());
            }
        }

        List<BlockPos> foundPositions = BlockDiscoverer.discoverBlocksWithSameStateAround(world, origin, true, mode.getSearchRadius(), totalItems, false);
        if (foundPositions.isEmpty()) {
            return placeables; //It.. shouldn't actually be empty here, ever. Should at least have 1 entry.
        }

        Map<BlockState, Integer> placeAmounts = Maps.newHashMap();
        for (BlockState state : tplStates.keySet()) {
            placeAmounts.put(state, placer.isCreative() ? Integer.MAX_VALUE : tplStates.get(state).getB());
        }
        List<BlockState> placeableStates = Lists.newArrayList(placeAmounts.keySet());
        Random rand = ItemBlockStorage.getPreviewRandomFromWorld(world);

        for (BlockPos pos : foundPositions) {
            Collections.shuffle(placeableStates, rand);
            BlockState toPlace = Iterables.getFirst(placeableStates, null);

            if (toPlace == null) {
                continue;
            }
            if (!placer.isCreative()) {
                int count = placeAmounts.get(toPlace);
                count--;
                if (count <= 0) {
                    placeAmounts.remove(toPlace);
                    placeableStates.remove(toPlace);
                } else {
                    placeAmounts.put(toPlace, count);
                }
            }

            placeables.put(pos, toPlace);
        }
        return placeables;
    }

    //TODO_PORT[AS-WND-02]: hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage (owned by another slice) is
    // currently missing the 1.16.5 statics getInventoryMatching(PlayerEntity, ItemStack) and the Tuple-returning
    // getInventoryMatchingItemStacks(PlayerEntity, ItemStack) (1.16.5 ItemBlockStorage.java:66-88). They are
    // reimplemented verbatim below so the wand keeps its exact behaviour; once the interface regains them, delete
    // these two helpers and call ItemBlockStorage.getInventoryMatching / getInventoryMatchingItemStacks again.
    @Nonnull
    private static Map<BlockState, Tuple<ItemStack, Integer>> getInventoryMatching(Player player, ItemStack referenceContainer) {
        Map<BlockState, ItemStack> mappedStacks = ItemBlockStorage.getMappedStoredStates(referenceContainer);
        Map<BlockState, Tuple<ItemStack, Integer>> foundContents = new HashMap<>();
        for (BlockState state : mappedStacks.keySet()) {
            ItemStack stored = mappedStacks.get(state);

            int countDisplay = 0;
            Collection<ItemStack> stacks = ItemUtils.findItemsInPlayerInventory(player, stored, true);
            for (ItemStack found : stacks) {
                countDisplay += found.getCount();
            }
            foundContents.put(state, new Tuple<>(stored.copy(), countDisplay));
        }
        return foundContents;
    }

    @Nonnull
    private static List<Tuple<ItemStack, Integer>> getInventoryMatchingItemStacks(Player player, ItemStack referenceContainer) {
        Map<BlockState, Tuple<ItemStack, Integer>> storedStates = getInventoryMatching(player, referenceContainer);
        List<Tuple<ItemStack, Integer>> foundStacks = new ArrayList<>(storedStates.values());
        //1.16.5 sorted by Item#getRegistryName, which Forge removed from the item itself in 1.19+
        foundStacks.sort(Comparator.comparing(tpl -> String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(tpl.getA().getItem()))));
        return foundStacks;
    }

    public static void setSizeMode(@Nonnull ItemStack stack, @Nonnull SizeMode mode) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemExchangeWand)) {
            return;
        }
        CompoundTag nbt = NBTHelper.getPersistentData(stack);
        nbt.putInt("sizeMode", mode.ordinal());
    }

    @Nonnull
    public static SizeMode getSizeMode(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemExchangeWand)) {
            return SizeMode.RANGE_2;
        }
        CompoundTag nbt = NBTHelper.getPersistentData(stack);
        return MiscUtils.getEnumEntry(SizeMode.class, nbt.getInt("sizeMode"));
    }

    public static enum SizeMode {

        RANGE_2(2),
        RANGE_3(3),
        RANGE_4(4),
        RANGE_5(5);

        private final int searchRadius;

        SizeMode(int searchRadius) {
            this.searchRadius = searchRadius;
        }

        public int getSearchRadius() {
            return searchRadius;
        }

        public MutableComponent getName() {
            return Component.translatable("astralsorcery.misc.exchange.size." + this.searchRadius);
        }

        public MutableComponent getDisplay() {
            return Component.translatable("astralsorcery.misc.exchange.size", this.getName());
        }

        @Nonnull
        private SizeMode next() {
            int next = (this.ordinal() + 1) % values().length;
            return MiscUtils.getEnumEntry(SizeMode.class, next);
        }
    }
}
