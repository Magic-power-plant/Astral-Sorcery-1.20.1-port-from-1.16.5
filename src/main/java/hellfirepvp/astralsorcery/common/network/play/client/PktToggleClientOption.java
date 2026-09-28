package hellfirepvp.astralsorcery.common.network.play.client;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PktToggleClientOption {

    private Option option;

    public PktToggleClientOption() {}

    public PktToggleClientOption(Option option) {
        this.option = option;
    }

    public static void encode(PktToggleClientOption msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeEnumValue(buf, msg.option);
    }

    public static PktToggleClientOption decode(FriendlyByteBuf buf) {
        return new PktToggleClientOption(ByteBufUtils.readEnumValue(buf, Option.class));
    }

    public static void handle(PktToggleClientOption msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                switch (msg.option) {
                    case DISABLE_PERK_ABILITIES:
                        if (ResearchManager.togglePerkAbilities(player)) {
                            PlayerProgress prog = ResearchHelper.getProgress(player, net.minecraftforge.fml.LogicalSide.SERVER);
                            if (prog.isValid()) {
                                Component status;
                                if (prog.doPerkAbilities()) {
                                    status = Component.translatable("astralsorcery.progress.perk_abilities.enable").withStyle(ChatFormatting.GREEN);
                                } else {
                                    status = Component.translatable("astralsorcery.progress.perk_abilities.disable").withStyle(ChatFormatting.RED);
                                }
                                player.sendSystemMessage(Component.translatable("astralsorcery.progress.perk_abilities", status).withStyle(ChatFormatting.GRAY));
                            }
                        }
                        break;
                }
            });
        }
        context.setPacketHandled(true);
    }

    public enum Option {
        DISABLE_PERK_ABILITIES
    }

}
