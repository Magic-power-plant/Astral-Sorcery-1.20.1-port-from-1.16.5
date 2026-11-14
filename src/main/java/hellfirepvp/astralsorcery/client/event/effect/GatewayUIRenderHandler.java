/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event.effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.util.*;
import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.blockentity.CelestialGatewayBlockEntity;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.EnumSet;
import java.util.Random;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GatewayUIRenderHandler
 * Created by HellFirePvP
 * Date: 20.09.2019 / 19:22
 */
public class GatewayUIRenderHandler implements ITickHandler {

    private static final GatewayUIRenderHandler INSTANCE = new GatewayUIRenderHandler();

    private GatewayUI currentUI = null;

    private GatewayUIRenderHandler() {}

    public static GatewayUIRenderHandler getInstance() {
        return INSTANCE;
    }

    // VERIFY_PORT[AS-MCH-02]: GatewayUI.create requires CelestialGatewayHandler client cache + gateway block/tile registration (AS-MCH-02) to yield non-null UIs in game
    public GatewayUI getOrCreateUI(Level world, BlockPos pos, Vector3 renderPos) {
        if (currentUI == null ||
                !currentUI.getDimType().equals(world.dimension()) ||
                !currentUI.getPos().equals(pos)) {
            currentUI = GatewayUI.create(world, pos, renderPos, 5.5D);
        }
        if (currentUI != null) {
            currentUI.refreshView();
        }
        return currentUI;
    }

    public GatewayUI getCurrentUI() {
        return currentUI;
    }

    private boolean validate() {
        if (this.currentUI == null) {
            return true;
        }
        Level world = Minecraft.getInstance().level;
        CelestialGatewayBlockEntity gateway;
        if (world == null ||
                this.currentUI.getVisibleTicks() <= 0 ||
                !this.currentUI.getDimType().equals(world.dimension()) ||
                (gateway = MiscUtils.getBlockEntityAt(world, this.currentUI.getPos(), CelestialGatewayBlockEntity.class, true)) == null ||
                !gateway.doesSeeSky() ||
                !gateway.hasMultiblock()) {
            this.currentUI = null;
        }
        return this.currentUI == null;
    }

    // 1.16.5: RenderWorldLastEvent -> RenderLevelStageEvent (Stage.AFTER_LEVEL)
    void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        if (this.validate()) {
            return;
        }
        float pTicks = event.getPartialTick();
        PoseStack renderStack = event.getPoseStack();
        Vector3 renderOffset = this.currentUI.getRenderCenter();

        Player player = Minecraft.getInstance().player;
        double dst = renderOffset.distance(Vector3.atEntityCorner(player).addY(1.5));
        if(dst > 3) {
            return;
        }

        if (Minecraft.useShaderTransparency()) {
            //If you found this while wanting to report seeing stars behind blocks on the gateway:
            //Yes i am aware of it, it'd need a ton of work to fix. So i'll fix it eventually, not right now.
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        }

