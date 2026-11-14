package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.blockentity.gateway.CelestialGatewayBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.entity.util.PlayerReference;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PktRevokeGatewayAccess {

    private ResourceKey<Level> dim = null;
    private BlockPos pos = BlockPos.ZERO;
    private UUID revokeUUID = null;

    public PktRevokeGatewayAccess() {}

    public PktRevokeGatewayAccess(ResourceKey<Level> dim, BlockPos pos, UUID revokeUUID) {
        this.dim = dim;
        this.pos = pos;
        this.revokeUUID = revokeUUID;
    }

    public static void encode(PktRevokeGatewayAccess msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeVanillaRegistryEntry(buf, msg.dim);
        ByteBufUtils.writePos(buf, msg.pos);
        ByteBufUtils.writeUUID(buf, msg.revokeUUID);
    }

    public static PktRevokeGatewayAccess decode(FriendlyByteBuf buf) {
        return new PktRevokeGatewayAccess(
                ByteBufUtils.readVanillaRegistryEntry(buf),
                ByteBufUtils.readPos(buf),
                ByteBufUtils.readUUID(buf));
    }

    public static void handle(PktRevokeGatewayAccess msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer sender = context.getSender();
                if (sender == null || msg.dim == null || msg.pos == null || msg.revokeUUID == null) {
                    return;
                }

                MinecraftServer server = sender.getServer();
                if (server == null) {
                    return;
                }
                ServerLevel world = server.getLevel(msg.dim);
                if (world == null) {
                    return;
                }
                CelestialGatewayBlockEntity gateway = MiscUtils.getBlockEntityAt(world, msg.pos,
                        CelestialGatewayBlockEntity.class, false);
                if (gateway == null || !gateway.isLocked() || gateway.getOwner() == null
                        || !gateway.getOwner().isPlayer(sender)) {
                    return;
                }

                BlockPos testPos = Vector3.atEntityCorner(sender).toBlockPos();
                CelestialGatewayBlockEntity playerGateway = MiscUtils.getBlockEntityAt(world, testPos,
                        CelestialGatewayBlockEntity.class, false);
                if (!gateway.equals(playerGateway)) {
                    return;
                }

                PlayerReference removedPlayer = gateway.removeAllowedUser(msg.revokeUUID);
                if (removedPlayer != null) {
                    PktPlayEffect effect = new PktPlayEffect(PktPlayEffect.Type.GATEWAY_REVOKE_EFFECT)
                            .addData(buffer -> ByteBufUtils.writePos(buffer, gateway.getBlockPos()));
                    PacketChannel.sendToPlayer(sender, effect);
                    sender.sendSystemMessage(Component.translatable(
                            "astralsorcery.misc.link.gateway.unlink",
                            removedPlayer.getPlayerName()).withStyle(ChatFormatting.GREEN));
                }
            });
        }
        context.setPacketHandled(true);
    }

}
