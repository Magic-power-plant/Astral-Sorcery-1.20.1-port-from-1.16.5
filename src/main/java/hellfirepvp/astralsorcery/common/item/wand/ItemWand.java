/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.structure.StructurePreview;
import hellfirepvp.astralsorcery.common.block.ore.BlockRockCrystalOre;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.data.world.RockCrystalBuffer;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.item.base.OverrideInteractItem;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.structure.match.util.BlockArray;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.blockentity.base.RequiresMultiblockBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemWand
 * Created by HellFirePvP
 * Date: 17.08.2019 / 23:03
 * <p>
 * TODO_PORT[AS-ITM-01]: 1.16.5 {@code displayClientStructurePreview} used ObserverLib's {@code StructurePreview}
 * client overlay to show the missing multiblock; that preview system is not part of the embedded ObserverLib subset yet.
 */
public class ItemWand extends Item implements OverrideInteractItem {

    //1.16.5 used Item#random, the protected static Random on Item; 1.20.1 no longer has that field
    private static final Random random = new Random();

    /**
     * 1.16.5: {@code super(new Properties().maxStackSize(1).group(CommonProxy.ITEM_GROUP_AS))}; in 1.20.1 the
     * properties are handed in by {@code ItemsAS} and creative tabs are data driven.
     */
    public ItemWand(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        boolean active = isSelected || (entity instanceof Player player && player.getOffhandItem() == stack);

        if (!world.isClientSide()) {
            if (active) {
                if (entity instanceof ServerPlayer serverPlayer) {
                    RockCrystalBuffer buf = DataAS.DOMAIN_AS.getData(world, DataAS.KEY_ROCK_CRYSTAL_BUFFER);

                    ChunkPos pos = new ChunkPos(entity.blockPosition());
                    for (BlockPos rPos : buf.collectPositions(pos, 6)) {
                        MiscUtils.executeWithChunk(world, rPos, () -> {
                            BlockState state = world.getBlockState(rPos);
                            if (!(state.getBlock() instanceof BlockRockCrystalOre)) {
                                buf.removeOre(rPos);
                                return;
                            }
                            if (!DayTimeHelper.isDay(world) && random.nextInt(600) == 0) {
                                PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.ROCK_CRYSTAL_COLUMN)
                                        .addData(b -> ByteBufUtils.writeVector(b, new Vector3(rPos.above())));
                                PacketChannel.sendToPlayer(serverPlayer, pkt);
                            }
                            if (random.nextInt(800) == 0) {
                                PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.ROCK_CRYSTAL_SPARKS)
                                        .addData(b -> ByteBufUtils.writeVector(b, new Vector3(rPos.above())));
                                PacketChannel.sendToPlayer(serverPlayer, pkt);
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldInterceptBlockInteract(LogicalSide side, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public boolean doBlockInteract(LogicalSide side, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        Level world = player.level();
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof WandInteractable) {
            if (((WandInteractable) b).onInteract(world, pos, player, face, player.isShiftKeyDown())) {
                return true;
            }
        }
        WandInteractable wandTe = MiscUtils.getBlockEntityAt(world, pos, WandInteractable.class, true);
        if (wandTe != null) {
            if (wandTe.onInteract(world, pos, player, face, player.isShiftKeyDown())) {
                return true;
            }
        }
        RequiresMultiblockBlockEntity mbTe = MiscUtils.getBlockEntityAt(world, pos, RequiresMultiblockBlockEntity.class, true);
        if (mbTe != null) {
            StructureType type = mbTe.getRequiredStructureType();
            if (type != null && !type.getStructure().matches(world, pos)) {
                if (world.isClientSide()) {
                    this.displayClientStructurePreview(world, pos, type);
                } else if (player.isCrouching() && player.isCreative()) {
                    BlockArray structure = type.getStructure();
                    structure.getContents().forEach((offset, rState) -> {
                        world.setBlock(pos.offset(offset), rState.getDescriptiveState(0), Block.UPDATE_ALL);
                    });
                }
                return true;
            }
        }
        return false;
    }

    //AS-ITM-01 (resolved): 1.16.5 ItemWand.java:140-156. ObserverLib's StructurePreview is embedded as
    //hellfirepvp.astralsorcery.client.structure.StructurePreview, per the source-only-reference policy in
    //build.gradle. 1.16.5 casts type.getStructure() to MatchableStructure; here PatternBlockArray already
    //implements it, so the cast is gone.
    @OnlyIn(Dist.CLIENT)
    private void displayClientStructurePreview(Level world, BlockPos pos, StructureType type) {
        StructurePreview.newBuilder(world.dimension(), pos, type.getStructure())
                .removeIfOutInDifferentWorld()
                .andPersistOnlyIf((inWorld, at) -> MiscUtils.executeWithChunk(world, pos, () -> {
                    RequiresMultiblockBlockEntity blockEntityFound = MiscUtils.getBlockEntityAt(world, pos, RequiresMultiblockBlockEntity.class, true);
                    if (blockEntityFound == null) {
                        return false;
                    }
                    return blockEntityFound.getRequiredStructureType() != null &&
                            blockEntityFound.getRequiredStructureType().equals(type);
                }, true))
                .andPersistOnlyIf((inWorld, at) -> !type.getStructure().matches(world, pos))
                .showBar(type.getDisplayName())
                .buildAndSet();
    }

    @OnlyIn(Dist.CLIENT)
    public static void playUndergroundEffect(PktPlayEffect effect) {
        Vector3 at = ByteBufUtils.readVector(effect.getExtraData());

        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        float dstr = 0.4F + 0.6F * DayTimeHelper.getCurrentDaytimeDistribution(world);
        Vector3 plVec = Vector3.atEntityCorner(Minecraft.getInstance().player);
        float dst = (float) at.distance(plVec);
        float dstMul = dst <= 25 ? 1F : (dst >= 50 ? 0F : (1F - (dst - 25F) / 25F));
        for (int i = 0; i < 3; i++) {
            if (random.nextBoolean()) {
                Effects.of(EffectTemplatesAS.GENERIC_DEPTH_PARTICLE)
                        .spawn(at.clone().add(-1 + random.nextFloat() * 3, -1 + random.nextFloat() * 3, -1 + random.nextFloat() * 3))
                        .color(VFXColorFunction.constant(ColorsAS.ROCK_CRYSTAL))
                        .setScaleMultiplier(0.4F)
                        .setAlphaMultiplier(((150F * dstr) / 255F) * dstMul)
                        .alpha(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(30 + random.nextInt(10));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void playEffect(PktPlayEffect effect) {
        Vector3 pos = ByteBufUtils.readVector(effect.getExtraData());

        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        BlockPos at = pos.toBlockPos();
        BlockPos top = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, at);

        Vector3 columnDisplay = new Vector3(top);
        MiscUtils.applyRandomOffset(columnDisplay, random, 2F);

        double mX = random.nextFloat() * 0.01F * (random.nextBoolean() ? 1 : -1);
        double mY = random.nextFloat() * 0.5F;
        double mZ = random.nextFloat() * 0.01F * (random.nextBoolean() ? 1 : -1);
        float dstr = DayTimeHelper.getCurrentDaytimeDistribution(world);
        for (int i = 0; i < 8 + random.nextInt(10); i++) {
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(columnDisplay)
                    .setMotion(new Vector3(
                            mX * (0.2 + 0.8 * random.nextFloat()),
                            mY * (random.nextFloat()),
                            mZ * (0.2 + 0.8 * random.nextFloat())
                    ))
                    .color(VFXColorFunction.constant(ColorsAS.ROCK_CRYSTAL))
                    .setAlphaMultiplier((150 * dstr) / 255F)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.3F + 0.3F * random.nextFloat())
                    .setMaxAge(25 + random.nextInt(30));
        }
    }
}
