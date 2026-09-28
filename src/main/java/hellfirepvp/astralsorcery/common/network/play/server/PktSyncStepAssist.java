package hellfirepvp.astralsorcery.common.network.play.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktSyncStepAssist {

    private float stepHeight;

    public PktSyncStepAssist() {}

    public PktSyncStepAssist(float stepHeight) {
        this.stepHeight = stepHeight;
    }

    public static void encode(PktSyncStepAssist msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.stepHeight);
    }

    public static PktSyncStepAssist decode(FriendlyByteBuf buf) {
        PktSyncStepAssist pkt = new PktSyncStepAssist();
        pkt.stepHeight = buf.readFloat();
        return pkt;
    }

    public static void handle(PktSyncStepAssist msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktSyncStepAssist packet) {
    }

}
