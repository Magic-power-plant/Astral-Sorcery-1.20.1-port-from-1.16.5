/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: InputScreen
 * Created by HellFirePvP
 * Date: 10.08.2019 / 17:18
 */
public class InputScreen extends Screen {

    private final Set<Integer> heldKeys = new HashSet<>();

    private double oMouseX, oMouseY;
    private boolean dragging = false;

    //1.16.5: AbstractGui#blitOffset, inherited by Screen and read back through Screen#getBlitOffset().
    //1.20.1 deleted GuiComponent along with that field (gui depth is a PoseStack translation on GuiGraphics now),
    //but every AS screen feeds the value into its own draw helpers as an explicit z coordinate, exactly like
    //1.16.5 did. So the field lives on the AS screen root instead, keeping the 1.16.5 accessor names and its
    //default value of 0.
    private int blitOffset = 0;

    protected InputScreen(Component name) {
        super(name);
    }

    /**
     * 1.16.5 equivalent: {@code Screen#getBlitOffset()} (inherited from {@code AbstractGui}).
     */
    public int getBlitOffset() {
        return this.blitOffset;
    }

    /**
     * 1.16.5 equivalent: {@code Screen#setBlitOffset(int)} (inherited from {@code AbstractGui}).
     */
    public void setBlitOffset(int blitOffset) {
        this.blitOffset = blitOffset;
    }

    @Override
    public void tick() {
        heldKeys.forEach(this::keyPressedTick);

        super.tick();
    }

    protected void keyPressedTick(int key) {}

    protected void mouseDragStart(double mouseX, double mouseY) {}

    protected void mouseDragStop(double mouseX, double mouseY, double mouseDiffX, double mouseDiffY) {}

    protected void mouseDragTick(double mouseX, double mouseY, double mouseDiffX, double mouseDiffY, double mouseOffsetX, double mouseOffsetY) {}

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        heldKeys.add(key);
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int key, int scanCode, int modifiers) {
        heldKeys.remove(key);
        return super.keyReleased(key, scanCode, modifiers);
    }

    public boolean isCurrentlyDragging() {
        return this.dragging;
    }

    protected void stopDragging(double mouseX, double mouseY) {
        if (this.dragging) {
            this.dragging = false;
            this.mouseDragStop(mouseX, mouseY, oMouseX, oMouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int click) {
        if (click == 0) {
            this.dragging = true;
            this.oMouseX = mouseX;
            this.oMouseY = mouseY;
            this.mouseDragStart(mouseX, mouseY);
        }
        return super.mouseClicked(mouseX, mouseY, click);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int click) {
        if (click == 0) {
            this.stopDragging(mouseX, mouseY);
        }
        return super.mouseReleased(mouseX, mouseY, click);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickType, double offsetX, double offsetY) {
        if (clickType == 0 && this.dragging) {
            double diffX = this.oMouseX - mouseX;
            double diffY = this.oMouseY - mouseY;
            this.mouseDragTick(mouseX, mouseY, diffX, diffY, offsetX, offsetY);
        }
        return super.mouseDragged(mouseX, mouseY, clickType, offsetX, offsetY);
    }
}
