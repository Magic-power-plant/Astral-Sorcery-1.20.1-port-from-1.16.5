/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.client.util.sound.ClientSoundHelper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.screen.base.WidthHeightScreen;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingConstellationUtils;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.fml.LogicalSide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenConstellationPaper
 * Created by HellFirePvP
 * Date: 02.08.2019 / 20:32
 */
public class ScreenConstellationPaper extends WidthHeightScreen {

    private final IConstellation constellation;
    private List<MoonPhase> phases = null;

    public ScreenConstellationPaper(IConstellation cst) {
        super(cst.getConstellationName(), 344, 275);
        this.constellation = cst;
        resolvePhases();
    }

    private void resolvePhases() {
        //1.16.5: Minecraft#world -> 1.20.1: Minecraft#level
        WorldContext ctx = SkyHandler.getContext(Minecraft.getInstance().level, LogicalSide.CLIENT);
        if (ctx != null) {
            phases = new ArrayList<>();
            for (MoonPhase phase : MoonPhase.values()) {
                if (ctx.getConstellationHandler().isActiveInPhase(this.constellation, phase)) {
                    phases.add(phase);
                }
            }
        }
    }

    /**
     * 1.16.5 overrode {@code Screen#onClose()}, which was the "this screen just got removed/replaced" callback.
     * 1.20.1 renamed that callback to {@code Screen#removed()} and gave the name {@code onClose()} to what 1.16.5
     * called {@code closeScreen()} (the "please close me" request). The close sound therefore has to hang off
     * {@link #removed()} here to keep firing exactly when the 1.16.5 screen played it.
     */
    @Override
    public void removed() {
        super.removed();
        ClientSoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_CLOSE, 1F, 1F);
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    //1.16.5 received the raw MatrixStack; 1.20.1 hands screens a GuiGraphics that owns the very same pose stack.
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pTicks) {
        PoseStack renderStack = guiGraphics.pose();

        RenderSystem.enableDepthTest();
        drawWHRect(renderStack, TexturesAS.TEX_GUI_CONSTELLATION_PAPER);
        drawHeader(renderStack);
        drawConstellation(renderStack);
        drawPhaseInformation(renderStack);
    }

    //1.16.5: MatrixStack -> 1.20.1: PoseStack
    private void drawHeader(PoseStack renderStack) {
        //1.16.5: IFormattableTextComponent -> 1.20.1: MutableComponent; IConstellation#getConstellationName() is
        //typed as the immutable Component supertype in this tree, which is all that is read here.
        Component name = this.constellation.getConstellationName();
        //1.16.5: FontRenderer#getStringPropertyWidth(ITextProperties) -> 1.20.1: Font#width(FormattedText)
        float length = font.width(name) * 1.8F;
        double offsetLeft = (width >> 1) - (length / 2);
        int offsetTop = guiTop + 45;

        renderStack.pushPose(); //1.16.5: MatrixStack#push()
        renderStack.translate(offsetLeft + 2, offsetTop, this.getGuiZLevel());
        renderStack.scale(1.8F, 1.8F, 1F);
        RenderingDrawUtils.renderStringAt(name, renderStack, font, 0xAA4D4D4D, false);
        renderStack.popPose(); //1.16.5: MatrixStack#pop()
    }

    private void drawConstellation(PoseStack renderStack) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();

        RenderingConstellationUtils.renderConstellationIntoGUI(ColorsAS.CONSTELLATION_TYPE_BLANK,
                constellation, renderStack,
                width / 2F - 145 / 2F, guiTop + 84,
                this.getGuiZLevel(),
                145, 145, 2F, () -> 0.5F,
                true, false);

        RenderSystem.disableBlend();
    }

    private void drawPhaseInformation(PoseStack renderStack) {
        if (this.phases == null) {
            this.resolvePhases();
        }

        List<MoonPhase> phases = this.phases == null ? Collections.emptyList() : this.phases;
        if (phases.isEmpty()) {
            //1.16.5: ITextProperties -> 1.20.1: FormattedText; TranslationTextComponent -> Component.translatable
            FormattedText text = Component.translatable("astralsorcery.journal.constellation.unknown");
            //1.16.5: Minecraft#fontRenderer -> 1.20.1: Minecraft#font
            RenderingDrawUtils.renderStringCentered(Minecraft.getInstance().font, renderStack,
                    text, guiLeft + guiWidth / 2 + 25, guiTop + 239,
                    1.8F, 0xAA4D4D4D);
        } else {
            int size = 16;
            int offsetX = (width / 2) - (phases.size() * (size + 2)) / 2;
            int offsetY = guiTop + 237;
            for (int i = 0; i < phases.size(); i++) {
                //1.16.5 called MoonPhase#getTexture(), an @OnlyIn(Dist.CLIENT) helper on the enum. This tree's
                //MoonPhase is a pure common-side enum without it, so the identical lookup
                //(AssetLibrary.loadTexture(MISC, "moon_" + name().toLowerCase(Locale.ROOT))) is inlined here.
                //See externalChangesNeeded: restoring MoonPhase#getTexture() would let this read like the reference.
                AssetLibrary.loadTexture(AssetLoader.TextureLocation.MISC,
                        "moon_" + phases.get(i).name().toLowerCase(Locale.ROOT)).bindTexture();
                RenderSystem.enableBlend();
                Blending.DEFAULT.apply();
                RenderingGuiUtils.drawRect(renderStack, offsetX + (i * (size + 2)), offsetY, this.getGuiZLevel(), size, size);
                RenderSystem.disableBlend();
            }
        }
    }
}
