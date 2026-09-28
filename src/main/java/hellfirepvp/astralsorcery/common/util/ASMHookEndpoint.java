/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyEntityReach;
import net.minecraft.client.Minecraft;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ASMHookEndpoint
 * Created by HellFirePvP
 * Date: 08.08.2019 / 06:52
 */
//TODO_PORT[AS-ASM-01]: both methods are call-target-only; in 1.16.5 nothing in java calls them, the two
// coremod scripts src/main/resources/coremods/reach_set_server_entity_interact.js and
// src/main/resources/coremods/reach_set_client_renderer.js patch the calls into
// ServerPlayNetHandler#processUseEntity and GameRenderer#getMouseOver respectively
// (1.16.5 common/util/ASMHookEndpoint.java:30-31). The 1.20.1 tree ships no src/main/resources/coremods
// directory and mods.toml declares no coremod, so nothing invokes these two hooks yet. Porting the
// coremod scripts (or replacing them with mixins) needs resources/ and astralsorcery.mixins.json, both of
// which are outside this slice - reported as an external change.
public class ASMHookEndpoint {

    //Kept as JS since mixins might clash if multiple mods are targeting the 36.0 constant
    //1.16.5 ServerPlayNetHandler -> 1.20.1 ServerGamePacketListenerImpl
    public static double getOverriddenSeenEntityReachMaximum(ServerGamePacketListenerImpl handler, double original) {
        Player player = handler.player;
        //1.16.5 Entity#getEntityWorld()#isRemote() -> 1.20.1 Entity#level()#isClientSide()
        PlayerProgress prog = ResearchHelper.getProgress(player, player.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER);
        if (prog.isValid() && prog.getPerkData().hasPerkEffect(perk -> perk instanceof KeyEntityReach)) {
            return 999_999_999.0;
        }
        return original;
    }

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
