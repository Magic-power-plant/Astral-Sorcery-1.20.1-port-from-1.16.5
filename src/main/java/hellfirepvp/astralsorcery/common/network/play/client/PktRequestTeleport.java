package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktRequestTeleport {

    private ResourceKey<Level> dim;
    private BlockPos pos;

    public PktRequestTeleport() {}

    public PktRequestTeleport(ResourceKey<Level> dim, BlockPos pos) {
        this.dim = dim;
        this.pos = pos;
    }

    public static void encode(PktRequestTeleport msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeVanillaRegistryEntry(buf, msg.dim);
        ByteBufUtils.writePos(buf, msg.pos);
    }

    public static PktRequestTeleport decode(FriendlyByteBuf buf) {
        PktRequestTeleport pkt = new PktRequestTeleport();
        pkt.dim = ByteBufUtils.readVanillaRegistryEntry(buf);
        pkt.pos = ByteBufUtils.readPos(buf);
        return pkt;
    }

    public static void handle(PktRequestTeleport msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || msg.dim == null || msg.pos == null) {
                    return;
                }

                TileCelestialGateway gateway = MiscUtils.getTileAt(player.level(),
                        Vector3.atEntityCorner(player).toBlockPos(),
                        TileCelestialGateway.class, false);
                if (gateway == null || !gateway.hasMultiblock() || !gateway.doesSeeSky()) {
                    return;
                }

                MinecraftServer server = player.getServer();
                if (server == null) {
                    return;
                }
                ServerLevel target = server.getLevel(msg.dim);
                if (target == null) {
                    return;
                }

                GatewayCache cache = DataAS.DOMAIN_AS.getData(target, DataAS.KEY_GATEWAY_CACHE);
                GatewayCache.GatewayNode node = cache.getGatewayNode(msg.pos);
                if (node != null && node.hasAccess(player)) {
                    AstralSorcery.getProxy().scheduleDelayed(
                            () -> MiscUtils.transferEntityTo(player, target.dimension(), msg.pos));
                }
            });
        }
        context.setPacketHandled(true);
    }

}
