/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.NavigationArrowScreen;
import hellfirepvp.astralsorcery.client.screen.base.SkyScreen;
import hellfirepvp.astralsorcery.client.screen.base.ConstellationDiscoveryBlockEntityScreen;
import hellfirepvp.astralsorcery.client.screen.telescope.TelescopeRotationDrawArea;
import hellfirepvp.astralsorcery.client.util.*;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktRotateTelescope;
import hellfirepvp.astralsorcery.common.blockentity.TelescopeBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenTelescope
 * Created by HellFirePvP
 * Date: 15.01.2020 / 17:16
 */
public class ScreenTelescope extends ConstellationDiscoveryBlockEntityScreen<TelescopeBlockEntity, TelescopeRotationDrawArea> implements SkyScreen, NavigationArrowScreen {

    private TelescopeBlockEntity.TelescopeRotation rotation;

    private Rectangle rectArrowCW = null, rectArrowCCW = null;

    public ScreenTelescope(TelescopeBlockEntity telescope) {
        //TODO_PORT[AS-SCR-02]: 1.16.5 called plain super(telescope, 280, 280) and let
        //TileConstellationDiscoveryScreen read the title off TileTelescope#getDisplayName()
        //(1.16.5 ScreenTelescope.java:54). This tree's TileTelescope does not implement NamedInventoryTile anymore,
        //so the exact component that tile returned - Component.translatable("screen.astralsorcery.telescope"),
        //1.16.5 common/tile/TileTelescope.java:59 - is passed in explicitly here. Revert to the two-value super call
        //once TileTelescope implements NamedInventoryTile again (see externalChangesNeeded).
        super(telescope, Component.translatable("screen.astralsorcery.telescope"), 280, 280);
        this.rotation = telescope.getRotation();
    }

    @Nonnull
    @Override
    protected List<TelescopeRotationDrawArea> createDrawAreas() {
        List<TelescopeRotationDrawArea> areas = new LinkedList<>();
        for (TelescopeBlockEntity.TelescopeRotation r : TelescopeBlockEntity.TelescopeRotation.values()) {
            areas.add(new TelescopeRotationDrawArea(this, r, this.getGuiBox()));
        }
        return areas;
    }

    @Override
    protected void fillConstellations(WorldContext ctx, List<TelescopeRotationDrawArea> drawAreas) {
        Random gen = ctx.getDayRandom();
        PlayerProgress prog = ResearchHelper.getClientProgress();

        List<IWeakConstellation> cst = new ArrayList<>();
        for (IConstellation active : ctx.getActiveCelestialsHandler().getCurrentRenderPositions().keySet()) {
            if (active instanceof IWeakConstellation && active.canDiscover(Minecraft.getInstance().player, prog)) {
                cst.add((IWeakConstellation) active);
            }
        }
        Collections.shuffle(cst, gen);
        cst = cst.subList(0, Math.min(drawAreas.size(), cst.size()));
        for (IWeakConstellation constellation : cst) {
            Point foundPoint;
            TelescopeRotationDrawArea associatedArea;
            do {
                associatedArea = MiscUtils.getRandomEntry(drawAreas, gen);
                foundPoint = findEmptySpace(gen, associatedArea);
            } while (foundPoint == null);
            associatedArea.addConstellationToArea(constellation, foundPoint, DEFAULT_CONSTELLATION_SIZE);
        }
    }

    private Point findEmptySpace(Random rand, TelescopeRotationDrawArea area) {
        int size = DEFAULT_CONSTELLATION_SIZE;
        int wdh = guiWidth  - 6 - size;
        int hgt = guiHeight - 6 - size;
        int rX = 6 + rand.nextInt(wdh);
        int rY = 6 + rand.nextInt(hgt);
        Rectangle constellationRect = new Rectangle(rX, rY, size, size);
        for (ConstellationDisplayInformation info : area.getDisplayMap().values()) {
            Point offset = info.getRenderPosition();
            Rectangle otherRect = new Rectangle(offset.x, offset.y, size, size);
            if (otherRect.intersects(constellationRect)) {
                return null;
            }
        }
        return new Point(rX, rY);
    }

