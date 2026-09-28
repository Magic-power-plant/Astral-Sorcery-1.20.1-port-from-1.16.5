package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPerkTree;
import hellfirepvp.astralsorcery.common.data.research.PlayerPerkAllocation;
import hellfirepvp.astralsorcery.common.data.research.PlayerPerkData;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktUnlockPerk {

    private ResourceLocation perkKey = null;
    private boolean serverAccept = false;

    public PktUnlockPerk() {}

    public PktUnlockPerk(boolean serverAccepted, ResourceLocation perkKey) {
        this.serverAccept = serverAccepted;
        this.perkKey = perkKey;
    }

    public static void encode(PktUnlockPerk msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeOptional(buf, msg.perkKey, ByteBufUtils::writeResourceLocation);
        buf.writeBoolean(msg.serverAccept);
    }

    public static PktUnlockPerk decode(FriendlyByteBuf buf) {
        PktUnlockPerk pkt = new PktUnlockPerk();
        pkt.perkKey = ByteBufUtils.readOptional(buf, ByteBufUtils::readResourceLocation);
        pkt.serverAccept = buf.readBoolean();
        return pkt;
    }

    public static void handle(PktUnlockPerk msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        } else if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || msg.perkKey == null) {
                    return;
                }
                PerkTree.PERK_TREE.getPerk(LogicalSide.SERVER, msg.perkKey).ifPresent(perk -> {
                    PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
                    if (!progress.isValid()) {
                        return;
                    }
                    PlayerPerkData perkData = progress.getPerkData();
                    if (!perkData.hasPerkAllocation(perk)
                            && perk.mayUnlockPerk(progress, player)
                            && ResearchManager.applyPerk(player, perk, PlayerPerkAllocation.unlock())) {
                        PacketChannel.sendToPlayer(player,
                                new PktUnlockPerk(true, perk.getRegistryName()));
                    }
                });
            });
        }
        context.setPacketHandled(true);
    }

    //1.16.5 common/network/play/client/PktUnlockPerk.java:74-86. The server already echoes this packet back with
    //serverAccept=true on a successful allocation, but there was no client branch, so
    //ScreenJournalPerkTree#playUnlockAnimation had zero callers. It is the only writer of that screen's
    //unlockEffects list and the only player of SoundsAS.PERK_UNLOCK, so allocating a perk gave no visual or
    //audible feedback at all.
    //Kept in an @OnlyIn(Dist.CLIENT) method: touching Minecraft or a Screen from handle() would make the
    //dedicated server fail to load this class when PacketChannel registers the handler.
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktUnlockPerk msg) {
        if (!msg.serverAccept || msg.perkKey == null) {
            return;
        }
        PerkTree.PERK_TREE.getPerk(LogicalSide.CLIENT, msg.perkKey).ifPresent(perk -> {
            Screen current = Minecraft.getInstance().screen;
            if (current instanceof ScreenJournalPerkTree tree) {
                tree.playUnlockAnimation(perk);
            }
        });
    }

}
