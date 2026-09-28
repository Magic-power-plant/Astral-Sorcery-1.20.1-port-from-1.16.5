package hellfirepvp.astralsorcery.common.network.login.client;

import hellfirepvp.astralsorcery.common.network.base.ASLoginPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;

//1.16.5 common/network/login/client/PktLoginAcknowledge.java. Every server->client login packet is answered with
//this reply; Forge's handshake will not advance to the next login packet until a reply with the matching login
//index arrives. The ported version had an empty client handler, so the handshake had nothing to advance on.
public class PktLoginAcknowledge extends ASLoginPacket<PktLoginAcknowledge> {

    public PktLoginAcknowledge() {}

    @Nonnull
    @Override
    public Encoder<PktLoginAcknowledge> encoder() {
        return (pktLoginAcknowledge, buffer) -> {};
    }

    @Nonnull
    @Override
    public Decoder<PktLoginAcknowledge> decoder() {
        return buf -> new PktLoginAcknowledge();
    }

    @Nonnull
    @Override
    public Handler<PktLoginAcknowledge> handler() {
        return new Handler<PktLoginAcknowledge>() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void handleClient(PktLoginAcknowledge packet, NetworkEvent.Context context) {
                //ping
                acknowledge(context); //pong
            }

            @Override
            public void handle(PktLoginAcknowledge packet, NetworkEvent.Context context, LogicalSide side) {}
        };
    }
}
