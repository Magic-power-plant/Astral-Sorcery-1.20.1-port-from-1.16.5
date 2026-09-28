package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktRequestSeed {

    private ResourceKey<Level> dim;
    private Integer session;
    private Long seed;

    public PktRequestSeed() {}

    public PktRequestSeed(Integer session, ResourceKey<Level> dim) {
        this.dim = dim;
        this.session = session;
        this.seed = -1L;
    }

    private PktRequestSeed withSeed(long seed) {
        this.seed = seed;
        return this;
    }

    public static void encode(PktRequestSeed msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeOptional(buf, msg.dim, ByteBufUtils::writeVanillaRegistryEntry);
        ByteBufUtils.writeOptional(buf, msg.session, (b, v) -> b.writeInt(v));
        ByteBufUtils.writeOptional(buf, msg.seed, (b, v) -> b.writeLong(v));
    }

    public static PktRequestSeed decode(FriendlyByteBuf buf) {
        PktRequestSeed pkt = new PktRequestSeed();
        pkt.dim = ByteBufUtils.readOptional(buf, ByteBufUtils::readVanillaRegistryEntry);
        pkt.session = ByteBufUtils.readOptional(buf, FriendlyByteBuf::readInt);
        pkt.seed = ByteBufUtils.readOptional(buf, FriendlyByteBuf::readLong);
        return pkt;
    }

    public static void handle(PktRequestSeed msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        } else {
            context.enqueueWork(() -> handleServer(msg, context));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktRequestSeed packet) {
        if (packet.dim != null && packet.session != null && packet.seed != null) {
            WorldSeedCache.updateSeedCache(packet.dim, packet.session, packet.seed);
        }
    }

    private static void handleServer(PktRequestSeed packet, NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        if (sender == null || packet.dim == null || packet.session == null) {
            return;
        }
        MinecraftServer server = sender.getServer();
        if (server == null) {
            return;
        }
        ServerLevel world = server.getLevel(packet.dim);
        if (world != null) {
            PacketChannel.sendToPlayer(sender,
                    new PktRequestSeed(packet.session, packet.dim)
                            .withSeed(MiscUtils.getRandomWorldSeed(world)));
        }
    }

}
