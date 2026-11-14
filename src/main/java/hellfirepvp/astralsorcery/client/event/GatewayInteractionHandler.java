/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.event.effect.GatewayUIRenderHandler;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.GatewayUI;
import hellfirepvp.astralsorcery.client.util.MouseUtil;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestTeleport;
import hellfirepvp.astralsorcery.common.network.play.client.PktRevokeGatewayAccess;
import hellfirepvp.astralsorcery.common.blockentity.gateway.CelestialGatewayBlockEntity;
import hellfirepvp.astralsorcery.common.util.color.ColorUtils;
import hellfirepvp.astralsorcery.common.util.collection.MapStream;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GatewayInteractionHandler
 * Created by HellFirePvP
 * Date: 12.09.2020 / 22:15
 */
public class GatewayInteractionHandler {

    private static final Random rand = new Random();

    public static GatewayUI.GatewayEntry focusingEntry = null;
    public static int focusTicks = 0;

    public static void attachEventListeners(IEventBus eventBus) {
        eventBus.addListener(GatewayInteractionHandler::clientTick);
        // 1.16.5: addListener(LOWEST, ::renderTick) - the fov zoom is now applied through ViewportEvent.ComputeFov
        eventBus.addListener(EventPriority.LOWEST, GatewayInteractionHandler::onComputeFov);
        eventBus.addListener(GatewayInteractionHandler::onAccessRevoke);
    }

    private static void onAccessRevoke(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        if (player == null || world == null || !world.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        GatewayUI ui = GatewayUIRenderHandler.getInstance().getCurrentUI();
        if (ui == null) {
            return;
        }
        GatewayCache.GatewayNode node = ui.getThisGatewayNode();
        if (node == null || !node.isLocked() || node.getOwner() == null || node.getAllowedUsers().isEmpty()) {
            return;
        }
        CelestialGatewayBlockEntity gateway = MiscUtils.getBlockEntityAt(world, Vector3.atEntityCorner(player).toBlockPos(), CelestialGatewayBlockEntity.class, true);
        if (gateway == null || !gateway.hasMultiblock() || !gateway.doesSeeSky()) {
            return;
        }

        BlockPos clickedPos = event.getPos();
        MapStream.of(node.getAllowedUsers())
                .filter(tpl -> CelestialGatewayBlockEntity.getAllowedUserOffset(tpl.getA())
                        .offset(node.getPos())
                        .below()
                        .equals(clickedPos))
                .findAny()
                .map(Tuple::getB)
                .ifPresent(playerRef -> {
                    PktRevokeGatewayAccess pkt = new PktRevokeGatewayAccess(world.dimension(), gateway.getBlockPos(), playerRef.getPlayerUUID());
                    PacketChannel.CHANNEL.sendToServer(pkt);
                });
    }

    private static void clientTick(TickEvent.ClientTickEvent event) {
        Player player = Minecraft.getInstance().player;
        Level world = Minecraft.getInstance().level;
        if (player == null || world == null) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        GatewayUI ui = GatewayUIRenderHandler.getInstance().getCurrentUI();
        if (ui == null) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        CelestialGatewayBlockEntity gateway = MiscUtils.getBlockEntityAt(world, Vector3.atEntityCorner(player).toBlockPos(), CelestialGatewayBlockEntity.class, true);
        if (gateway == null || !gateway.hasMultiblock() || !gateway.doesSeeSky()) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        GatewayUI.GatewayEntry entry = GatewayUIRenderHandler.getInstance().
                findMatchingEntry(Mth.wrapDegrees(player.getYRot()), Mth.wrapDegrees(player.getXRot()));
        if (entry == null) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        if (!Minecraft.getInstance().options.keyUse.isDown() &&
                !Minecraft.getInstance().options.keyShift.isDown()) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        if (focusingEntry != null && !entry.equals(focusingEntry)) {
            focusingEntry = null;
            focusTicks = 0;
            return;
        }

        focusingEntry = entry;
        focusTicks++;

        Vector3 dir = focusingEntry.getRelativePos().clone().add(ui.getRenderCenter()).subtract(player.getEyePosition(1F));
        Vector3 mov = dir.clone().normalize().multiply(0.25F).negate();
        Vector3 pos = focusingEntry.getRelativePos().clone().add(ui.getRenderCenter());

        DyeColor nodeColor = focusingEntry.getNode().getColor();
        Color gatewayColor = ColorUtils.flareColorFromDye(nodeColor == null ? DyeColor.YELLOW : nodeColor);
        if (focusTicks <= 40) {
            pos = focusingEntry.getRelativePos().clone().multiply(0.8).add(ui.getRenderCenter());

            float perc = ((float) focusTicks) / 40;
            List<Vector3> positions = MiscUtils.getCirclePositions(pos, dir.clone().negate(), rand.nextFloat() * 0.2 + 0.4, rand.nextInt(6) + 25);
            for (int i = 0; i < positions.size(); i++) {
                float pc = ((float) i) / ((float) positions.size());
                if (pc >= perc) continue;

                Color color = MiscUtils.eitherOf(rand, Color.WHITE, gatewayColor, gatewayColor.brighter());
                Vector3 at = positions.get(i);
                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_GATEWAY_PARTICLE)
                        .spawn(at)
                        .setScaleMultiplier(0.08F)
                        .color(VFXColorFunction.constant(color));
                if (rand.nextInt(3) == 0) {
                    Vector3 to = pos.clone().subtract(at);
                    to.normalize().multiply(0.02);
                    p.setMotion(to)
                            .setAlphaMultiplier(0.1F);
                }
            }

            positions = MiscUtils.getCirclePositions(pos, dir, rand.nextFloat() * 0.2 + 0.4, rand.nextInt(6) + 25);
            Collections.reverse(positions);
            for (int i = 0; i < positions.size(); i++) {
                float pc = ((float) i) / ((float) positions.size());
                if (pc >= perc) continue;

                Color color = MiscUtils.eitherOf(rand, Color.WHITE, gatewayColor, gatewayColor.brighter());
                Vector3 at = positions.get(i);
                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_GATEWAY_PARTICLE)
                        .spawn(at)
                        .setScaleMultiplier(0.08F)
                        .color(VFXColorFunction.constant(color));
                if (rand.nextInt(3) == 0) {
                    Vector3 to = pos.clone().subtract(at);
                    to.normalize().multiply(0.02);
                    p.setMotion(to)
                            .setAlphaMultiplier(0.1F);
                }
            }
        } else {
            for (Vector3 v : MiscUtils.getCirclePositions(pos, dir, rand.nextFloat() * 0.3 + 0.2, rand.nextInt(20) + 30)) {

                Color color = MiscUtils.eitherOf(rand, Color.WHITE, gatewayColor, gatewayColor.brighter());
                Vector3 m = mov.clone().multiply(0.5 + rand.nextFloat() * 0.5);
                EffectHelper.of(EffectTemplatesAS.GENERIC_GATEWAY_PARTICLE)
                        .spawn(v)
                        .setScaleMultiplier(0.1F)
                        .setMotion(m)
                        .color(VFXColorFunction.constant(color));
            }
        }

