/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.opengl.GL11;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenCustomContainer
 * Created by HellFirePvP
 * Date: 15.08.2019 / 14:56
 */
//1.16.5: Container -> 1.20.1: AbstractContainerMenu; ContainerScreen -> AbstractContainerScreen;
//net.minecraft.client.gui.IHasContainer -> net.minecraft.client.gui.screens.inventory.MenuAccess
public abstract class ScreenCustomContainer<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements MenuAccess<T> {

    private final int sWidth, sHeight;

    //1.16.5: AbstractGui#blitOffset, inherited by ContainerScreen and read back through getBlitOffset().
    //1.20.1 deleted GuiComponent along with that field, but the AS draw helpers below still take the gui depth as
    //an explicit z coordinate, exactly like 1.16.5 did. So the field lives here instead, keeping the 1.16.5
    //accessor names and its default value of 0 - the same translation InputScreen uses for the non-container screens.
    private int blitOffset = 0;

    //1.16.5: PlayerInventory -> 1.20.1: Inventory; ITextComponent -> Component
    public ScreenCustomContainer(T screenContainer, Inventory inv, Component name, int width, int height) {
        super(screenContainer, inv, name);
        this.sWidth = width;
        this.sHeight = height;
    }

    public abstract AbstractRenderableTexture getBackgroundTexture();

    /**
     * 1.16.5 equivalent: {@code ContainerScreen#getBlitOffset()} (inherited from {@code AbstractGui}).
     */
    public int getBlitOffset() {
        return this.blitOffset;
    }

    /**
     * 1.16.5 equivalent: {@code ContainerScreen#setBlitOffset(int)} (inherited from {@code AbstractGui}).
     */
    public void setBlitOffset(int blitOffset) {
        this.blitOffset = blitOffset;
    }

    @Override
    protected void init() {
        //1.16.5: ContainerScreen#xSize/#ySize -> 1.20.1: AbstractContainerScreen#imageWidth/#imageHeight
        this.imageWidth = sWidth;
        this.imageHeight = sHeight;
        super.init();
    }

    //1.16.5: IHasContainer#getContainer() -> 1.20.1: MenuAccess#getMenu()
    @Override
    public T getMenu() {
        return this.menu;
    }


    //1.16.5 received the raw MatrixStack; 1.20.1 hands screens a GuiGraphics that owns the very same pose stack.
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, pTicks);
        //1.16.5: ContainerScreen#renderHoveredTooltip -> 1.20.1: AbstractContainerScreen#renderTooltip
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    //1.16.5: ContainerScreen#drawGuiContainerBackgroundLayer -> 1.20.1: AbstractContainerScreen#renderBg
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        PoseStack renderStack = guiGraphics.pose();

        this.getBackgroundTexture().bindTexture();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        //1.16.5: DefaultVertexFormats -> 1.20.1: com.mojang.blaze3d.vertex.DefaultVertexFormat
        //1.16.5: ContainerScreen#guiLeft/#guiTop -> 1.20.1: AbstractContainerScreen#leftPos/#topPos
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, this.leftPos, this.topPos, this.getBlitOffset(), this.sWidth, this.sHeight).draw();
        });
    }
}
