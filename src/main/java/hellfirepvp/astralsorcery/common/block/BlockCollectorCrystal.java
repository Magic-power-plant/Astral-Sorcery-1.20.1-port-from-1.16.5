package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.BlockStarlightNetwork;
import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import hellfirepvp.astralsorcery.common.block.crystal.CollectorCrystalType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.CalculationContext;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.source.AttunedSourceInstance;
import hellfirepvp.astralsorcery.common.data.research.GatedKnowledge;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockCollectorCrystal;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.crystal.CollectorCrystalBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Port of 1.16.5 {@code BlockCollectorCrystal} (1.16.5 common/block/tile/BlockCollectorCrystal.java; kept in this
 * tree's package per TODO_PORT[AS-PKG-01]).
 * <p>
 * Differences from upstream:
 * <ul>
 *     <li>{@code abstract}, as in 1.16.5, which registers only the {@code BlockRockCollectorCrystal} /
 *     {@code BlockCelestialCollectorCrystal} subclasses. An earlier port stage made this concrete purely to serve
 *     an invented {@code astralsorcery:collector_crystal} block id; that id is gone, so the class is abstract
 *     again. The {@code crystalType} field stays (the subclasses pass it through {@code super}).
 *     {@code getItemBlockClass()} is gone tree-wide because 1.20.1's DeferredRegister takes the item factory
 *     explicitly (see {@link CustomItemBlock}).</li>
 *     <li>ObserverLib's {@code BlockStructureObserver} marker interface is not part of the embedded subset
 *     (TODO_PORT[AS-CCR-01] below).</li>
 * </ul>
 */
//TODO_PORT[AS-CCR-01]: 1.16.5 also declares `implements BlockStructureObserver` (1.16.5
//common/block/tile/BlockCollectorCrystal.java:66), ObserverLib's marker that lets the structure-observer system
//notice block changes around this position and keep the enhanced-collector match cached. ObserverLib is a
//source-only reference here and BlockStructureObserver is not among the embedded classes, so the match is
//recomputed by TileEntityTick#hasMultiblock instead (same answer, see TODO_PORT[AS-TILE-03]).
public abstract class BlockCollectorCrystal extends BlockStarlightNetwork implements CustomItemBlock {

    private static final VoxelShape SHAPE = box(4.5, 0, 4.5, 11.5, 16, 11.5);
    private static final float PLAYER_HARVEST_HARDNESS = 4F;

    private final CollectorCrystalType crystalType;

    protected BlockCollectorCrystal(CollectorCrystalType crystalType) {
        super(Properties.of()
                .mapColor(crystalType.getMaterialColor())
                .strength(-1F, 3600000.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.GLASS)
                .lightLevel(state -> 11)
                .noOcclusion());
        this.crystalType = crystalType;
    }

    public CollectorCrystalType getCrystalType() {
        return crystalType;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> toolTip, TooltipFlag flag) {
        super.appendHoverText(stack, world, toolTip, flag);

        if (!(stack.getItem() instanceof ConstellationItem cstItem)) {
            return;
        }
        CrystalAttributes attr = CrystalAttributes.getCrystalAttributes(stack);
        CrystalAttributes.TooltipResult result = null;
        if (attr != null) {
            result = attr.addTooltip(toolTip, CalculationContext.Builder
                    .withSource(new AttunedSourceInstance(CrystalPropertiesAS.Sources.SOURCE_COLLECTOR_CRYSTAL_BLOCK_ENTITY, cstItem.getAttunedConstellation(stack)))
                    .addUsage(CrystalPropertiesAS.Usages.USE_COLLECTOR_CRYSTAL)
                    .addUsage(CrystalPropertiesAS.Usages.USE_LENS_TRANSFER)
                    .build());
        }

        if (result != null) {
            PlayerProgress clientProgress = ResearchHelper.getClientProgress();
            ProgressionTier tier = clientProgress.getTierReached();

            boolean addedMissing = result != CrystalAttributes.TooltipResult.ADDED_ALL;
            IWeakConstellation c = cstItem.getAttunedConstellation(stack);
            if (c != null) {
                if (GatedKnowledge.COLLECTOR_TYPE.canSee(tier) && clientProgress.hasConstellationDiscovered(c)) {
                    toolTip.add(Component.translatable("crystal.info.astralsorcery.collect.type",
                            c.getConstellationName().copy().withStyle(ChatFormatting.BLUE))
                            .withStyle(ChatFormatting.GRAY));

                } else if (!addedMissing) {
                    toolTip.add(Component.translatable("astralsorcery.progress.missing.knowledge").withStyle(ChatFormatting.GRAY));
                }
            }

            IMinorConstellation tr = cstItem.getTraitConstellation(stack);
            if (tr != null) {
                if (GatedKnowledge.CRYSTAL_TRAIT.canSee(tier) && clientProgress.hasConstellationDiscovered(tr)) {
                    toolTip.add(Component.translatable("crystal.info.astralsorcery.trait",
                            tr.getConstellationName().copy().withStyle(ChatFormatting.BLUE))
                            .withStyle(ChatFormatting.GRAY));

                } else if (!addedMissing) {
                    toolTip.add(Component.translatable("astralsorcery.progress.missing.knowledge").withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter world, BlockPos pos) {
        CollectorCrystalBlockEntity crystal = MiscUtils.getBlockEntityAt(world, pos, CollectorCrystalBlockEntity.class, false);
        if (crystal != null && crystal.isPlayerMade()) {
            int i = ForgeHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
            return player.getDigSpeed(state, pos) / PLAYER_HARVEST_HARDNESS / i;
        }
        return super.getDestroyProgress(state, player, world, pos);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        CollectorCrystalBlockEntity tcc = MiscUtils.getBlockEntityAt(world, pos, CollectorCrystalBlockEntity.class, true);
        Item i = stack.getItem();
        if (tcc != null && i instanceof ItemBlockCollectorCrystal) {
            ItemBlockCollectorCrystal ibcc = (ItemBlockCollectorCrystal) i;
            UUID playerUUID = null;
            //1.16.5: entity.getUniqueID() -> 1.20.1: Entity#getUUID()
            if (entity instanceof Player) {
                playerUUID = entity.getUUID();
            }

            tcc.updateData(playerUUID, ibcc.getCollectorType());
        }

        super.setPlacedBy(world, pos, state, entity, stack);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CollectorCrystalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.COLLECTOR_CRYSTAL.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof CollectorCrystalBlockEntity crystal) {
                crystal.tick();
            }
        } : null;
    }
}