    //1.16.5 received the raw MatrixStack; 1.20.1 hands screens a GuiGraphics that owns the very same pose stack.
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pTicks) {
        PoseStack renderStack = guiGraphics.pose();

        RenderSystem.enableDepthTest();
        super.render(guiGraphics, mouseX, mouseY, pTicks);

        this.drawWHRect(renderStack, TexturesAS.TEX_GUI_TELESCOPE);

        this.drawConstellationCell(renderStack, pTicks);

        this.drawNavArrows(renderStack, mouseX, mouseY, pTicks);
    }

    //1.16.5: MatrixStack -> 1.20.1: PoseStack
    private void drawNavArrows(PoseStack renderStack, int mouseX, int mouseY, float pTicks) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();

        this.rectArrowCCW = this.drawArrow(renderStack, guiLeft - 40, guiTop + (guiHeight / 2), this.getGuiZLevel(), Type.LEFT, mouseX, mouseY, pTicks);
        this.rectArrowCW = this.drawArrow(renderStack, guiLeft + guiWidth + 10, guiTop + (guiHeight / 2), this.getGuiZLevel(), Type.RIGHT, mouseX, mouseY, pTicks);

        RenderSystem.disableBlend();
    }

    private void drawConstellationCell(PoseStack renderStack, float pTicks) {
        //1.16.5: TileEntity#getPos() -> 1.20.1: BlockEntity#getBlockPos()
        boolean canSeeSky = this.canObserverSeeSky(this.getBlockEntity().getBlockPos(), 1);

        //1.16.5 ran RenderSystem.disableAlphaTest() here and RenderSystem.enableAlphaTest() at every exit of this
        //method. 1.20.1 removed the fixed-function alpha test entirely - the position_color_tex core shader used by
        //RenderingUtils/RenderingGuiUtils does its own alpha handling - so those calls drop out.
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();

        this.setBlitOffset(-10);
        this.drawSkyBackground(renderStack, pTicks, canSeeSky);

        if (!this.isInitialized()) {
            this.setBlitOffset(0);

            Blending.DEFAULT.apply();
            RenderSystem.disableBlend();
            return;
        }

        WorldContext ctx = this.getContext();
        if (ctx != null && canSeeSky) {
            Random gen = ctx.getDayRandom();
            PlayerProgress prog = ResearchHelper.getClientProgress();

            for (int i = 0; i < this.rotation.ordinal(); i++) {
                gen.nextFloat(); //Flush
            }

            this.setBlitOffset(-9);
            float starSize = 5F;
            TexturesAS.TEX_STAR_1.bindTexture();
            //1.16.5: DefaultVertexFormats -> 1.20.1: com.mojang.blaze3d.vertex.DefaultVertexFormat
            RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                for (int i = 0; i < 72 + gen.nextInt(108); i++) {
                    float innerOffsetX = starSize + gen.nextFloat() * (guiWidth  - starSize * 2) + this.getGuiLeft();
                    float innerOffsetY = starSize + gen.nextFloat() * (guiHeight - starSize * 2) + this.getGuiTop();
                    float brightness = 0.4F + (RenderingConstellationUtils.stdFlicker(ClientScheduler.getClientTick(), pTicks, 10 + gen.nextInt(20))) * 0.5F;
                    brightness = this.multiplyStarBrightness(pTicks, brightness);

                    RenderingGuiUtils.rect(buf, renderStack, this)
                            .at(innerOffsetX, innerOffsetY)
                            .dim(starSize, starSize)
                            .color(brightness, brightness, brightness, brightness)
                            .draw();
                }
            });

            this.setBlitOffset(-7);
            for (TelescopeRotationDrawArea area : this.getVisibleDrawAreas()) {
                for (IConstellation cst : area.getDisplayMap().keySet()) {
                    ConstellationDisplayInformation info = area.getDisplayMap().get(cst);
                    info.getFrameDrawInformation().clear();

                    Point pos = info.getRenderPosition();
                    int size = (int) info.getRenderSize();

                    //1.16.5: Minecraft#world#getRainStrength(float) -> 1.20.1: Minecraft#level#getRainLevel(float)
                    float rainBr = 1F - Minecraft.getInstance().level.getRainLevel(pTicks);
                    Map<StarLocation, Rectangle.Float> cstRenderInfo = RenderingConstellationUtils.renderConstellationIntoGUI(
                            cst, renderStack,
                            pos.x + guiLeft,
                            pos.y + guiTop,
                            this.getGuiZLevel(),
                            size, size,
                            2.5F,
                            () -> RenderingConstellationUtils.conCFlicker(ClientScheduler.getClientTick(), pTicks, 5 + gen.nextInt(15)) * rainBr,
                            prog.hasConstellationDiscovered(cst),
                            true);

                    info.getFrameDrawInformation().putAll(cstRenderInfo);
                }
            }

            this.setBlitOffset(-5);
            this.renderDrawnLines(renderStack, gen, pTicks);
        }

        this.setBlitOffset(0);

        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    private void drawSkyBackground(PoseStack renderStack, float pTicks, boolean canSeeSky) {
        Tuple<Color, Color> rgbFromTo = SkyScreen.getSkyGradient(canSeeSky, 1F, pTicks);
        RenderingDrawUtils.drawGradientRect(renderStack, this.getGuiZLevel(),
                this.guiLeft + 5, this.guiTop + 5,
                this.guiLeft + this.guiWidth - 5, this.guiTop + this.guiHeight - 5,
                rgbFromTo.getA().getRGB(), rgbFromTo.getB().getRGB());
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        Point p = new Point((int) mouseX, (int) mouseY);
        if (rectArrowCW != null && rectArrowCW.contains(p)) {
            //1.16.5: TileEntity#getWorld()#getDimensionKey() -> 1.20.1: BlockEntity#getLevel()#dimension();
            //        TileEntity#getPos() -> BlockEntity#getBlockPos()
            PktRotateTelescope pkt = new PktRotateTelescope(true, this.getBlockEntity().getLevel().dimension(), this.getBlockEntity().getBlockPos());
            PacketChannel.CHANNEL.sendToServer(pkt);
            return true;
        }
        if (rectArrowCCW != null && rectArrowCCW.contains(p)) {
            PktRotateTelescope pkt = new PktRotateTelescope(false, this.getBlockEntity().getLevel().dimension(), this.getBlockEntity().getBlockPos());
            PacketChannel.CHANNEL.sendToServer(pkt);
            return true;
        }
        return false;
    }

    //VERIFY_PORT[AS-SCR-03]: 1.16.5's PktRotateTelescope handler called this on the client after the server echoed
    //the packet back, which is what advanced the locally displayed rotation and cleared the drawn lines
    //(1.16.5 common/network/play/client/PktRotateTelescope.java). This tree's packet only calls
    //TileTelescope#setRotation and never notifies the open screen, so nothing invokes this method yet - the screen
    //would keep rendering the old rotation cell. See externalChangesNeeded for the required packet-side hook.
    public void handleRotationChange(boolean isClockwise) {
        this.rotation = isClockwise ? rotation.nextClockWise() : rotation.nextCounterClockWise();
        this.clearDrawing();
    }

    public TelescopeBlockEntity.TelescopeRotation getRotation() {
        return rotation;
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return true;
    }

    @Override
    protected boolean isMouseRotatingGui() {
        return false;
    }
}