        if (focusTicks > 95) {
            Minecraft.getInstance().player.setShiftKeyDown(false);
            PktRequestTeleport pkt = new PktRequestTeleport(focusingEntry.getNodeDimension(), focusingEntry.getNode().getPos());
            PacketChannel.CHANNEL.sendToServer(pkt);
            focusingEntry = null;
            focusTicks = 0;
        }
    }

    // 1.16.5: renderTick(TickEvent.RenderTickEvent) cached GameSettings#fov on Phase.START, overwrote it with the
    // zoom value for the frame and restored the cached value on Phase.END.
    // 1.20.1 Options#fov() is an OptionInstance<Integer> - writing to it quantizes the fov to whole degrees and
    // fires the option's update callback every frame, so the save/overwrite/restore dance is no longer viable.
    // ViewportEvent.ComputeFov is the hook vanilla/forge uses to alter the fov for exactly one frame, which is what
    // the 1.16.5 code emulated by hand; getPartialTick() is the renderTickTime the reference read off the event.
    //VERIFY_PORT[AS-CLI-11]: fov zoom moved from GameSettings#fov write-back to ViewportEvent.ComputeFov; the base
    // fov seen here already includes the vanilla hand/sprint fov modifier, where 1.16.5 read the raw setting value
    // (1.16.5 client/event/GatewayInteractionHandler.java:218-237)
    private static void onComputeFov(ViewportEvent.ComputeFov event) {
        GatewayUI ui = GatewayUIRenderHandler.getInstance().getCurrentUI();
        if (ui == null) {
            return;
        }

        if (focusTicks < 80) {
            return;
        }
        float percDone = 1F - ((focusTicks - 80F + (float) event.getPartialTick()) / 15F);
        percDone = (float) Math.pow(percDone, 2.4F);
        float targetFov = 10F;
        double fovPre = event.getFOV();
        double diff = fovPre - targetFov;
        event.setFOV(Math.max(targetFov, targetFov + diff * percDone));
    }
}