        this.renderGatewayShieldOverlay(renderStack, renderOffset, dst, pTicks);
        this.renderGatewayFocusedEntry(renderStack, renderOffset, pTicks);
        this.renderGatewayAllowedPlayers(renderStack, renderOffset, dst, pTicks);
    }

    private void renderGatewayAllowedPlayers(PoseStack renderStack, Vector3 renderOffset, double distance, float pTicks) {
        GatewayCache.GatewayNode node = this.currentUI.getThisGatewayNode();
        if (node == null || !node.isLocked() || node.getOwner() == null || node.getAllowedUsers().isEmpty()) {
            return;
        }
        UUID currentUUID = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getUUID() : null;
        HitResult mouseOverRtr = Minecraft.getInstance().hitResult;
        BlockPos blockSelected;
        if (mouseOverRtr != null && mouseOverRtr.getType() == HitResult.Type.BLOCK && mouseOverRtr instanceof BlockHitResult) {
            blockSelected = ((BlockHitResult) mouseOverRtr).getBlockPos().above();
        } else {
            blockSelected = null;
        }

        Color c = ColorsAS.CONSTELLATION_TYPE_MAJOR;
        float alpha = Mth.clamp(1F - ((float) (distance / 2D)), 0F, 1F);

        node.getAllowedUsers().forEach((index, playerRef) -> {
            BlockPos drawPos = CelestialGatewayBlockEntity.getAllowedUserOffset(index).offset(node.getPos());
            Vector3 at = new Vector3(drawPos)
                    .add(0.5, 0.001, 0.5)
                    .subtract(RenderingVectorUtils.getStandardTranslationRemovalVector(pTicks));
            IConstellation cst = this.getCurrentUI().getGeneratedConstellation(playerRef.getPlayerUUID());
            if (cst != null) {
                // TODO_PORT[AS-CLI-03]: RenderingConstellationUtils.renderConstellationIntoWorldFlat(Color, IConstellation, PoseStack, Vector3, double, double, float) - target RenderingConstellationUtils is a stub
                RenderingConstellationUtils.renderConstellationIntoWorldFlat(c, cst, renderStack, at, 1.2, 1, alpha);

                UUID targetUUID = playerRef.getPlayerUUID();
                if ((node.getOwner().getPlayerUUID().equals(currentUUID) || targetUUID.equals(currentUUID)) && drawPos.equals(blockSelected)) {
                    // TODO_PORT[AS-CLI-02]: RenderingUtils.renderInWorldText(Component, Color, float, Vector3, PoseStack, float, boolean) - target only has the String/no-scale stub
                    RenderingUtils.renderInWorldText(playerRef.getPlayerName(), c, 1 / 48F, at.clone().addY(0.2),
                            renderStack, pTicks, true);
                }
            }
        });
    }

    private void renderGatewayFocusedEntry(PoseStack renderStack, Vector3 renderOffset, float pTicks) {
        Player player = Minecraft.getInstance().player;
        GatewayUI.GatewayEntry entry = findMatchingEntry(Mth.wrapDegrees(player.getYRot()), Mth.wrapDegrees(player.getXRot()));
        if (entry != null) {
            Component display = entry.getNode().getDisplayName();
            if (display != null && !display.getString().isEmpty()) {
                Vector3 at = entry.getRelativePos().clone()
                        .add(renderOffset)
                        .addY(0.4F)
                        .subtract(RenderingVectorUtils.getStandardTranslationRemovalVector(pTicks));

                Color c = ColorsAS.CONSTELLATION_SINGLE_STAR;
                DyeColor nodeColor = entry.getNode().getColor();
                if (nodeColor != null) {
                    c = ColorUtils.flareColorFromDye(nodeColor);
                }

                // TODO_PORT[AS-CLI-02]: RenderingUtils.renderInWorldText(Component, Color, Vector3, PoseStack, float, boolean) - target only has the String stub
                RenderingUtils.renderInWorldText(display, c, at, renderStack, pTicks, true);
            }
        }
    }

    private void renderGatewayShieldOverlay(PoseStack renderStack, Vector3 renderOffset, double distance, float pTicks) {
        float alpha = Mth.clamp(1F - ((float) (distance / 2D)), 0F, 1F);
        Color c = ColorsAS.CONSTELLATION_SINGLE_STAR;
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();

        long seed = 0xA781B4F01C771923L;
        seed |= ((long) this.currentUI.getPos().getX()) << 48;
        seed |= ((long) this.currentUI.getPos().getY()) << 24;
        seed |= ((long) this.currentUI.getPos().getZ());
        Random rand = new Random(seed);

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        // 1.16.5: RenderSystem.enableTexture(); fixed-function texturing is replaced by the position_color_tex shader in 1.20.1
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        TexturesAS.TEX_STAR_1.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            for (int i = 0; i < 300; i++) {
                Vector3 at = Vector3.random(rand).normalize().multiply(this.currentUI.getSphereRadius() * 0.9).add(renderOffset);
                if (at.getY() >= this.currentUI.getPos().getY()) {
                    // TODO_PORT[AS-CLI-03]: RenderingConstellationUtils.conCFlicker(long, float, int) - target RenderingConstellationUtils is a stub
                    float a = RenderingConstellationUtils.conCFlicker(ClientScheduler.getClientTick(), pTicks, rand.nextInt(7) + 6);
                    a *= alpha;
                    RenderingDrawUtils.renderFacingFullQuadVB(buf, renderStack, at.getX(), at.getY(), at.getZ(),
                            0.07F, rand.nextFloat(), 255, 255, 255, (int) (a * 255F));
                }
            }
            for (GatewayUI.GatewayEntry entry : this.currentUI.getGatewayEntries()) {
                int r = red;
                int g = green;
                int b = blue;
                DyeColor nodeColor = entry.getNode().getColor();
                if (nodeColor != null) {
                    if (nodeColor == DyeColor.BLACK) {
                        nodeColor = DyeColor.GRAY; //Avoid practical invisibility
                    }
                    Color ovr = ColorUtils.flareColorFromDye(nodeColor);
                    r = ovr.getRed();
                    g = ovr.getGreen();
                    b = ovr.getBlue();
                }
                float a = RenderingConstellationUtils.conCFlicker(ClientScheduler.getClientTick(), pTicks, rand.nextInt(7) + 6);
                a = 0.4F + (0.6F * a);
                a *= alpha;
                RenderingDrawUtils.renderFacingFullQuadVB(buf, renderStack,
                        renderOffset.getX() + entry.getRelativePos().getX(),
                        renderOffset.getY() + entry.getRelativePos().getY(),
                        renderOffset.getZ() + entry.getRelativePos().getZ(),
                        0.16F, 0, r, g, b, (int) (a * 255F));
            }
        });

        RenderSystem.depthMask(true);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    @Nullable
    public GatewayUI.GatewayEntry findMatchingEntry(float yaw, float pitch) {
        float matchAccurancy = 4;
        for (GatewayUI.GatewayEntry entry : this.currentUI.getGatewayEntries()) {
            if(Math.abs(entry.getPitch() - pitch) < matchAccurancy &&
                    (Math.abs(entry.getYaw() - yaw) <= matchAccurancy || Math.abs(entry.getYaw() - yaw - 360F) <= matchAccurancy)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        if (this.currentUI != null) {
            this.currentUI.decrementVisibleTicks();
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.CLIENT);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "GatewayUI Render Handler";
    }
}
