/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import hellfirepvp.astralsorcery.common.container.ContainerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerBaseScreen
 * Created by HellFirePvP
 * Date: 03.08.2019 / 16:08
 */
//1.16.5: TileEntity -> 1.20.1: BlockEntity; ContainerScreen -> AbstractContainerScreen
public abstract class ContainerBaseScreen<T extends BlockEntity, C extends ContainerBlockEntity<T>> extends AbstractContainerScreen<C> {

    //1.16.5: PlayerInventory -> 1.20.1: Inventory; ITextComponent -> Component
    public ContainerBaseScreen(C screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    //1.16.5 received the raw MatrixStack; 1.20.1 hands screens a GuiGraphics that owns the very same pose stack.
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, pTicks);
        //1.16.5: ContainerScreen#renderHoveredTooltip -> 1.20.1: AbstractContainerScreen#renderTooltip
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * 1.16.5 overrode {@code Screen#tick()} here. In 1.20.1 {@code AbstractContainerScreen#tick()} is final - it
     * closes the container when the player died and otherwise delegates to the new {@code containerTick()} hook -
     * so the 1.16.5 body moves into that hook verbatim. {@code super.containerTick()} is the empty vanilla default
     * that the final {@code tick()} would have called anyway, so ordering and behaviour are unchanged.
     */
    @Override
    protected void containerTick() {
        super.containerTick();

        //1.16.5: ContainerScreen#container -> 1.20.1: AbstractContainerScreen#menu
        BlockEntity te = this.menu.getBlockEntity();
        //1.16.5: Container#canInteractWith(PlayerEntity) -> 1.20.1: AbstractContainerMenu#stillValid(Player)
        if (te.isRemoved() || !this.menu.stillValid(Minecraft.getInstance().player)) {
            this.onClose(); //1.16.5: Screen#closeScreen()
        }
    }
}
