package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientData;
import hellfirepvp.astralsorcery.common.data.sync.base.ClientDataReader;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PktSyncData {

    private Map<ResourceLocation, CompoundTag> diffData = new HashMap<>();

    public PktSyncData() {}

    public PktSyncData(Map<ResourceLocation, CompoundTag> dataToSend) {
        this.diffData = dataToSend;
    }

    public static void encode(PktSyncData msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.diffData.size());
        for (Map.Entry<ResourceLocation, CompoundTag> entry : msg.diffData.entrySet()) {
            ByteBufUtils.writeResourceLocation(buf, entry.getKey());
            ByteBufUtils.writeCompound(buf, entry.getValue());
        }
    }

    public static PktSyncData decode(FriendlyByteBuf buf) {
        PktSyncData pkt = new PktSyncData();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            ResourceLocation key = ByteBufUtils.readResourceLocation(buf);
            CompoundTag tag = ByteBufUtils.readCompound(buf);
            if (tag != null) {
                pkt.diffData.put(key, tag);
            }
        }
        return pkt;
    }

    public static void handle(PktSyncData msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktSyncData packet) {
        for (ResourceLocation key : packet.diffData.keySet()) {
            ClientDataReader reader = SyncDataHolder.getReader(key);
            if (reader != null) {
                SyncDataHolder.executeClient(key, ClientData.class,
                        data -> reader.readFromIncomingDiff(data, packet.diffData.get(key)));
            }
        }
    }

}
