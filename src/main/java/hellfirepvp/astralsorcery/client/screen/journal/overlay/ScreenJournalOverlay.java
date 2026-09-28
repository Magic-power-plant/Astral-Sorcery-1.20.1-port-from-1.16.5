/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.overlay;

import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournal;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPerkTree;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalOverlay
 * Created by HellFirePvP
 * Date: 04.08.2019 / 09:11
 */
public abstract class ScreenJournalOverlay extends ScreenJournal {

    private final ScreenJournal origin;

    //1.16.5: ITextComponent -> 1.20.1: Component
    protected ScreenJournalOverlay(Component titleIn, ScreenJournal origin) {
        super(titleIn, origin.getGuiHeight(), origin.getGuiWidth(), NO_BOOKMARK);
        this.origin = origin;
    }

    @Override
    public boolean isPauseScreen() {
        return origin.isPauseScreen();
    }

    //1.16.5 overrode Screen#init(Minecraft, int, int) to run the origin screen's init alongside its own.
    //That method is 'public final' in 1.20.1 - every (re-)init now funnels through the protected no-arg
    //Screen#init() (directly, or via resize/repositionElements -> rebuildWidgets -> init()), so the origin
    //is re-initialized from here with the very same minecraft/width/height the final method would have set.
    @Override
    protected void init() {
        super.init();

        origin.init(this.minecraft, this.width, this.height);
    }

    //1.16.5: Screen#render(MatrixStack, int, int, float) -> 1.20.1: Screen#render(GuiGraphics, int, int, float)
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pTicks) {
        super.render(guiGraphics, mouseX, mouseY, pTicks);

        origin.render(guiGraphics, 0, 0, pTicks);
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return true;
    }

    //1.16.5: Screen#closeScreen() (the "user requested this screen to go away" hook) -> 1.20.1: Screen#onClose()
    @Override
    public void onClose() {
        //1.16.5: Minecraft#displayGuiScreen -> 1.20.1: Minecraft#setScreen
        Minecraft.getInstance().setScreen(this.origin);
    }

    //1.16.5: Screen#onClose() (the "this screen was taken off screen" hook) -> 1.20.1: Screen#removed()
    @Override
    public void removed() {
        super.removed();

        if (origin instanceof ScreenJournalProgression) {
            ((ScreenJournalProgression) origin).expectReInit();
        }

        if (origin instanceof ScreenJournalPerkTree) {
            ((ScreenJournalPerkTree) origin).expectReinit = true;
        }
    }

    @Override
    public boolean charTyped(char charCode, int keyModifiers) {
        if (super.charTyped(charCode, keyModifiers)) {
            return true;
        }

        //1.16.5: Minecraft#currentScreen -> 1.20.1: Minecraft#screen
        if (Minecraft.getInstance().screen != this && Minecraft.getInstance().screen != origin) {
            Minecraft.getInstance().setScreen(origin);
            return true;
        }
        return false;
    }
}
