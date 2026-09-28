package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.common.item.base.OverrideInteractItem;
import hellfirepvp.astralsorcery.common.tile.base.TileOwned;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class EventHandlerInteract {

    private EventHandlerInteract() {}

    public static void attachListeners(IEventBus eventBus) {
        eventBus.addListener(EventHandlerInteract::onBlockInteract);
        eventBus.addListener(EventHandlerInteract::onEntityInteract);
        eventBus.addListener(EventHandlerInteract::onSinglePlace);
        eventBus.addListener(EventHandlerInteract::onMultiPlace);
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack held = event.getItemStack();
        if (held.getItem() instanceof OverrideInteractItem item) {
            if (item.shouldInterceptEntityInteract(event.getSide(), event.getEntity(), event.getHand(), event.getTarget()) &&
                    item.doEntityInteract(event.getSide(), event.getEntity(), event.getHand(), event.getTarget())) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    private static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (held.getItem() instanceof OverrideInteractItem item) {
            if (item.shouldInterceptBlockInteract(event.getSide(), event.getEntity(), event.getHand(), event.getPos(), event.getFace()) &&
                    item.doBlockInteract(event.getSide(), event.getEntity(), event.getHand(), event.getPos(), event.getFace())) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    private static void onSinglePlace(BlockEvent.EntityPlaceEvent event) {
        if (event instanceof BlockEvent.EntityMultiPlaceEvent) {
            return;
        }
        LevelAccessor world = event.getLevel();
        if (world.isClientSide() || !(event.getEntity() instanceof Player)) {
            return;
        }
        handleOwnerPlacement(world, event.getPos(), (Player) event.getEntity());
    }

    private static void onMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        LevelAccessor world = event.getLevel();
        if (world.isClientSide() || !(event.getEntity() instanceof Player)) {
            return;
        }
        Player placer = (Player) event.getEntity();
        for (var snapshot : event.getReplacedBlockSnapshots()) {
            handleOwnerPlacement(world, snapshot.getPos(), placer);
        }
    }

    private static void handleOwnerPlacement(LevelAccessor world, BlockPos pos, Player placer) {
        TileOwned owned = MiscUtils.getTileAt(world, pos, TileOwned.class, true);
        if (owned != null) {
            owned.setOwner(placer);
        }
    }
}
