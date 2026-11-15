/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.progression;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.resource.query.TextureQuery;
import hellfirepvp.astralsorcery.client.screen.base.WidthHeightScreen;
import hellfirepvp.astralsorcery.client.screen.helper.ScalingPoint;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPages;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalClusterRenderer
 * Created by HellFirePvP
 * Date: 03.08.2019 / 18:06
 */
public class ScreenJournalClusterRenderer {

    //TODO_PORT[AS-JCR-01]: 1.16.5's ResearchNode carries the frame texture itself
    // (private TextureQuery backgroundTextureQuery = new TextureQuery(AssetLoader.TextureLocation.GUI, "research_frame_wood");
    //  + public TextureQuery getBackgroundTexture(), 1.16.5 common/data/research/ResearchNode.java:45/151).
    // The ported ResearchNode has neither the field nor the getter, so renderNodeToGUI cannot ask the node for its
    // frame. 1.16.5 never reassigns that field (there is no setter), so every node resolves to exactly this query -
    // it is inlined here to keep the render identical until ResearchNode regains the member.
    private static final TextureQuery NODE_BACKGROUND_TEXTURE = new TextureQuery(AssetLoader.TextureLocation.GUI, "research_frame_wood");

    private final Map<ResearchNode.SpriteIcon, SpriteSheetResource> spriteTextures = new HashMap<>();

    private ProgressionSizeHandler progressionSizeHandler;
    private ResearchProgression progression;
    private ScalingPoint mousePointScaled;
    private ScalingPoint previousMousePointScaled;

    private int renderOffsetX, renderOffsetY;
    private int renderGuiHeight, renderGuiWidth;
    private boolean hasPrevOffset = false;

    private float alpha = 1F;

    private Map<Rectangle, ResearchNode> clickableNodes = new HashMap<>();

    public ScreenJournalClusterRenderer(ResearchProgression progression, int guiHeight, int guiWidth, int guiLeft, int guiTop) {
        this.progression = progression;
        this.progressionSizeHandler = new ProgressionSizeHandler(progression);
        this.progressionSizeHandler.setMaxScale(1.2F);
        this.progressionSizeHandler.setMinScale(0.1F);
        this.progressionSizeHandler.setScaleSpeed(0.9F / 20F);
        this.progressionSizeHandler.updateSize();
        this.progressionSizeHandler.forceScaleTo(0.1F);

        this.mousePointScaled = ScalingPoint.createPoint(0, 0, this.progressionSizeHandler.getScalingFactor(), false);
        this.centerMouse();
        this.applyMovedMouseOffset();

        this.renderOffsetX = guiLeft;
        this.renderOffsetY = guiTop;
        this.renderGuiHeight = guiHeight;
        this.renderGuiWidth = guiWidth;
    }

    public boolean propagateClick(ScreenJournalProgression parent, double mouseX, double mouseY) {
        Rectangle frame = new Rectangle(renderOffsetX, renderOffsetY, renderGuiWidth, renderGuiHeight);
        if (frame.contains(mouseX, mouseY)) {
            for (Rectangle r : clickableNodes.keySet()) {
                if (r.contains(mouseX, mouseY)) {
                    ResearchNode clicked = clickableNodes.get(r);
                    //1.16.5: Minecraft#displayGuiScreen -> 1.20.1: Minecraft#setScreen
                    Minecraft.getInstance().setScreen(new ScreenJournalPages(parent, clicked));
                    return true;
                }
            }
        }
        return false;
    }

    public void drawMouseHighlight(PoseStack renderStack, float zLevel, int mouseX, int mouseY) {
        Rectangle frame = new Rectangle(renderOffsetX, renderOffsetY, renderGuiWidth, renderGuiHeight);
        if (frame.contains(mouseX, mouseY)) {
            for (Rectangle r : clickableNodes.keySet()) {
                if (r.contains(mouseX, mouseY)) {
                    //1.16.5: ITextProperties -> 1.20.1: FormattedText
                    FormattedText name = clickableNodes.get(r).getName();

                    renderStack.pushPose();
                    renderStack.translate(r.getX(), r.getY(), zLevel + 200);
                    renderStack.scale(progressionSizeHandler.getScalingFactor(), progressionSizeHandler.getScalingFactor(), 1F);
                    //1.16.5: Minecraft#fontRenderer -> 1.20.1: Minecraft#font
                    RenderingDrawUtils.renderBlueTooltipComponents(renderStack, 0, 0, 0, Lists.newArrayList(name), Minecraft.getInstance().font, false);
                    renderStack.popPose();
                }
            }
        }
    }

