/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyEntityReach;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

public final class ClientReachHelper {
    @OnlyIn(Dist.CLIENT)
    public static double getOverriddenCreativeEntityReach(double defaultExtendedReach) {
        PlayerProgress prog = ResearchHelper.getProgress(Minecraft.getInstance().player, LogicalSide.CLIENT);
        if (prog.isValid() && prog.getPerkData().hasPerkEffect(perk -> perk instanceof KeyEntityReach)) {
            //1.16.5 Minecraft#playerController (PlayerController#getBlockReachDistance)
            // -> 1.20.1 Minecraft#gameMode (MultiPlayerGameMode#getPickRange)
            return Math.max(defaultExtendedReach, Minecraft.getInstance().gameMode.getPickRange());
        }
        return defaultExtendedReach;
    }
}
