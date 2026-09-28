/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.client.event.PerkExperienceRenderer;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.packet.PktSyncKnowledge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ResearchSyncHelper
 * Created by HellFirePvP
 * Date: 21.04.2019 / 19:20
 */
public class ResearchSyncHelper {

    public static void pushProgressToClientUnsafe(PlayerProgress progress, Player p) {
        PktSyncKnowledge pkt = new PktSyncKnowledge(PktSyncKnowledge.STATE_ADD);
        pkt.load(progress);
        //1.16.5 PacketChannel.CHANNEL.sendToPlayer(PlayerEntity, packet) accepted any player; the ported
        //PacketChannel helper is typed to ServerPlayer, so non-server players are skipped here.
        if (p instanceof ServerPlayer) {
            PacketChannel.sendToPlayer((ServerPlayer) p, pkt);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void recieveProgressFromServer(PktSyncKnowledge packet, Player player) {
        PlayerPerkData perkData = ResearchHelper.getClientProgress().getPerkData();
        int currentLvl = perkData.getPerkLevel(player, LogicalSide.CLIENT);
        ResearchHelper.updateClientResearch(packet);
        if (perkData.getPerkLevel(player, LogicalSide.CLIENT) > currentLvl) {
            PerkExperienceRenderer.INSTANCE.revealExperience(160);
        }
    }

}
