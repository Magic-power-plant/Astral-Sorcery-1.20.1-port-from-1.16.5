package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.node.socket.GemSocketItem;
import hellfirepvp.astralsorcery.common.perk.node.socket.GemSocketPerk;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.function.Supplier;

public class PktPerkGemModification {

    private int action = 0;
    private ResourceLocation perkKey = null;
    private int slotId = -1;

    public PktPerkGemModification() {}

    public static PktPerkGemModification insertItem(ResourceLocation perk, int slotId) {
        PktPerkGemModification pkt = new PktPerkGemModification();
        pkt.action = 0;
        pkt.perkKey = perk;
        pkt.slotId = slotId;
        return pkt;
    }

    public static PktPerkGemModification dropItem(ResourceLocation perk) {
        PktPerkGemModification pkt = new PktPerkGemModification();
        pkt.action = 1;
        pkt.perkKey = perk;
        return pkt;
    }

    public static void encode(PktPerkGemModification msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.action);
        ByteBufUtils.writeOptional(buf, msg.perkKey, ByteBufUtils::writeResourceLocation);
        buf.writeInt(msg.slotId);
    }

    public static PktPerkGemModification decode(FriendlyByteBuf buf) {
        PktPerkGemModification pkt = new PktPerkGemModification();
        pkt.action = buf.readInt();
        pkt.perkKey = ByteBufUtils.readOptional(buf, ByteBufUtils::readResourceLocation);
        pkt.slotId = buf.readInt();
        return pkt;
    }

    public static void handle(PktPerkGemModification msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || msg.perkKey == null) {
                    return;
                }
                PerkTree.PERK_TREE.getPerk(LogicalSide.SERVER, msg.perkKey).ifPresent(perk -> {
                    if (!(perk instanceof GemSocketPerk socketPerk)) {
                        return;
                    }
                    if (msg.action == 0) {
                        tryInsert(socketPerk, perk, player, msg.slotId);
                    } else if (msg.action == 1 && socketPerk.hasItem(player, LogicalSide.SERVER)) {
                        socketPerk.dropItemToPlayer(player);
                    }
                });
            });
        }
        context.setPacketHandled(true);
    }

    private static void tryInsert(GemSocketPerk socketPerk, AbstractPerk perk, ServerPlayer player, int slotId) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid() || slotId < 0 || slotId >= player.getInventory().getContainerSize()
                || socketPerk.hasItem(player, LogicalSide.SERVER)) {
            return;
        }
        ItemStack stack = player.getInventory().getItem(slotId);
        if (stack.isEmpty() || !(stack.getItem() instanceof GemSocketItem socketItem)) {
            return;
        }
        ItemStack toInsert = ItemUtils.copyStackWithSize(stack, 1);
        if (socketItem.canBeInserted(toInsert, (AbstractPerk & GemSocketPerk) perk,
                player, progress, LogicalSide.SERVER)
                && socketPerk.setContainedItem(player, LogicalSide.SERVER, toInsert)) {
            player.getInventory().setItem(slotId, ItemUtils.copyStackWithSize(stack, stack.getCount() - 1));
        }
    }

}
