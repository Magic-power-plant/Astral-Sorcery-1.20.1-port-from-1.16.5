package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.common.perk.source.ModifierSource;
import hellfirepvp.astralsorcery.common.perk.PerkEffectHelper;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktSyncModifierSource {

    private ActionType actionType;
    private ModifierSource source;
    private ModifierSource newSource;

    public PktSyncModifierSource() {}

    private PktSyncModifierSource(ModifierSource source, ModifierSource newSource, ActionType actionType) {
        this.actionType = actionType;
        this.source = source;
        this.newSource = newSource;
    }

    public static PktSyncModifierSource add(ModifierSource source) {
        return new PktSyncModifierSource(source, null, ActionType.ADD);
    }

    public static PktSyncModifierSource remove(ModifierSource source) {
        return new PktSyncModifierSource(source, null, ActionType.REMOVE);
    }

    public static PktSyncModifierSource update(ModifierSource existing, ModifierSource newSource) {
        return new PktSyncModifierSource(existing, newSource, ActionType.UPDATE);
    }

    public static void encode(PktSyncModifierSource msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeOptional(buf, msg.source, ByteBufUtils::writeModifierSource);
        ByteBufUtils.writeOptional(buf, msg.newSource, ByteBufUtils::writeModifierSource);
        ByteBufUtils.writeEnumValue(buf, msg.actionType);
    }

    public static PktSyncModifierSource decode(FriendlyByteBuf buf) {
        PktSyncModifierSource pkt = new PktSyncModifierSource();
        pkt.source = ByteBufUtils.readOptional(buf, ByteBufUtils::readModifierSource);
        pkt.newSource = ByteBufUtils.readOptional(buf, ByteBufUtils::readModifierSource);
        pkt.actionType = ByteBufUtils.readEnumValue(buf, ActionType.class);
        return pkt;
    }

    public static void handle(PktSyncModifierSource msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktSyncModifierSource packet) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        switch (packet.actionType) {
            case UPDATE:
                if (packet.source != null && packet.newSource != null) {
                    PerkEffectHelper.updateSource(player, LogicalSide.CLIENT, packet.source, packet.newSource);
                }
                break;
            case ADD:
                if (packet.source != null) {
                    PerkEffectHelper.modifySource(player, LogicalSide.CLIENT, packet.source, PerkEffectHelper.Action.ADD);
                }
                break;
            case REMOVE:
                if (packet.source != null) {
                    PerkEffectHelper.modifySource(player, LogicalSide.CLIENT, packet.source, PerkEffectHelper.Action.REMOVE);
                }
                break;
        }
    }

    public enum ActionType {
        ADD,
        REMOVE,
        UPDATE;
    }
}
