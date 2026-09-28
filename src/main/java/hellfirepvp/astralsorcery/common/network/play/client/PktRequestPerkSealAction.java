package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPerkTree;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.item.useables.ItemPerkSeal;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktRequestPerkSealAction {

    private ResourceLocation perkKey;
    private boolean doSealing;

    public PktRequestPerkSealAction() {}

    public PktRequestPerkSealAction(ResourceLocation perkKey, boolean seal) {
        this.perkKey = perkKey;
        this.doSealing = seal;
    }

    public ResourceLocation getPerkKey() {
        return perkKey;
    }

    public boolean isDoSealing() {
        return doSealing;
    }

    public static void encode(PktRequestPerkSealAction msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeResourceLocation(buf, msg.perkKey);
        buf.writeBoolean(msg.doSealing);
    }

    public static PktRequestPerkSealAction decode(FriendlyByteBuf buf) {
        PktRequestPerkSealAction pkt = new PktRequestPerkSealAction();
        pkt.perkKey = ByteBufUtils.readResourceLocation(buf);
        pkt.doSealing = buf.readBoolean();
        return pkt;
    }

    public static void handle(PktRequestPerkSealAction msg, Supplier<NetworkEvent.Context> ctx) {
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
                    if (msg.doSealing) {
                        if (ItemPerkSeal.useSeal(player, true) && ResearchManager.applyPerkSeal(player, perk)) {
                            if (!ItemPerkSeal.useSeal(player, false)) {
                                ResearchManager.breakPerkSeal(player, perk);
                            } else {
                                hellfirepvp.astralsorcery.common.network.PacketChannel.sendToPlayer(player,
                                        new PktRequestPerkSealAction(perk.getRegistryName(), true));
                            }
                        }
                    } else if (ResearchManager.breakPerkSeal(player, perk)) {
                        hellfirepvp.astralsorcery.common.network.PacketChannel.sendToPlayer(player,
                                new PktRequestPerkSealAction(perk.getRegistryName(), false));
                    }
                });
            });
        }
        context.setPacketHandled(true);
    }

    //1.16.5 common/network/play/client/PktRequestPerkSealAction.java:76-88. The server echoes this packet back on
    //both a successful seal and a successful break, but there was no client branch, so
    //ScreenJournalPerkTree#playSealApplyAnimation and #playSealBreakAnimation had zero callers. They are the only
    //writers of that screen's unlockEffects/breakEffects lists and the only players of SoundsAS.PERK_SEAL and
    //PERK_UNSEAL; playSealBreakAnimation also calls updateSearchHighlight(), so the perk-tree search highlight went
    //stale after a seal break.
    //Kept in an @OnlyIn(Dist.CLIENT) method: touching Minecraft or a Screen from handle() would make the dedicated
    //server fail to load this class when PacketChannel registers the handler.
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktRequestPerkSealAction msg) {
        if (msg.perkKey == null) {
            return;
        }
        Screen current = Minecraft.getInstance().screen;
        if (!(current instanceof ScreenJournalPerkTree tree)) {
            return;
        }
        PerkTree.PERK_TREE.getPerk(LogicalSide.CLIENT, msg.perkKey).ifPresent(perk -> {
            if (msg.doSealing) {
                tree.playSealApplyAnimation(perk);
            } else {
                tree.playSealBreakAnimation(perk);
            }
        });
    }

}
