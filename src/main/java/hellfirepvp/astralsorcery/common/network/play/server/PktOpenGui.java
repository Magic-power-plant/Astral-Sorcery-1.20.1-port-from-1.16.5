package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktOpenGui {

    private int guiType;
    private CompoundTag data;

    public PktOpenGui() {}

    public PktOpenGui(GuiType guiType, CompoundTag data) {
        this.guiType = guiType.ordinal();
        this.data = data;
    }

    public static void encode(PktOpenGui msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.guiType);
        ByteBufUtils.writeCompound(buf, msg.data);
    }

    public static PktOpenGui decode(FriendlyByteBuf buf) {
        PktOpenGui pkt = new PktOpenGui();
        pkt.guiType = buf.readInt();
        pkt.data = ByteBufUtils.readCompound(buf);
        return pkt;
    }

    public static void handle(PktOpenGui msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktOpenGui packet) {
        GuiType[] values = GuiType.values();
        if (packet.guiType >= 0 && packet.guiType < values.length) {
            GuiType type = values[packet.guiType];
            AstralSorcery.getProxy().openGuiClient(type, packet.data);
        }
    }
}
