package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktDiscoverConstellation {

    private ResourceLocation constellation;

    public PktDiscoverConstellation() {}

    public PktDiscoverConstellation(ResourceLocation constellation) {
        this.constellation = constellation;
    }

    public static void encode(PktDiscoverConstellation msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeResourceLocation(buf, msg.constellation);
    }

    public static PktDiscoverConstellation decode(FriendlyByteBuf buf) {
        return new PktDiscoverConstellation(ByteBufUtils.readResourceLocation(buf));
    }

    public static void handle(PktDiscoverConstellation msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                IConstellation constellation = ConstellationRegistry.getConstellation(msg.constellation);
                if (player == null || constellation == null) {
                    return;
                }
                PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
                if (progress.isValid() && constellation.canDiscover(player, progress)) {
                    ResearchManager.discoverConstellation(constellation, player);
                }
            });
        }
        context.setPacketHandled(true);
    }

}
