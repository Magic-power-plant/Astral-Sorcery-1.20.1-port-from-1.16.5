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
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.TileEntityScreen;
import hellfirepvp.astralsorcery.client.util.*;
import hellfirepvp.astralsorcery.common.constellation.DrawnConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravedStarMap;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktEngraveGlass;
import hellfirepvp.astralsorcery.common.tile.TileRefractionTable;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.LogicalSide;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenRefractionTable
 * Created by HellFirePvP
 * Date: 27.04.2020 / 21:07
 */
public class ScreenRefractionTable extends TileEntityScreen<TileRefractionTable> {

    private static final Rectangle PLACEMENT_GRID = new Rectangle(
            68 + DrawnConstellation.CONSTELLATION_DRAW_SIZE, 45 + DrawnConstellation.CONSTELLATION_DRAW_SIZE,
            120 - (DrawnConstellation.CONSTELLATION_DRAW_SIZE * 2), 120 - (DrawnConstellation.CONSTELLATION_DRAW_SIZE * 2));

    private final Map<Rectangle, IConstellation> mapRenderedConstellations = new HashMap<>();

    private final List<DrawnConstellation> currentlyDrawnConstellations = new ArrayList<>();
    private IConstellation dragging = null;

    public ScreenRefractionTable(TileRefractionTable tile) {
        super(tile, 188, 256);
    }

