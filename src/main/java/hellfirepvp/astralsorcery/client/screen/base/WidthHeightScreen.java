/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WidthHeightScreen
 * Created by HellFirePvP
 * Date: 02.08.2019 / 20:33
 */
public class WidthHeightScreen extends InputScreen {

    protected final int guiHeight;
    protected final int guiWidth;
    protected int guiLeft, guiTop;

    protected boolean closeWithInventoryKey = true;

    //1.16.5: ITextComponent -> 1.20.1: Component
    protected WidthHeightScreen(Component titleIn, int guiHeight, int guiWidth) {
        super(titleIn);
        this.guiHeight = guiHeight;
        this.guiWidth = guiWidth;
    }

    /**
     * TODO_PORT[AS-WHS-01]: transitional constructor that does not exist in the 1.16.5 reference. Every
     * {@code WidthHeightScreen} subclass in this tree (ScreenJournal, ScreenObservatory, ScreenHandTelescope,
     * ScreenTelescope, ScreenRefractionTable, ScreenConstellationPaper, ScreenJournalOverlay, TileEntityScreen,
     * ConstellationDiscoveryScreen, ...) is still an un-ported stub without a constructor and therefore relies on
     * an accessible no-arg super constructor. Adding the reference's three-argument constructor alone would break
     * all of them, and those files are owned by other porting passes. Delete this constructor once every subclass
     * declares its own constructor and calls {@link #WidthHeightScreen(Component, int, int)} with the 1.16.5
     * title/height/width triple.
     */
    @Deprecated
    protected WidthHeightScreen() {
        this(Component.empty(), 0, 0);
    }

    @Override
    protected void init() {
        super.init();

        this.initComponents();
    }

    public int getGuiLeft() {
        return this.guiLeft;
    }

    public int getGuiTop() {
        return this.guiTop;
    }

    /**
     * 1.16.5 returned {@code Screen#getBlitOffset()}, i.e. the {@code AbstractGui#blitOffset} field (default 0,
     * changed by the journal/telescope screens through {@code setBlitOffset}). 1.20.1 removed {@code GuiComponent}
     * and that field entirely, so the very same value now comes from {@link InputScreen#getBlitOffset()}, which
     * carries the ported field with identical default and accessor names.
     */
    public int getGuiZLevel() {
        return this.getBlitOffset();
    }

    public int getGuiWidth() {
        return this.guiWidth;
    }

    public int getGuiHeight() {
        return this.guiHeight;
    }

    public Rectangle getGuiBox() {
        return new Rectangle(this.getGuiLeft(), this.getGuiTop(), this.getGuiWidth(), this.getGuiHeight());
    }

    private void initComponents() {
        //Screen#width/Screen#height keep their 1.16.5 names in 1.20.1
        guiLeft = width / 2 - guiWidth / 2;
        guiTop = height / 2 - guiHeight / 2;
    }

    //1.16.5: MatrixStack -> 1.20.1: PoseStack
    protected void drawWHRect(PoseStack renderStack, AbstractRenderableTexture resource) {
        //Whoever disables blending on GUI overlays, your states bleed into following GUIs
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //1.16.5 additionally ran RenderSystem.enableAlphaTest()/defaultAlphaFunc() here and disableAlphaTest()
        //after the draw. The fixed-function alpha test does not exist in 1.20.1 anymore - the core shaders used by
        //RenderingGuiUtils (position_color_tex) do the alpha handling themselves - so those three calls drop out.

        resource.bindTexture();
        RenderingGuiUtils.drawRect(renderStack, guiLeft, guiTop, this.getBlitOffset(), guiWidth, guiHeight);
    }

    /**
     * 1.20.1 hands screens a {@link GuiGraphics} instead of the bare {@code MatrixStack} of 1.16.5; this overload
     * unwraps the pose stack it owns so ported subclasses can forward their render argument directly.
     */
    protected void drawWHRect(GuiGraphics guiGraphics, AbstractRenderableTexture resource) {
        this.drawWHRect(guiGraphics.pose(), resource);
    }

    @Override
    public boolean charTyped(char charCode, int keyModifiers) {
        if (super.charTyped(charCode, keyModifiers)) {
            return true;
        }

        //1.16.5: Minecraft#gameSettings#keyBindInventory#isKeyDown() -> 1.20.1: Minecraft#options#keyInventory#isDown()
        if (closeWithInventoryKey && Minecraft.getInstance().options.keyInventory.isDown()) {
            this.onClose(); //1.16.5: Screen#closeScreen()

            //1.16.5: Minecraft#currentScreen/#mouseHelper -> 1.20.1: Minecraft#screen/#mouseHandler
            if (Minecraft.getInstance().screen == null) {
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 1 && shouldRightClickCloseScreen(mouseX, mouseY)) {
            this.onClose(); //1.16.5: Screen#closeScreen()

            if (Minecraft.getInstance().screen == null) {
                Minecraft.getInstance().mouseHandler.grabMouse();
            }
            return true;
        }
        return false;
    }

    /**
     * @return false if rightclick shouldn't close the current screen, true if it should close the current screen
     */
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return false;
    }
}
