package hellfirepvp.astralsorcery.common.network.login.server;

import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataRegistry;
import hellfirepvp.astralsorcery.common.data.sync.base.AbstractData;
import hellfirepvp.astralsorcery.common.network.base.ASLoginPacket;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

//1.16.5 common/network/login/server/PktLoginSyncDataHolder.java. This is a LOGIN-phase packet: it must be
//registered through PacketChannel#registerLoginMessage so Forge generates it from makeLogin() during the
//handshake. Registered as an ordinary play packet it is never sent at all and makeLogin() has no caller.
public class PktLoginSyncDataHolder extends ASLoginPacket<PktLoginSyncDataHolder> {

    private Map<ResourceLocation, CompoundTag> syncData = new HashMap<>();

    public PktLoginSyncDataHolder() {}

    public static PktLoginSyncDataHolder makeLogin() {
        PktLoginSyncDataHolder pkt = new PktLoginSyncDataHolder();
        for (ResourceLocation key : SyncDataRegistry.getKnownKeys()) {
            SyncDataHolder.executeServer(key, AbstractData.class, data -> {
                CompoundTag nbt = new CompoundTag();
                data.writeAllDataToPacket(nbt);
                pkt.syncData.put(key, nbt);
            });
        }
        return pkt;
    }

    @Nonnull
    @Override
    public Encoder<PktLoginSyncDataHolder> encoder() {
        return (packet, buffer) -> {
            buffer.writeInt(packet.syncData.size());

            for (ResourceLocation key : packet.syncData.keySet()) {
                ByteBufUtils.writeResourceLocation(buffer, key);
                //1.16.5 ByteBufUtils#writeNBTTag -> this tree's writeCompound (same nullable-compound format)
                ByteBufUtils.writeCompound(buffer, packet.syncData.get(key));
            }
        };
    }

    @Nonnull
    @Override
    public Decoder<PktLoginSyncDataHolder> decoder() {
        return buffer -> {
            PktLoginSyncDataHolder pktData = new PktLoginSyncDataHolder();
            int size = buffer.readInt();

            for (int i = 0; i < size; i++) {
                ResourceLocation key = ByteBufUtils.readResourceLocation(buffer);
                CompoundTag tag = ByteBufUtils.readCompound(buffer);
                if (tag != null) {
                    pktData.syncData.put(key, tag);
                }
            }
            return pktData;
        };
    }

    @Nonnull
    @Override
    public Handler<PktLoginSyncDataHolder> handler() {
        return new Handler<PktLoginSyncDataHolder>() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void handleClient(PktLoginSyncDataHolder packet, NetworkEvent.Context context) {
                context.enqueueWork(() -> {
                    packet.syncData.forEach(SyncDataHolder::applyFullSync);
                    acknowledge(context);
                });
            }

            @Override
            public void handle(PktLoginSyncDataHolder packet, NetworkEvent.Context context, LogicalSide side) {}
        };
    }
}
