/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.container;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.screen.base.ScreenContainerAltar;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.container.ContainerAltarAttunement;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenContainerAltarAttunement
 * Created by HellFirePvP
 * Date: 15.08.2019 / 17:26
 */
public class ScreenContainerAltarAttunement extends ScreenContainerAltar<ContainerAltarAttunement> {

    //1.16.5: PlayerInventory -> 1.20.1: Inventory; ITextComponent -> Component
    public ScreenContainerAltarAttunement(ContainerAltarAttunement screenContainer, Inventory inv, Component name) {
        super(screenContainer, inv, name, 256, 202);
    }

    @Override
    public AbstractRenderableTexture getBackgroundTexture() {
        return TexturesAS.TEX_CONTAINER_ALTAR_ATTUNEMENT;
    }

    //1.16.5: ContainerScreen#drawGuiContainerForegroundLayer(MatrixStack, int, int)
    //  -> 1.20.1: AbstractContainerScreen#renderLabels(GuiGraphics, int, int)
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PoseStack renderStack = guiGraphics.pose();

        //1.16.5: SimpleAltarRecipe -> this tree: AltarRecipe
        AltarRecipe recipe = this.findRecipe(false);
        if (recipe != null) {
            ItemStack out = this.getOutputForRender(recipe);
            //1.16.5: MatrixStack#push/#pop -> 1.20.1: PoseStack#pushPose/#popPose
            renderStack.pushPose();
            renderStack.translate(190, 35, 0);
            renderStack.scale(2.5F, 2.5F, 1F);

            RenderingUtils.renderItemStackGUI(renderStack, out, null);

            renderStack.popPose();
        }
    }

    @Override
    public void renderGuiBackground(PoseStack renderStack, float partialTicks, int mouseX, int mouseY) {
        this.renderStarlightBar(renderStack, 11, 104, 232, 10);
    }
}