    public void centerMouse() {
        Point.Float center = this.progressionSizeHandler.getRelativeCenter();
        this.moveMouse(center.x, center.y);
    }

    public void moveMouse(float changedX, float changedY) {
        if (hasPrevOffset) {
            mousePointScaled.updateScaledPos(
                    progressionSizeHandler.clampX(previousMousePointScaled.getScaledPosX() + changedX),
                    progressionSizeHandler.clampY(previousMousePointScaled.getScaledPosY() + changedY),
                    progressionSizeHandler.getScalingFactor());
        } else {
            mousePointScaled.updateScaledPos(
                    progressionSizeHandler.clampX(changedX),
                    progressionSizeHandler.clampY(changedY),
                    progressionSizeHandler.getScalingFactor());
        }
    }

    public void applyMovedMouseOffset() {
        this.previousMousePointScaled = ScalingPoint.createPoint(
                mousePointScaled.getScaledPosX(),
                mousePointScaled.getScaledPosY(),
                progressionSizeHandler.getScalingFactor(),
                true);
        this.hasPrevOffset = true;
    }

    public void handleZoomOut() {
        this.progressionSizeHandler.handleZoomOut();
        rescale(progressionSizeHandler.getScalingFactor());
    }

    public void handleZoomIn() {
        this.progressionSizeHandler.handleZoomIn();
        rescale(progressionSizeHandler.getScalingFactor());
    }

    public float getMouseX() {
        return mousePointScaled.getPosX();
    }

    public float getMouseY() {
        return mousePointScaled.getPosY();
    }

    private void rescale(float newScale) {
        this.mousePointScaled.rescale(newScale);
        if (this.previousMousePointScaled != null) {
            this.previousMousePointScaled.rescale(newScale);
        }
        moveMouse(0, 0);
    }

    public void drawClusterScreen(PoseStack renderStack, WidthHeightScreen parentGui, float zLevel) {
        clickableNodes.clear();

        drawNodesAndConnections(renderStack, parentGui, zLevel);
    }

    private void drawNodesAndConnections(PoseStack renderStack, WidthHeightScreen parentGui, float zLevel) {
        alpha = progressionSizeHandler.getScalingFactor(); //between 0.25F and ~1F
        alpha -= 0.25F;
        alpha /= 0.75F;
        //1.16.5: MathHelper -> 1.20.1: Mth
        alpha = Mth.clamp(alpha, 0F, 1F);

        Map<ResearchNode, Point.Float> displayPositions = new HashMap<>();
        for (ResearchNode node : progression.getResearchNodes()) {
            if (!node.canSee(ResearchHelper.getClientProgress())) {
                continue;
            }
            Point.Float from = this.progressionSizeHandler.scalePointToGui(parentGui, this.mousePointScaled, new Point.Float(node.renderPosX, node.renderPosZ));
            for (ResearchNode target : node.getConnectionsTo()) {
                Point.Float to = this.progressionSizeHandler.scalePointToGui(parentGui, this.mousePointScaled, new Point.Float(target.renderPosX, target.renderPosZ));
                drawConnection(renderStack, from.x, from.y, to.x, to.y, zLevel);
            }

            displayPositions.put(node, from);
        }
        displayPositions.forEach((node, pos) -> renderNodeToGUI(renderStack, node, pos, zLevel));
    }

