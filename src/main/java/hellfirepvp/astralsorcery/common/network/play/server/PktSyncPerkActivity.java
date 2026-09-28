package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkEffectHelper;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PktSyncPerkActivity {

    private Type type = null;
    private ResourceLocation perkKey = null;
    private CompoundTag newData = null, oldData = null;
    private List<ResourceLocation> perkKeys = new ArrayList<>();

    public PktSyncPerkActivity() {}

    public PktSyncPerkActivity(Type type) {
        this.type = type;
    }

    public PktSyncPerkActivity(ResourceLocation perkKey, CompoundTag oldData, CompoundTag newData) {
        this.type = Type.DATACHANGE;
        this.perkKey = perkKey;
        this.oldData = oldData;
        this.newData = newData;
    }

    public PktSyncPerkActivity(List<ResourceLocation> removals) {
        this.type = Type.REMOVE_LISTED;
        this.perkKeys = new ArrayList<>(removals);
    }

    public static void encode(PktSyncPerkActivity msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeOptional(buf, msg.type, (b, v) -> ByteBufUtils.writeEnumValue(b, v));
        ByteBufUtils.writeOptional(buf, msg.perkKey, ByteBufUtils::writeResourceLocation);
        ByteBufUtils.writeOptional(buf, msg.newData, ByteBufUtils::writeCompound);
        ByteBufUtils.writeOptional(buf, msg.oldData, ByteBufUtils::writeCompound);
        ByteBufUtils.writeCollection(buf, msg.perkKeys, ByteBufUtils::writeResourceLocation);
    }

    public static PktSyncPerkActivity decode(FriendlyByteBuf buf) {
        PktSyncPerkActivity pkt = new PktSyncPerkActivity();
        pkt.type = ByteBufUtils.readOptional(buf, b -> ByteBufUtils.readEnumValue(b, Type.class));
        pkt.perkKey = ByteBufUtils.readOptional(buf, ByteBufUtils::readResourceLocation);
        pkt.newData = ByteBufUtils.readOptional(buf, ByteBufUtils::readCompound);
        pkt.oldData = ByteBufUtils.readOptional(buf, ByteBufUtils::readCompound);
        pkt.perkKeys = ByteBufUtils.readList(buf, ByteBufUtils::readResourceLocation);
        return pkt;
    }

    public static void handle(PktSyncPerkActivity msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktSyncPerkActivity packet) {
        if (Minecraft.getInstance().player == null || packet.type == null) {
            return;
        }

        switch (packet.type) {
            case CLEARALL:
                PerkEffectHelper.clientClearAllPerks();
                break;
            case REMOVE_LISTED:
                List<AbstractPerk> perks = packet.perkKeys.stream()
                        .map(key -> PerkTree.PERK_TREE.getPerk(LogicalSide.CLIENT, key))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                PerkEffectHelper.modifySources(Minecraft.getInstance().player, LogicalSide.CLIENT, perks, PerkEffectHelper.Action.REMOVE);
                break;
            case UNLOCKALL:
                PerkEffectHelper.clientRefreshAllPerks();
                break;
            case DATACHANGE:
                if (packet.perkKey == null || packet.oldData == null || packet.newData == null) {
                    return;
                }
                PerkTree.PERK_TREE.getPerk(LogicalSide.CLIENT, packet.perkKey)
                        .ifPresent(perk -> PerkEffectHelper.clientChangePerkData(perk, packet.oldData, packet.newData));
                break;
            default:
                break;
        }
    }

    public enum Type {
        CLEARALL,
        REMOVE_LISTED,
        UNLOCKALL,
        DATACHANGE
    }

}
