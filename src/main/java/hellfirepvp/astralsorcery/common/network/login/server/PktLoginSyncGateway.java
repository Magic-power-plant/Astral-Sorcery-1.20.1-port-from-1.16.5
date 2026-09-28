package hellfirepvp.astralsorcery.common.network.login.server;

import hellfirepvp.astralsorcery.common.auxiliary.gateway.CelestialGatewayHandler;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.network.base.ASLoginPacket;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//1.16.5 common/network/login/server/PktLoginSyncGateway.java. The ported stub declared
//Map<ResourceKey<Level>, Collection<Object>>, made an empty packet, wrote a literal 0 per dimension and had an
//empty client handler, so a joining client never learned about any celestial gateway. The gateway cache and its
//GatewayNode buffer codec already exist in this tree, so the real payload is restored here.
//1.16.5 selects the server cache with LogicalSide.SERVER; this tree's CelestialGatewayHandler is keyed by Dist,
//and CelestialGatewayHandler#syncToAll uses Dist.DEDICATED_SERVER for the same "server side cache" lookup.
public class PktLoginSyncGateway extends ASLoginPacket<PktLoginSyncGateway> {

    private Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> positions = new HashMap<>();

    public PktLoginSyncGateway() {}

    public static PktLoginSyncGateway makeLogin() {
        PktLoginSyncGateway pkt = new PktLoginSyncGateway();
        pkt.positions = CelestialGatewayHandler.INSTANCE.getGatewayCache(Dist.DEDICATED_SERVER);
        return pkt;
    }

    @Nonnull
    @Override
    public Encoder<PktLoginSyncGateway> encoder() {
        return (packet, buffer) -> {
            buffer.writeInt(packet.positions.size());
            for (ResourceKey<Level> dim : packet.positions.keySet()) {
                ByteBufUtils.writeVanillaRegistryEntry(buffer, dim);
                ByteBufUtils.writeCollection(buffer, packet.positions.get(dim), (buf, node) -> node.write(buf));
            }
        };
    }

    @Nonnull
    @Override
    public Decoder<PktLoginSyncGateway> decoder() {
        return buffer -> {
            PktLoginSyncGateway pkt = new PktLoginSyncGateway();
            int dimSize = buffer.readInt();
            for (int i = 0; i < dimSize; i++) {
                ResourceKey<Level> dim = ByteBufUtils.readVanillaRegistryEntry(buffer);
                pkt.positions.put(dim, ByteBufUtils.readList(buffer, GatewayCache.GatewayNode::read));
            }
            return pkt;
        };
    }

    @Nonnull
    @Override
    public Handler<PktLoginSyncGateway> handler() {
        return new Handler<PktLoginSyncGateway>() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void handleClient(PktLoginSyncGateway packet, NetworkEvent.Context context) {
                context.enqueueWork(() -> {
                    CelestialGatewayHandler.INSTANCE.updateClientCache(packet.positions);
                    acknowledge(context);
                });
            }

            @Override
            public void handle(PktLoginSyncGateway packet, NetworkEvent.Context context, LogicalSide side) {}
        };
    }

    //Test/verification accessor: the login packet has no other read path on the receiving side.
    public Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> getPositions() {
        return this.positions;
    }
}