    private void renderNodeToGUI(PoseStack renderStack, ResearchNode node, Point.Float offset, float zLevel) {
        float zoomedWH = progressionSizeHandler.getZoomedWHNode();
        float offsetX = offset.x - zoomedWH / 2F;
        float offsetY = offset.y - zoomedWH / 2F;

        //1.16.5: node.getBackgroundTexture().resolve().bindTexture(); see AS-JCR-01 above.
        NODE_BACKGROUND_TEXTURE.resolve().bindTexture();
        if (progressionSizeHandler.getScalingFactor() >= 0.7) {
            clickableNodes.put(new Rectangle(Mth.floor(offsetX), Mth.floor(offsetY), Mth.floor(zoomedWH), Mth.floor(zoomedWH)), node);
        }
        drawResearchItemBackground(zoomedWH, offsetX, offsetY, zLevel);

        float pxWH = progressionSizeHandler.getZoomedWHNode() / 16F;

        switch (node.getNodeRenderType()) {
            case ITEMSTACK:
                renderStack.pushPose();
                renderStack.translate(offsetX, offsetY, 0);
                renderStack.scale(progressionSizeHandler.getScalingFactor(), progressionSizeHandler.getScalingFactor(), 1);
                renderStack.translate(3, 3, 100);
                renderStack.scale(0.75F, 0.75F, 1);

                //1.16.5 wrapped this in RenderHelper.enableStandardItemLighting()/disableStandardItemLighting().
                //Both were deleted with the 1.17 render rewrite (RenderHelper became com.mojang.blaze3d.platform.Lighting,
                //which only exposes setupForFlatItems/setupFor3DItems/...). RenderingUtils#renderTranslucentItemStackModelGUI
                //already performs exactly that flat/3D item lighting switch around the model batch, so the GUI item
                //stays lit the same way without the removed calls.
                RenderingUtils.renderTranslucentItemStackModelGUI(node.getRenderItemStack(ClientScheduler.getClientTick()),
                        renderStack, Color.WHITE, Blending.DEFAULT, Mth.clamp((int) (alpha * 255F), 0, 255));

                renderStack.popPose();
                break;
            case TEXTURE_SPRITE:
                //AS-JCR-02 (resolved): 1.16.5 client/screen/journal/progression/ScreenJournalClusterRenderer.java:223-265.
                //The note was accurate when written - ResearchNode had neither member nor the SpriteQuery
                //constructor - and all three have now been restored, so this branch is reachable again. It is the
                //only renderer for NodeRenderType.TEXTURE_SPRITE, whose single user is the QUICK_CHARGE node.
                Color col = node.getTextureColorHint();

                float r = (col.getRed() / 255F) * alpha;
                float g = (col.getGreen() / 255F) * alpha;
                float b = (col.getBlue() / 255F) * alpha;
                float a = (col.getAlpha() / 255F) * alpha;

                SpriteSheetResource res = spriteTextures.computeIfAbsent(node.getSpriteTexture(), icon ->
                        new SpriteSheetResource(AssetLibrary.loadTexture(icon.texture()), icon.rows(), icon.columns()));
                res.getResource().bindTexture();
                Tuple<Float, Float> uvTexture = res.getUVOffset(ClientScheduler.getClientTick());

                renderStack.pushPose();
                renderStack.translate(offsetX, offsetY, 0);

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                //1.16.5 relied on fixed-function texturing; 1.20.1 needs the position_color_tex core shader
                //selected explicitly, as every other textured draw in this port does.
                RenderSystem.setShader(GameRenderer::getPositionColorTexShader);

                RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                    //1.16.5: MatrixStack#getLast()#getMatrix() -> 1.20.1: PoseStack#last()#pose()
                    Matrix4f matr = renderStack.last().pose();
                    buf.vertex(matr, pxWH, zoomedWH - pxWH, zLevel)
                            .color(r, g, b, a)
                            .uv(uvTexture.getA(), uvTexture.getB() + res.getVLength())
                            .endVertex();
                    buf.vertex(matr, zoomedWH - pxWH, zoomedWH - pxWH, zLevel)
                            .color(r, g, b, a)
                            .uv(uvTexture.getA() + res.getULength(), uvTexture.getB() + res.getVLength())
                            .endVertex();
                    buf.vertex(matr, zoomedWH - pxWH, pxWH, zLevel)
                            .color(r, g, b, a)
                            .uv(uvTexture.getA() + res.getULength(), uvTexture.getB())
                            .endVertex();
                    buf.vertex(matr, pxWH, pxWH, zLevel)
                            .color(r, g, b, a)
                            .uv(uvTexture.getA(), uvTexture.getB())
                            .endVertex();
                });

                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();

