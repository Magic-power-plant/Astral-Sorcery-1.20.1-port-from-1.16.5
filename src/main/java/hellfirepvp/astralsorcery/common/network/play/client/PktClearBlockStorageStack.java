package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.item.base.ItemBlockStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktClearBlockStorageStack {

    public PktClearBlockStorageStack() {}

    public static void encode(PktClearBlockStorageStack msg, FriendlyByteBuf buf) {}

    public static PktClearBlockStorageStack decode(FriendlyByteBuf buf) {
        return new PktClearBlockStorageStack();
    }

    public static void handle(PktClearBlockStorageStack msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    ItemBlockStorage.clearContainerFor(player);
                }
            });
        }
        context.setPacketHandled(true);
    }

}
