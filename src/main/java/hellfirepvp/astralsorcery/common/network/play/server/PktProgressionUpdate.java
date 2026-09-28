/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.network.play.server;

import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournal;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPerkTree;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktProgressionUpdate
 * Created by HellFirePvP
 * Date: 02.06.2019 / 00:06
 */
//1.16.5 extends ASPacket<PktProgressionUpdate> and supplies Encoder/Decoder/Handler instances; the 1.20.1
//tree registers this packet through PacketChannel#register with static encode/decode/handle method
//references, so the same three bodies live in those static methods here.
public class PktProgressionUpdate {

    public ProgressionTier tier = null;
    public ResearchProgression prog = null;

    public PktProgressionUpdate() {}

    public PktProgressionUpdate(ResearchProgression prog) {
        this.prog = prog;
    }

    public PktProgressionUpdate(ProgressionTier tier) {
        this.tier = tier;
    }

    private PktProgressionUpdate(ProgressionTier tier, ResearchProgression prog) {
        this.tier = tier;
        this.prog = prog;
    }

    public static void encode(PktProgressionUpdate msg, FriendlyByteBuf buf) {
        ByteBufUtils.writeOptional(buf, msg.tier, (b, v) -> ByteBufUtils.writeEnumValue(b, v));
        ByteBufUtils.writeOptional(buf, msg.prog, (b, v) -> ByteBufUtils.writeEnumValue(b, v));
    }

    public static PktProgressionUpdate decode(FriendlyByteBuf buf) {
        return new PktProgressionUpdate(
                ByteBufUtils.readOptional(buf, b -> ByteBufUtils.readEnumValue(b, ProgressionTier.class)),
                ByteBufUtils.readOptional(buf, b -> ByteBufUtils.readEnumValue(b, ResearchProgression.class)));
    }

    public static void handle(PktProgressionUpdate msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleClient(msg));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PktProgressionUpdate packet) {
        if (packet.tier != null) {
            //1.16.5: PlayerEntity#sendMessage(ITextComponent, UUID) with Util.DUMMY_UUID
            // -> 1.20.1: Player#sendSystemMessage(Component); the sender UUID parameter is gone.
            Minecraft.getInstance().player.sendSystemMessage(
                    Component.translatable("astralsorcery.progress.gain.progress.chat")
                            .withStyle(ChatFormatting.BLUE));
        }
        if (packet.prog != null) {
            Minecraft.getInstance().player.sendSystemMessage(
                    Component.translatable("astralsorcery.progress.gain.research.chat", packet.prog.getName())
                            .withStyle(ChatFormatting.AQUA));
        }
        packet.refreshJournal();
    }

    @OnlyIn(Dist.CLIENT)
    private void refreshJournal() {
        Screen open = Minecraft.getInstance().screen;
        if (open != null) {
            if (open instanceof ScreenJournal && !(open instanceof ScreenJournalPerkTree)) {
                Minecraft.getInstance().setScreen(null);
            }
        }
        ScreenJournalProgression.resetJournal();
    }
}
