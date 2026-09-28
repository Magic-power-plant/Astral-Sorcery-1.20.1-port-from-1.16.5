/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.base.NavigationArrowScreen;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageAltarRecipe;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderablePage;
import hellfirepvp.astralsorcery.client.util.*;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.fml.LogicalSide;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalConstellationDetail
 * Created by HellFirePvP
 * Date: 04.08.2019 / 10:54
 */
public class ScreenJournalConstellationDetail extends ScreenJournal implements NavigationArrowScreen {

    private final ScreenJournal origin;
    private final IConstellation constellation;
    private final boolean detailed;

    private int doublePageID = 0;
    private int doublePages = 0;
    private List<MoonPhase> activePhases = null;

    private RenderablePage lastFramePage = null;
    private Rectangle rectBack, rectNext, rectPrev;

    //1.16.5: IReorderingProcessor -> 1.20.1: FormattedCharSequence
    private final List<FormattedCharSequence> locTextMain = new ArrayList<>();
    private final List<FormattedCharSequence> locTextRitual = new ArrayList<>();
    private final List<FormattedCharSequence> locTextRefraction = new ArrayList<>();
    private final List<FormattedCharSequence> locTextMantle = new ArrayList<>();

    public ScreenJournalConstellationDetail(ScreenJournal origin, IConstellation cst) {
        super(cst.getConstellationName(), NO_BOOKMARK);
        this.origin = origin;
        this.constellation = cst;

        //1.16.5: Minecraft#fontRenderer -> 1.20.1: Minecraft#font
        this.font = Minecraft.getInstance().font;

        this.detailed = ResearchHelper.getClientProgress().hasConstellationDiscovered(cst);

        PlayerProgress playerProgress = ResearchHelper.getClientProgress();
        if (this.detailed) {
            if (playerProgress.getTierReached().isThisLaterOrEqual(ProgressionTier.ATTUNEMENT)) {
                this.doublePages++;
            }
            if (playerProgress.getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                if (!(constellation instanceof IMinorConstellation)) {
                    this.doublePages++; //mantle info pages
                }
                this.doublePages++; //constellation paper page
            }
        }

        testActivePhases();
        buildMainText();
        buildEnchText();
        buildRitualText();
        buildCapeText();
    }

    public IConstellation getConstellation() {
        return constellation;
    }