    //1.16.5 received the raw MatrixStack; 1.20.1 hands screens a GuiGraphics that owns the very same pose stack.
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pTicks) {
        PoseStack renderStack = guiGraphics.pose();

        RenderSystem.enableDepthTest();
        super.render(guiGraphics, mouseX, mouseY, pTicks);
        this.mapRenderedConstellations.clear();

        if (this.getTile().hasParchment()) {
            this.drawWHRect(renderStack, TexturesAS.TEX_GUI_REFRACTION_TABLE_PARCHMENT);
        } else {
            this.drawWHRect(renderStack, TexturesAS.TEX_GUI_REFRACTION_TABLE_EMPTY);
        }

        //1.16.5: TileEntity#getWorld() -> 1.20.1: BlockEntity#getLevel()
        if (DayTimeHelper.getCurrentDaytimeDistribution(this.getTile().getLevel()) <= 0.05 || !this.getTile().hasParchment()) {
            this.currentlyDrawnConstellations.clear();
            this.dragging = null;
        }

        //1.16.5: ITextProperties -> 1.20.1: FormattedText
        List<FormattedText> tooltip = new ArrayList<>();
        //1.16.5: FontRenderer -> 1.20.1: Font; Minecraft#fontRenderer -> Minecraft#font
        Font tooltipRenderer = Minecraft.getInstance().font;

        tooltipRenderer = this.renderTileItems(renderStack, mouseX, mouseY, tooltip, tooltipRenderer);
        this.renderConstellationOptions(renderStack, mouseX, mouseY, tooltip);
        this.renderRunningHalo(renderStack);
        this.renderInputItem(renderStack);
        this.renderDrawnConstellations(renderStack, mouseX, mouseY, tooltip);
        this.renderDraggedConstellations(renderStack);
        this.renderDragging(renderStack, mouseX, mouseY);

        if (!tooltip.isEmpty()) {
            this.setBlitOffset(510);
            RenderingDrawUtils.renderBlueTooltipComponents(renderStack, mouseX, mouseY, this.getGuiZLevel(), tooltip, tooltipRenderer, true);
            this.setBlitOffset(0);
        }
    }

    //1.16.5: MatrixStack -> 1.20.1: PoseStack
    private void renderDragging(PoseStack renderStack, int mouseX, int mouseY) {
        if (this.dragging == null) {
            return;
        }

        int whDrawn = DrawnConstellation.CONSTELLATION_DRAW_SIZE;
        Point offset = new Point(mouseX, mouseY);
        offset.translate(-whDrawn, -whDrawn);

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderingConstellationUtils.renderConstellationIntoGUI(dragging, renderStack, offset.x, offset.y, this.getGuiZLevel(),
                whDrawn * 2, whDrawn * 2, 1.4F,
                () -> DayTimeHelper.getCurrentDaytimeDistribution(this.getTile().getLevel()), true, false);
        RenderSystem.disableBlend();

        this.renderBox(renderStack, offset.x, offset.y, whDrawn * 2, whDrawn * 2, dragging.getTierRenderColor());
        Rectangle r = new Rectangle(PLACEMENT_GRID);
        r.grow(DrawnConstellation.CONSTELLATION_DRAW_SIZE, DrawnConstellation.CONSTELLATION_DRAW_SIZE);
        r.translate(guiLeft, guiTop);
        this.renderBox(renderStack, r.x, r.y, r.width, r.height, dragging.getTierRenderColor());
    }

    private void renderDraggedConstellations(PoseStack renderStack) {
        int whDrawn = DrawnConstellation.CONSTELLATION_DRAW_SIZE;
        for (DrawnConstellation dragged : this.currentlyDrawnConstellations) {
            Point offset = new Point(dragged.getPoint());
            offset.translate(guiLeft, guiTop);
            offset.translate(PLACEMENT_GRID.x, PLACEMENT_GRID.y);
            offset.translate(-whDrawn, -whDrawn);

            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderingConstellationUtils.renderConstellationIntoGUI(dragged.getConstellation(), renderStack,
                    offset.x, offset.y, this.getGuiZLevel(),
                    whDrawn * 2, whDrawn * 2, 1.4F,
                    () -> DayTimeHelper.getCurrentDaytimeDistribution(this.getTile().getLevel()), true, false);
            RenderSystem.disableBlend();
        }
    }

    private void renderInputItem(PoseStack renderStack) {
        if (this.getTile().getInputStack().isEmpty() || this.getTile().hasParchment()) {
            return;
        }

        this.setBlitOffset(100);
        ItemStack input = this.getTile().getInputStack();
        RenderSystem.disableDepthTest();

        renderStack.pushPose(); //1.16.5: MatrixStack#push()
        renderStack.translate(guiLeft + 63 + 16.25, guiTop + 42 + 16.25, getGuiZLevel());
        renderStack.scale(6F, 6F, 1F);

        RenderingUtils.renderItemStackGUI(renderStack, input, null);

        renderStack.popPose(); //1.16.5: MatrixStack#pop()

        RenderSystem.enableDepthTest();
        this.setBlitOffset(0);
    }

    private void renderRunningHalo(PoseStack renderStack) {
        if (!(this.getTile().getRunProgress() > 0)) {
            return;
        }

        SpritesAS.SPR_HALO_INFUSION.bindTexture();
        Tuple<Float, Float> uvFrame = SpritesAS.SPR_HALO_INFUSION.getUVOffset(ClientScheduler.getClientTick());

        float scale = 160F;

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        //1.16.5 ran RenderSystem.disableAlphaTest() here and RenderSystem.enableAlphaTest() after the draw. 1.20.1
        //removed the fixed-function alpha test entirely - the position_color_tex core shader that
        //RenderingUtils#setupShaderFor selects does its own alpha handling - so those two calls drop out.

        renderStack.pushPose();
        renderStack.translate(guiWidth / 2F, guiHeight / 2F, 0);
        renderStack.scale(-scale / 2, -scale / 2, 1);

        //1.16.5: DefaultVertexFormats -> 1.20.1: com.mojang.blaze3d.vertex.DefaultVertexFormat
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, this)
                    .dim(scale, scale)
                    .color(1F, 1F, 1F, this.getTile().getRunProgress())
                    .tex(uvFrame.getA(), uvFrame.getB(), SpritesAS.SPR_HALO_INFUSION.getUWidth(), SpritesAS.SPR_HALO_INFUSION.getVWidth())
                    .draw();
        });

        renderStack.popPose();

        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    private void renderDrawnConstellations(PoseStack renderStack, int mouseX, int mouseY, List<FormattedText> tooltip) {
        ItemStack glass = this.getTile().getGlassStack();
        if (glass.isEmpty()) {
            return;
        }
        //1.16.5: World -> 1.20.1: Level; TileEntity#getWorld() -> BlockEntity#getLevel()
        Level world = this.getTile().getLevel();
        float nightPerc = DayTimeHelper.getCurrentDaytimeDistribution(world);
        WorldContext ctx = SkyHandler.getContext(world, LogicalSide.CLIENT);
        if (ctx == null || !this.getTile().doesSeeSky() || nightPerc <= 0.05F) {
            return;
        }
        EngravedStarMap map = ItemInfusedGlass.getEngraving(glass);
        if (map == null) {
            return;
        }
        for (DrawnConstellation cst : map.getDrawnConstellations()) {
            int whDrawn = DrawnConstellation.CONSTELLATION_DRAW_SIZE;
            Point offset = new Point(cst.getPoint());
            offset.translate(guiLeft, guiTop);
            offset.translate(PLACEMENT_GRID.x, PLACEMENT_GRID.y);
            offset.translate(-whDrawn, -whDrawn);

            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderingConstellationUtils.renderConstellationIntoGUI(cst.getConstellation(), renderStack,
                    offset.x, offset.y, this.getGuiZLevel(),
                    whDrawn * 2, whDrawn * 2, 1.6F,
                    () -> DayTimeHelper.getCurrentDaytimeDistribution(world) * 0.8F, true, false);
            RenderSystem.disableBlend();
        }
    }

    private void renderConstellationOptions(PoseStack renderStack, int mouseX, int mouseY, List<FormattedText> tooltip) {
        ItemStack glass = this.getTile().getGlassStack();
        if (glass.isEmpty()) {
            return;
        }
        Level world = this.getTile().getLevel();
        float nightPerc = DayTimeHelper.getCurrentDaytimeDistribution(world);
        WorldContext ctx = SkyHandler.getContext(world, LogicalSide.CLIENT);
        if (ctx == null || !this.getTile().doesSeeSky() || nightPerc <= 0.05F) {
            return;
        }
        List<IConstellation> cstList = ctx.getActiveCelestialsHandler().getActiveConstellations()
                .stream()
                .filter(c -> ResearchHelper.getClientProgress().hasConstellationDiscovered(c))
                .collect(Collectors.toList());

        //1.16.5: World#getDimensionKey() -> 1.20.1: Level#dimension()
        Random rand = new Random(WorldSeedCache.getSeedIfPresent(world.dimension()).orElse(0x515F1EB654AB915EL));
        for (int i = 0; i < ctx.getConstellationHandler().getLastTrackedDay(); i++) {
            rand.nextLong();
        }
        Collections.shuffle(cstList, rand);

        for (int i = 0; i < Math.min(cstList.size(), 12); i++) {
            IConstellation cst = cstList.get(i);
            int offsetX = guiLeft + (i % 2 == 0 ? 8 : 232);
            int offsetY = guiTop + (40 + (i / 2) * 23);

            Rectangle rct = new Rectangle(offsetX, offsetY, 16, 16);
            this.mapRenderedConstellations.put(rct, cst);

            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderingConstellationUtils.renderConstellationIntoGUI(Color.WHITE, cst, renderStack,
                    offsetX, offsetY, this.getGuiZLevel(),
                    16, 16, 0.5,
                    () -> DayTimeHelper.getCurrentDaytimeDistribution(world), true, false);
            RenderSystem.disableBlend();

            if (rct.contains(mouseX, mouseY)) {
                tooltip.add(cst.getConstellationName());
            }
        }
    }

    //1.16.5: FontRenderer -> 1.20.1: Font
    private Font renderTileItems(PoseStack renderStack, int mouseX, int mouseY, List<FormattedText> tooltip, Font tooltipRenderer) {
        this.setBlitOffset(100);

        ItemStack input = this.getTile().getInputStack();
        if (!input.isEmpty()) {
            Rectangle itemRct = new Rectangle(guiLeft + 111, guiTop + 8, 16, 16);
            renderStack.pushPose();
            renderStack.translate(itemRct.x, itemRct.y, getGuiZLevel());
            RenderingUtils.renderItemStackGUI(renderStack, input, null);
            renderStack.popPose();

            if (itemRct.contains(mouseX, mouseY)) {
                //1.16.5: Item#getFontRenderer(ItemStack) -> 1.20.1 Forge: IClientItemExtensions#getFont(..)
                Font custom = IClientItemExtensions.of(input).getFont(input, IClientItemExtensions.FontContext.TOOLTIP);
                if (custom != null) {
                    tooltipRenderer = custom;
                }
                //1.16.5: ItemStack#getTooltip -> 1.20.1: ItemStack#getTooltipLines;
                //        Minecraft#gameSettings -> Minecraft#options;
                //        ITooltipFlag.TooltipFlags.ADVANCED/NORMAL -> TooltipFlag.ADVANCED/NORMAL
                tooltip.addAll(input.getTooltipLines(getMinecraft().player, Minecraft.getInstance().options.advancedItemTooltips ?
                        TooltipFlag.ADVANCED : TooltipFlag.NORMAL));
            }
        }
        ItemStack glass = this.getTile().getGlassStack();
        if (!glass.isEmpty()) {
            Rectangle itemRct = new Rectangle(guiLeft + 129, guiTop + 8, 16, 16);
            renderStack.pushPose();
            renderStack.translate(itemRct.x, itemRct.y, getGuiZLevel());
            RenderingUtils.renderItemStackGUI(renderStack, glass, null);
            renderStack.popPose();

            if (itemRct.contains(mouseX, mouseY)) {
                Font custom = IClientItemExtensions.of(glass).getFont(glass, IClientItemExtensions.FontContext.TOOLTIP);
                if (custom != null) {
                    tooltipRenderer = custom;
                }
                tooltip.addAll(glass.getTooltipLines(getMinecraft().player, Minecraft.getInstance().options.advancedItemTooltips ?
                        TooltipFlag.ADVANCED : TooltipFlag.NORMAL));
            }
        }

        this.setBlitOffset(0);
        return tooltipRenderer;
    }

    private void renderBox(PoseStack renderStack, float offsetX, float offsetY, float width, float height, Color c) {
        Random rand = new Random(0x12);
        float r = c.getRed() / 255F;
        float g = c.getGreen() / 255F;
        float b = c.getBlue() / 255F;
        //1.16.5: MathHelper -> 1.20.1: Mth
        Supplier<Float> alpha = () -> 0.1F + 0.4F * ((Mth.sin(rand.nextInt(200) + ClientScheduler.getClientTick() / 20F) + 1F) / 2F);

        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        //1.16.5 ran RenderSystem.disableAlphaTest()/enableAlphaTest() and RenderSystem.disableTexture()/
        //enableTexture() around this draw. Neither the fixed-function alpha test nor the texture enable toggle
        //exists in 1.20.1 anymore; RenderingUtils#draw selects the position_color core shader for
        //DefaultVertexFormat.POSITION_COLOR, which never samples a texture, so the untextured lines come out the
        //same and those four calls drop out.
        RenderSystem.lineWidth(2F);
        RenderSystem.disableDepthTest();

        RenderingUtils.draw(GL11.GL_LINES, DefaultVertexFormat.POSITION_COLOR, buf -> {
            //1.16.5: MatrixStack#getLast()#getMatrix() -> 1.20.1: PoseStack#last()#pose() (org.joml.Matrix4f)
            Matrix4f offset = renderStack.last().pose();
            //1.16.5: IVertexBuilder#pos(Matrix4f, x, y, z) -> 1.20.1: VertexConsumer#vertex(Matrix4f, x, y, z)
            buf.vertex(offset, offsetX, offsetY, 0).color(r, g, b, alpha.get()).endVertex();
            buf.vertex(offset, offsetX +width, offsetY, 0).color(r, g, b, alpha.get()).endVertex();

            buf.vertex(offset, offsetX + width, offsetY, 0).color(r, g, b, alpha.get()).endVertex();
            buf.vertex(offset, offsetX + width, offsetY + height, 0).color(r, g, b, alpha.get()).endVertex();

            buf.vertex(offset, offsetX + width, offsetY + height, 0).color(r, g, b, alpha.get()).endVertex();
            buf.vertex(offset, offsetX, offsetY + height, 0).color(r, g, b, alpha.get()).endVertex();

            buf.vertex(offset, offsetX, offsetY + height, 0).color(r, g, b, alpha.get()).endVertex();
            buf.vertex(offset, offsetX, offsetY, 0).color(r, g, b, alpha.get()).endVertex();
        });

        RenderSystem.enableDepthTest();
        Blending.DEFAULT.apply();
        RenderSystem.disableBlend();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.currentlyDrawnConstellations.size() >= 3) {
            List<DrawnConstellation> copyList = new ArrayList<>(this.currentlyDrawnConstellations);

            PktEngraveGlass engraveGlass = new PktEngraveGlass(
                    //1.16.5: World#getDimensionKey() -> 1.20.1: Level#dimension();
                    //        TileEntity#getPos() -> BlockEntity#getBlockPos()
                    this.getTile().getLevel().dimension(),
                    this.getTile().getBlockPos(), copyList);
            PacketChannel.CHANNEL.sendToServer(engraveGlass);
            this.currentlyDrawnConstellations.clear();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 0 &&
                dragging == null &&
                this.getTile().hasParchment() &&
                this.getTile().hasUnengravedGlass() &&
                this.currentlyDrawnConstellations.size() < 3) {
            tryPick(mouseX, mouseY);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int click) {
        if (super.mouseReleased(mouseX, mouseY, click)) {
            return true;
        }

        if (click == 0 &&
                dragging != null &&
                this.getTile().hasParchment() &&
                this.getTile().hasUnengravedGlass() &&
                this.currentlyDrawnConstellations.size() < 3) {
            tryDrop(mouseX, mouseY);
        }
        return false;
    }

    private void tryDrop(double mouseX, double mouseY) {
        if (this.dragging != null) {
            if (PLACEMENT_GRID.contains(mouseX - guiLeft, mouseY - guiTop)) {
                Point gridPoint = new Point((int) Math.round(mouseX), (int) Math.round(mouseY));
                gridPoint.translate(-this.guiLeft, -this.guiTop);
                gridPoint.translate(-PLACEMENT_GRID.x, -PLACEMENT_GRID.y);

                this.currentlyDrawnConstellations.add(new DrawnConstellation(gridPoint, dragging));
            }
            this.dragging = null;
        }
    }

    private void tryPick(double mouseX, double mouseY) {
        for (Rectangle r : mapRenderedConstellations.keySet()) {
            if (r.contains(mouseX, mouseY)) {
                dragging = mapRenderedConstellations.get(r);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
