package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktSyncCharge {

    private float maxCharge = 0;
    private float charge = 0;

    public PktSyncCharge() {}

    public PktSyncCharge(float maxCharge, float charge) {
        this.maxCharge = maxCharge;
        this.charge = charge;
    }

    public float getMaxCharge() {
        return maxCharge;
    }

    public float getCharge() {
        return charge;
    }

    public static void encode(PktSyncCharge msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.maxCharge);
        buf.writeFloat(msg.charge);
    }

    public static PktSyncCharge decode(FriendlyByteBuf buf) {
        return new PktSyncCharge(buf.readFloat(), buf.readFloat());
    }

    public static void handle(PktSyncCharge msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    //1.16.5 PktSyncCharge#handleClient (1.16.5 common/network/play/server/PktSyncCharge.java:75-79). This body was
    //empty, and AlignmentChargeHandler#receiveCharge is the ONLY writer of the LogicalSide.CLIENT charge maps, so
    //it had zero callers while the server sent this packet every player tick. getCurrentCharge/getMaximumCharge
    //default a missing entry to MAX_CHARGE, so the client believed it was permanently at full charge: the HUD bar
    //never moved, the low-charge auto-reveal in AlignmentChargeRenderer (<= 0.95F) was unreachable, and
    //PlayerWalkableAir#canSupportEffect kept client-side air collision alive after the server had stopped
    //supporting it, which rubber-bands the player.
    //The null guard is ours, not 1.16.5's: receiveCharge dereferences player.getUUID(), and the local player can
    //be null if this arrives while disconnecting.
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktSyncCharge packet) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        AlignmentChargeHandler.INSTANCE.receiveCharge(packet, player);
    }

}