    private void buildCapeText() {
        if (this.constellation instanceof IWeakConstellation) {
            if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                Component txtMantle = ((IWeakConstellation) this.constellation).getInfoMantleEffect();

                FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.mantle");
                locTextMantle.add(localize(headTxt));
                locTextMantle.add(FormattedCharSequence.EMPTY); //1.16.5: IReorderingProcessor#field_242232_a

                List<FormattedCharSequence> lines = new LinkedList<>();
                for (String segment : txtMantle.getString().split("<NL>")) {
                    //1.16.5: FontRenderer#trimStringToWidth(ITextProperties, int) -> 1.20.1: Font#split(FormattedText, int)
                    lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                    lines.add(FormattedCharSequence.EMPTY);
                }
                locTextMantle.addAll(lines);
                locTextMantle.add(FormattedCharSequence.EMPTY);
            }
        }
    }

    private void buildEnchText() {
        if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.CONSTELLATION_CRAFT)) {
            Component txtEnchantments = this.constellation.getConstellationEnchantmentDescription();

            FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.enchantments");
            locTextRefraction.add(localize(headTxt));
            locTextRefraction.add(FormattedCharSequence.EMPTY);

            List<FormattedCharSequence> lines = new LinkedList<>();
            for (String segment : txtEnchantments.getString().split("<NL>")) {
                lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                lines.add(FormattedCharSequence.EMPTY);
            }
            locTextRefraction.addAll(lines);
            locTextRefraction.add(FormattedCharSequence.EMPTY);
        }
    }

    private void buildRitualText() {
        if (this.constellation instanceof IMinorConstellation) {
            if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                Component txtRitual = ((IMinorConstellation) this.constellation).getInfoTraitEffect();

                FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.ritual.trait");
                locTextRitual.add(localize(headTxt));
                locTextRitual.add(FormattedCharSequence.EMPTY);

                List<FormattedCharSequence> lines = new LinkedList<>();
                for (String segment : txtRitual.getString().split("<NL>")) {
                    lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                    lines.add(FormattedCharSequence.EMPTY);
                }
                locTextRitual.addAll(lines);
            }
        } else if (this.constellation instanceof IWeakConstellation) {
            if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.ATTUNEMENT)) {
                Component txtRitual = ((IWeakConstellation) this.constellation).getInfoRitualEffect();

                FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.ritual");
                locTextRitual.add(localize(headTxt));
                locTextRitual.add(FormattedCharSequence.EMPTY);

                List<FormattedCharSequence> lines = new LinkedList<>();
                for (String segment : txtRitual.getString().split("<NL>")) {
                    lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                    lines.add(FormattedCharSequence.EMPTY);
                }
                locTextRitual.addAll(lines);
                locTextRitual.add(FormattedCharSequence.EMPTY);
            }
            if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                Component txtCorruptedRitual = ((IWeakConstellation) this.constellation).getInfoCorruptedRitualEffect();

                FormattedText headTxt = Component.translatable("astralsorcery.journal.constellation.corruption");
                locTextRitual.add(localize(headTxt));
                locTextRitual.add(FormattedCharSequence.EMPTY);

                List<FormattedCharSequence> lines = new LinkedList<>();
                for (String segment : txtCorruptedRitual.getString().split("<NL>")) {
                    lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
                    lines.add(FormattedCharSequence.EMPTY);
                }
                locTextRitual.addAll(lines);
                locTextRitual.add(FormattedCharSequence.EMPTY);
            }
        }
    }

    private void buildMainText() {
        Component txtDescription = this.constellation.getConstellationDescription();

        List<FormattedCharSequence> lines = new LinkedList<>();
        for (String segment : txtDescription.getString().split("<NL>")) {
            lines.addAll(font.split(Component.literal(segment), JournalPage.DEFAULT_WIDTH));
            lines.add(FormattedCharSequence.EMPTY);
        }
        locTextMain.addAll(lines);
    }

    private void testActivePhases() {
        //1.16.5: Minecraft#world -> 1.20.1: Minecraft#level
        WorldContext ctx = SkyHandler.getContext(Minecraft.getInstance().level, LogicalSide.CLIENT);
        if (ctx == null) {
            return;
        }
        this.activePhases = new LinkedList<>();
        for (MoonPhase phase : MoonPhase.values()) {
            if (ctx.getConstellationHandler().isActiveInPhase(this.constellation, phase)) {
                this.activePhases.add(phase);
            }
        }
    }

    //1.16.5: Screen#render(MatrixStack, int, int, float) -> 1.20.1: Screen#render(GuiGraphics, int, int, float);
    //the MatrixStack the 1.16.5 code drew with is the pose stack owned by the GuiGraphics.
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pTicks) {
        PoseStack renderStack = guiGraphics.pose();
        this.lastFramePage = null;

        if (this.doublePageID == 0) {
            drawCstBackground(renderStack);
            drawDefault(renderStack, TexturesAS.TEX_GUI_BOOK_FRAME_LEFT, mouseX, mouseY);
        } else {
            drawDefault(renderStack, TexturesAS.TEX_GUI_BOOK_BLANK, mouseX, mouseY);
        }

        drawNavArrows(renderStack, pTicks, mouseX, mouseY);

        this.setBlitOffset(120);
        switch (doublePageID) {
            case 0:
                drawPageConstellation(renderStack, pTicks);
                drawPagePhaseInformation(renderStack);
                drawPageExtendedInformation(renderStack);
                break;
            case 1:
                drawRefractionTableInformation(renderStack, mouseX, mouseY, pTicks);
                break;
            case 2:
                drawCapeInformationPages(renderStack, mouseX, mouseY, pTicks);
                if (this.constellation instanceof IMinorConstellation) { //Doesn't have a 3rd double page
                    drawConstellationPaperRecipePage(renderStack, mouseX, mouseY, pTicks);
                }
                break;
            case 3:
                drawConstellationPaperRecipePage(renderStack, mouseX, mouseY, pTicks);
                break;
            default:
                break;
        }
        this.setBlitOffset(0);
    }

    private void drawRefractionTableInformation(PoseStack renderStack, int mouseX, int mouseY, float pTicks) {
        for (int i = 0; i < locTextRitual.size(); i++) {
            FormattedCharSequence line = locTextRitual.get(i);
            renderStack.pushPose(); //1.16.5: MatrixStack#push()
            renderStack.translate(guiLeft + 30, guiTop + 30 + i * 10, this.getGuiZLevel());
            RenderingDrawUtils.renderStringAt(line, renderStack, font, 0xFFCCCCCC, true);
            renderStack.popPose(); //1.16.5: MatrixStack#pop()
        }
        for (int i = 0; i < locTextRefraction.size(); i++) {
            FormattedCharSequence line = locTextRefraction.get(i);
            renderStack.pushPose();
            renderStack.translate(guiLeft + 220, guiTop + 30 + i * 10, this.getGuiZLevel());
            RenderingDrawUtils.renderStringAt(line, renderStack, font, 0xFFCCCCCC, true);
            renderStack.popPose();
        }
    }

    private void drawCapeInformationPages(PoseStack renderStack, int mouseX, int mouseY, float partialTicks) {
        for (int i = 0; i < locTextMantle.size(); i++) {
            FormattedCharSequence line = locTextMantle.get(i);
            renderStack.pushPose();
            renderStack.translate(guiLeft + 30, guiTop + 30 + i * 10, this.getGuiZLevel());
            RenderingDrawUtils.renderStringAt(line, renderStack, font, 0xFFCCCCCC, true);
            renderStack.popPose();
        }

        if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
            //1.16.5 addressed the item directly through ItemsAS; in 1.20.1 ItemsAS holds RegistryObject<Item>,
            //so the registered instance has to be unwrapped and cast to reach ItemMantle#getConstellation.
            AltarRecipe recipe = RecipeHelper.findAltarRecipeResult(stack ->
                    stack.getItem() instanceof ItemMantle &&
                            this.constellation.equals(((ItemMantle) ItemsAS.MANTLE.get()).getConstellation(stack)));

            if (recipe != null) {
                lastFramePage = new RenderPageAltarRecipe(null, -1, recipe);
                lastFramePage.render    (renderStack, guiLeft + 220, guiTop + 20, this.getGuiZLevel(), partialTicks, mouseX, mouseY);
                lastFramePage.postRender(renderStack, guiLeft + 220, guiTop + 20, this.getGuiZLevel(), partialTicks, mouseX, mouseY);
            }
        }
    }

    private void drawConstellationPaperRecipePage(PoseStack renderStack, int mouseX, int mouseY, float partialTicks) {
        if (ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
            AltarRecipe recipe = RecipeHelper.findAltarRecipeResult(stack ->
                    stack.getItem() instanceof ItemConstellationPaper &&
                    this.constellation.equals(((ItemConstellationPaper) ItemsAS.CONSTELLATION_PAPER.get()).getConstellation(stack)));

            if (recipe != null) {
                lastFramePage = new RenderPageAltarRecipe(null, -1, recipe);
                lastFramePage.render    (renderStack, guiLeft + 30, guiTop + 20, this.getGuiZLevel(), partialTicks, mouseX, mouseY);
                lastFramePage.postRender(renderStack, guiLeft + 30, guiTop + 20, this.getGuiZLevel(), partialTicks, mouseX, mouseY);
            }
        }
    }

    private void drawPageExtendedInformation(PoseStack renderStack) {
        //1.16.5: ITextProperties -> 1.20.1: FormattedText
        FormattedText info = this.getConstellation().getConstellationTag();
        if (!detailed) {
            info = Component.translatable("astralsorcery.journal.constellation.unknown");
        }

        //1.16.5: FontRenderer#getStringPropertyWidth(ITextProperties) -> 1.20.1: Font#width(FormattedText)
        int width = font.width(info);
        float chX = 305 - (width / 2F);
        renderStack.pushPose();
        renderStack.translate(guiLeft + chX, guiTop + 44, this.getGuiZLevel());
        RenderingDrawUtils.renderStringAt(font, renderStack, info, 0xFFCCCCCC);
        renderStack.popPose();

        if (detailed && !locTextMain.isEmpty()) {
            int offsetX = 220, offsetY = 77;
            renderStack.pushPose();
            renderStack.translate(guiLeft + offsetX, guiTop + offsetY, this.getGuiZLevel());
            for (FormattedCharSequence line : locTextMain) {
                RenderingDrawUtils.renderStringAt(font, renderStack, line, 0xFFCCCCCC);
                renderStack.translate(0, 13, 0);
            }
            renderStack.popPose();
        }
    }

    private void drawPagePhaseInformation(PoseStack renderStack) {
        if (this.activePhases == null) {
            this.testActivePhases();
            if (this.activePhases == null) {
                return;
            }
        }

        List<MoonPhase> phases = this.activePhases;
        if (phases.isEmpty()) {

            FormattedText none = Component.translatable("astralsorcery.journal.constellation.unknown");
            float scale = 1.8F;
            float length = font.width(none) * scale;
            float offsetLeft = guiLeft + 296 - length / 2;
            int offsetTop = guiTop + 199;

            renderStack.pushPose();
            renderStack.translate(offsetLeft + 10, offsetTop, getGuiZLevel());
            renderStack.scale(scale, scale, scale);
            RenderingDrawUtils.renderStringAt(none, renderStack, font, 0xCCDDDDDD, true);
            renderStack.popPose();
        } else {
            boolean known = ResearchHelper.getClientProgress().hasConstellationDiscovered(this.constellation);

            int size = 19;
            int offsetX = 95 + (width / 2) - (MoonPhase.values().length * (size + 2)) / 2;
            int offsetY = 199 + guiTop;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            MoonPhase[] mPhases = MoonPhase.values();
            for (int i = 0; i < mPhases.length; i++) {
                MoonPhase phase = mPhases[i];
                int index = i;

                float brightness;
                //VERIFY_PORT[AS-JRN-01]: ported verbatim, but common/base/MoonPhase in this tree is still the
                //bare enum and has no getTexture() yet (1.16.5 common/base/MoonPhase.java:38-40 returned
                //AssetLibrary.loadTexture(TextureLocation.MISC, "moon_" + name().toLowerCase(Locale.ROOT))).
                //Reported as an external change; re-check this call once MoonPhase#getTexture() lands.
                phase.getTexture().bindTexture();
                if (known && this.activePhases.contains(phase)) {
                    Blending.PREALPHA.apply();
                    brightness = 1F;
                } else {
                    RenderSystem.defaultBlendFunc();
                    brightness = 0.7F;
                }
                //1.16.5: DefaultVertexFormats -> 1.20.1: com.mojang.blaze3d.vertex.DefaultVertexFormat
                RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                    RenderingGuiUtils.rect(buf, renderStack, offsetX + (index * (size + 2)), offsetY, this.getGuiZLevel(), size, size)
                            .color(brightness, brightness, brightness, brightness)
                            .draw();
                });
            }
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private void drawPageConstellation(PoseStack renderStack, float partial) {
        FormattedText cstName = this.constellation.getConstellationName();
        int width = font.width(cstName);

        renderStack.pushPose();
        renderStack.translate(guiLeft + (305 - (width * 1.8F / 2F)), guiTop + 26, this.getGuiZLevel());
        renderStack.scale(1.8F, 1.8F, 1);
        RenderingDrawUtils.renderStringAt(cstName, renderStack, font, 0xFFC3C3C3, true);
        renderStack.popPose();

        FormattedText dstInfo = constellation.getConstellationTypeDescription();
        if (!detailed) {
            dstInfo = Component.translatable("astralsorcery.journal.constellation.unknown");
        }
        width = font.width(dstInfo);

        renderStack.pushPose();
        renderStack.translate(guiLeft + (305 - (width / 2F)), guiTop + 219, this.getGuiZLevel());
        RenderingDrawUtils.renderStringAt(dstInfo, renderStack, font, 0xFFDDDDDD, true);
        renderStack.popPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Random rand = new Random(0x4196A15C91A5E199L);
        boolean known = ResearchHelper.getClientProgress().hasConstellationDiscovered(constellation);
        RenderingConstellationUtils.renderConstellationIntoGUI(
                known ? constellation.getConstellationColor() : constellation.getTierRenderColor(), constellation, renderStack,
                guiLeft + 40, guiTop + 60, this.getGuiZLevel(),
                150, 150, 2F,
                () -> 0.6F + 0.4F * RenderingConstellationUtils.conCFlicker(ClientScheduler.getClientTick(), partial, 12 + rand.nextInt(10)),
                true, false);
        RenderSystem.disableBlend();
    }

    private void drawNavArrows(PoseStack renderStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        this.rectNext = null;
        this.rectPrev = null;
        this.rectBack = this.drawArrow(renderStack, guiLeft + 197, guiTop + 230, this.getGuiZLevel(), Type.LEFT, mouseX, mouseY, partialTicks);

        if (doublePageID - 1 >= 0) {
            this.rectPrev = this.drawArrow(renderStack, guiLeft + 25, guiTop + 220, this.getGuiZLevel(), Type.LEFT, mouseX, mouseY, partialTicks);
        }

        if (doublePageID + 1 <= doublePages) {
            this.rectNext = this.drawArrow(renderStack, guiLeft + 367, guiTop + 220, this.getGuiZLevel(), Type.RIGHT, mouseX, mouseY, partialTicks);
        }

        RenderSystem.disableBlend();
    }

    private void drawCstBackground(PoseStack renderStack) {
        TexturesAS.TEX_BLACK.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            //1.16.5: MatrixStack#getLast()#getMatrix() -> 1.20.1: PoseStack#last()#pose() (JOML matrix)
            Matrix4f offset = renderStack.last().pose();
            //1.16.5: IVertexBuilder#pos(..)/#tex(..) -> 1.20.1: VertexConsumer#vertex(..)/#uv(..)
            buf.vertex(offset, guiLeft + 15,  guiTop + 240, this.getGuiZLevel()).color(1F, 1F, 1F, 1F).uv(0, 1).endVertex();
            buf.vertex(offset, guiLeft + 200, guiTop + 240, this.getGuiZLevel()).color(1F, 1F, 1F, 1F).uv(1, 1).endVertex();
            buf.vertex(offset, guiLeft + 200, guiTop + 10,  this.getGuiZLevel()).color(1F, 1F, 1F, 1F).uv(1, 0).endVertex();
            buf.vertex(offset, guiLeft + 15,  guiTop + 10,  this.getGuiZLevel()).color(1F, 1F, 1F, 1F).uv(0, 0).endVertex();
        });

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        TexturesAS.TEX_GUI_BACKGROUND_CONSTELLATIONS.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            Matrix4f offset = renderStack.last().pose();
            buf.vertex(offset, guiLeft + 15,  guiTop + 240, this.getGuiZLevel()).color(0.8F, 0.8F, 1F, 0.5F).uv(0.3F, 0.9F).endVertex();
            buf.vertex(offset, guiLeft + 200, guiTop + 240, this.getGuiZLevel()).color(0.8F, 0.8F, 1F, 0.5F).uv(0.7F, 0.9F).endVertex();
            buf.vertex(offset, guiLeft + 200, guiTop + 10,  this.getGuiZLevel()).color(0.8F, 0.8F, 1F, 0.5F).uv(0.7F, 0.1F).endVertex();
            buf.vertex(offset, guiLeft + 15,  guiTop + 10,  this.getGuiZLevel()).color(0.8F, 0.8F, 1F, 0.5F).uv(0.3F, 0.1F).endVertex();
        });
        RenderSystem.disableBlend();
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double mouseX, double mouseY) {
        return true;
    }

    //1.16.5: Screen#closeScreen() -> 1.20.1: Screen#onClose()
    @Override
    public void onClose() {
        //1.16.5: Minecraft#displayGuiScreen -> 1.20.1: Minecraft#setScreen
        Minecraft.getInstance().setScreen(origin);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        if (mouseButton != 0) {
            return false;
        }
        if (handleBookmarkClick(mouseX, mouseY)) {
            return true;
        }

        if (rectBack != null && rectBack.contains(mouseX, mouseY)) {
            Minecraft.getInstance().setScreen(origin);
            return true;
        }
        if (rectPrev != null && rectPrev.contains(mouseX, mouseY)) {
            if (doublePageID >= 1) {
                this.doublePageID--;
            }
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }
        if (rectNext != null && rectNext.contains(mouseX, mouseY)) {
            if (doublePageID <= doublePages - 1) {
                this.doublePageID++;
            }
            SoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            return true;
        }
        if (doublePageID != 0 && lastFramePage != null) {
            if (lastFramePage.propagateMouseClick(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

}
