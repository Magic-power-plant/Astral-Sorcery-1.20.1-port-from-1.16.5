package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktClearBlockStorageStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class EventHandlerBlockStorage {

    public static void attachListeners(IEventBus bus) {
        bus.addListener(EventHandlerBlockStorage::onClickAir);
        bus.addListener(EventHandlerBlockStorage::onClickBlockServer);
    }

    private static void onClickBlockServer(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack held = event.getItemStack();
        if (!event.getLevel().isClientSide() && !held.isEmpty() && held.getItem() instanceof ItemBlockStorage) {
            ItemBlockStorage.clearContainerFor(event.getEntity());
        }
    }

    private static void onClickAir(PlayerInteractEvent.LeftClickEmpty event) {
        ItemStack held = event.getItemStack();
        if (!held.isEmpty() && held.getItem() instanceof ItemBlockStorage) {
            PacketChannel.CHANNEL.sendToServer(new PktClearBlockStorageStack());
        }
    }

}
