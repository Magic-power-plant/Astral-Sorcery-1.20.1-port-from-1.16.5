package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble;
import hellfirepvp.astralsorcery.common.container.ContainerProviders;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class BlockAltar extends BaseEntityBlock {

    private final AltarType type;

    public BlockAltar(AltarType type) {
        super(PropertiesMarble.defaultMarble());
        this.type = type;
    }

    public AltarType getAltarType() {
        return type;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof AltarBlockEntity altar)) {
            return InteractionResult.PASS;
        }

        altar.updateType(type, false);

        //Always open the container, as 1.16.5 does (common/block/tile/BlockAltar.java:64-99). Two invented
        //behaviours used to live here and both broke the altar:
        //
        //1. The menu was gated behind player.isShiftKeyDown(). Forge 47.4.20 patches
        //   ServerPlayerGameMode#useItemOn to compute
        //     flag1 = (isSecondaryUseActive() && (mainHand or offHand non-empty))
        //             && !(both held stacks doesSneakBypassUse)
        //   and skips BlockState#use entirely when flag1 is true. IForgeItem#doesSneakBypassUse defaults to
        //   false, so with the sneak gate the altar menu opened ONLY when the player sneaked with BOTH hands
        //   empty - i.e. you could never open the altar while holding the item you wanted to put in it.
        //   ContainerProviders.AltarProvider has exactly one construction site, right here, so there was no
        //   other way in.
        //
        //2. A plain right-click fell through to an inline hasMultiblock/findRecipe/startCrafting block, so any
        //   empty-handed click force-started a craft without the Resonating Wand. Crafting belongs to
        //   TileAltar#onInteract, which ItemWand already reaches: ItemWand#shouldInterceptBlockInteract returns
        //   true unconditionally and #doBlockInteract resolves the WandInteractable tile, and TileAltar
        //   implements WandInteractable. That path already does the multiblock check, the active-recipe match,
        //   abortCrafting and startCrafting - the inline copy was redundant as well as wrong.
        //   1.21.1 AltarBlock#useItemOn is explicit about the same split: the wand crafts, any other stack
        //   opens the container.
        if (player instanceof ServerPlayer serverPlayer) {
            //1.16.5 only informs the crafting progression the first time, before BASIC_CRAFT is reached.
            if (type == AltarType.DISCOVERY
                    && !ResearchHelper.getProgress(player, LogicalSide.SERVER)
                            .getTierReached().isThisLaterOrEqual(ProgressionTier.BASIC_CRAFT)) {
                ResearchManager.informCrafted(player, type.getAltarItemRepresentation());
            }

            String translationKey = "block.astralsorcery.altar_" + type.name().toLowerCase();
            ContainerProviders.AltarProvider provider = new ContainerProviders.AltarProvider(pos, type, Component.translatable(translationKey));
            NetworkHooks.openScreen(serverPlayer, provider, provider::writeScreenOpeningData);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!(newState.getBlock() instanceof BlockAltar)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!world.isClientSide && blockEntity instanceof AltarBlockEntity altar) {
                ItemUtils.dropInventory(altar.getInventory(), world, pos);
            }
            super.onRemove(state, world, pos, newState, isMoving);
            return;
        }
        AltarType newType = ((BlockAltar) newState.getBlock()).getAltarType();
        if (this.type != newType) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AltarBlockEntity altar) {
                altar.updateType(newType, false);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        AltarBlockEntity altar = new AltarBlockEntity(pos, state);
        altar.updateType(type, true);
        return altar;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.ALTAR.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof AltarBlockEntity altar) {
                altar.tick();
            }
        } : null;
    }
}
