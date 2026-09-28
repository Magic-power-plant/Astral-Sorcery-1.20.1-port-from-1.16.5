/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.container;

import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.screen.base.ScreenCustomContainer;
import hellfirepvp.astralsorcery.common.container.ContainerTome;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenContainerTome
 * Created by HellFirePvP
 * Date: 15.08.2019 / 14:38
 */
public class ScreenContainerTome extends ScreenCustomContainer<ContainerTome> {

    //1.16.5: PlayerInventory -> 1.20.1: Inventory; ITextComponent -> Component
    public ScreenContainerTome(ContainerTome screenContainer, Inventory inv, Component name) {
        super(screenContainer, inv, name, 176, 166);
    }

    @Override
    public AbstractRenderableTexture getBackgroundTexture() {
        return TexturesAS.TEX_CONTAINER_TOME_STORAGE;
    }

    //1.16.5: ContainerScreen#drawGuiContainerForegroundLayer(MatrixStack, int, int)
    //  -> 1.20.1: AbstractContainerScreen#renderLabels(GuiGraphics, int, int). Both are the hook that draws the
    //             container/inventory titles, so overriding it empty suppresses them in either version.
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int x, int y) {
        //Don't draw container names. (Currently all this does)
    }
}
