package hellfirepvp.astralsorcery.common.network.channel;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class SimpleSendChannel {

    private final SimpleChannel channel;

    public SimpleSendChannel(SimpleChannel channel) {
        this.channel = channel;
    }

    public void sendToPlayer(Player player, Object packet) {
        if (player instanceof ServerPlayer) {
            this.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), packet);
        }
    }

    public void sendToAll(Object packet) {
        this.send(PacketDistributor.ALL.noArg(), packet);
    }

    public void sendToAllObservingChunk(Object packet, LevelChunk ch) {
        this.send(PacketDistributor.TRACKING_CHUNK.with(() -> ch), packet);
    }

    public void sendToAllAround(Object packet, PacketDistributor.TargetPoint point) {
        this.send(PacketDistributor.NEAR.with(() -> point), packet);
    }

    public void sendToServer(Object message) {
        channel.sendToServer(message);
    }

    public void send(Object message, net.minecraft.network.Connection manager, NetworkDirection direction) {
        channel.sendTo(message, manager, direction);
    }

    public void send(PacketDistributor.PacketTarget target, Object message) {
        channel.send(target, message);
    }

    public void reply(Object msgToReply, NetworkEvent.Context context) {
        channel.reply(msgToReply, context);
    }

    public <MSG> SimpleChannel.MessageBuilder<MSG> messageBuilder(final Class<MSG> type, int id) {
        return channel.messageBuilder(type, id);
    }

}