                renderStack.popPose();
                break;
            default:
                break;
        }
    }

    private void drawConnection(PoseStack renderStack, float originX, float originY, float targetX, float targetY, float zLevel) {
        //1.16.5: RenderSystem.disableTexture(); - the fixed-function texture toggle does not exist in 1.20.1;
        //the untextured POSITION_COLOR core shader selected by RenderingUtils#draw takes its place.
        RenderSystem.enableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        RenderSystem.lineWidth(4F);

        long clientTicks = ClientScheduler.getClientTick();
        Vector3 origin = new Vector3(originX, originY, 0);
        Vector3 line = origin.vectorFromHereTo(targetX, targetY, 0);
        int segments = (int) Math.ceil(line.length() / 1); //1 = max line segment length
        int activeSegment = (int) (clientTicks % segments);
        Vector3 segmentIter = line.divide(segments);
        RenderingUtils.draw(GL11.GL_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR, buf -> {
            for (int i = segments; i >= 0; i--) {
                double lx = origin.getX();
                double ly = origin.getY();
                origin.add(segmentIter);

                float brightness = 0.6F;
                brightness += (0.4F * evaluateBrightness(i, activeSegment));

                drawLinePart(buf, renderStack, lx, ly, origin.getX(), origin.getY(), zLevel, brightness);
            }
        });

        RenderSystem.lineWidth(2.0F);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableBlend();
        //1.16.5: RenderSystem.enableTexture(); - removed in 1.20.1 along with the rest of the fixed-function state.
    }

    //1.16.5: IVertexBuilder -> 1.20.1: VertexConsumer
    private void drawLinePart(VertexConsumer buf, PoseStack renderStack, double lx, double ly, double hx, double hy, float zLevel, float brightness) {
        //1.16.5: MatrixStack#getLast()#getMatrix() -> 1.20.1: PoseStack#last()#pose() (org.joml.Matrix4f)
        Matrix4f offset = renderStack.last().pose();
        buf.vertex(offset, (float) lx, (float) ly, zLevel)
                .color(brightness * alpha, brightness * alpha, brightness * alpha, 0.4F * alpha)
                .endVertex();
        buf.vertex(offset, (float) hx, (float) hy, zLevel)
                .color(brightness * alpha, brightness * alpha, brightness * alpha, 0.4F * alpha)
                .endVertex();
    }

    private float evaluateBrightness(int segment, int activeSegment) {
        if (segment == activeSegment) return 1.0F;
        float res = ((float) (10 - Math.abs(activeSegment - segment))) / 10F;
        return Math.max(0, res);
    }

    private void drawResearchItemBackground(double zoomedWH, double xAdd, double yAdd, float zLevel) {
        RenderSystem.enableBlend();
        //1.16.5: DefaultVertexFormats -> 1.20.1: DefaultVertexFormat; RenderingUtils#draw selects the matching core shader.
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            buf.vertex(xAdd,            yAdd + zoomedWH, zLevel).color(alpha, alpha, alpha, alpha).uv(0, 1).endVertex();
            buf.vertex(xAdd + zoomedWH, yAdd + zoomedWH, zLevel).color(alpha, alpha, alpha, alpha).uv(1, 1).endVertex();
            buf.vertex(xAdd + zoomedWH, yAdd,            zLevel).color(alpha, alpha, alpha, alpha).uv(1, 0).endVertex();
            buf.vertex(xAdd,            yAdd,            zLevel).color(alpha, alpha, alpha, alpha).uv(0, 0).endVertex();
        });
        RenderSystem.disableBlend();
    }
}
