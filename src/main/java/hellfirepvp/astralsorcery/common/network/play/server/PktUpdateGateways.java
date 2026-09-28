package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class PktUpdateGateways {

    private Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> positions = new HashMap<>();

    public PktUpdateGateways() {}

    public PktUpdateGateways(Map<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> positions) {
        this.positions = positions;
    }

    public static void encode(PktUpdateGateways msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.positions.size());
        for (Map.Entry<ResourceKey<Level>, Collection<GatewayCache.GatewayNode>> entry : msg.positions.entrySet()) {
            ByteBufUtils.writeVanillaRegistryEntry(buf, entry.getKey());
            buf.writeInt(entry.getValue().size());
        }
    }

    public static PktUpdateGateways decode(FriendlyByteBuf buf) {
        PktUpdateGateways pkt = new PktUpdateGateways();
        int dimSize = buf.readInt();
        for (int i = 0; i < dimSize; i++) {
            ResourceKey<Level> dim = ByteBufUtils.readVanillaRegistryEntry(buf);
            int count = buf.readInt();
            pkt.positions.put(dim, new ArrayList<>());
        }
        return pkt;
    }

    public static void handle(PktUpdateGateways msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktUpdateGateways packet) {
    }

}
