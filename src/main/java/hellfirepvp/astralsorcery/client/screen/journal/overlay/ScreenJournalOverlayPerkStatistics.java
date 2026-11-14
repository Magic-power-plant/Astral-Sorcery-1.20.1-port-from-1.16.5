/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.overlay;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournal;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeMap;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeInterpreter;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader;
import hellfirepvp.astralsorcery.common.perk.reader.PerkStatistic;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.perk.type.vanilla.VanillaPerkAttributeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

import java.awt.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalOverlayPerkStatistics
 * Created by HellFirePvP
 * Date: 09.08.2019 / 07:52
 */
public class ScreenJournalOverlayPerkStatistics extends ScreenJournalOverlay {

    private static final int HEADER_WIDTH = 190;
    private static final int DEFAULT_WIDTH = 175;

    private final List<PerkStatistic> statistics = new LinkedList<>();

    private int nameStrWidth = -1;
    private int valueStrWidth = -1;
    private int suffixStrWidth = -1;

    public ScreenJournalOverlayPerkStatistics(ScreenJournal origin) {
        //1.16.5: new TranslationTextComponent(..) -> 1.20.1: Component.translatable(..)
        super(Component.translatable("screen.astralsorcery.tome.perks.stats"), origin);
    }

    @Override
    protected void init() {
        super.init();

        statistics.clear();

        //1.16.5: PlayerEntity -> 1.20.1: Player
        Player player = Minecraft.getInstance().player;
        PerkAttributeInterpreter interpreter = PerkAttributeInterpreter.defaultInterpreter(player);

        RegistriesAS.PERK_ATTRIBUTE_TYPES.getValues()
                .stream()
                .filter(t -> t instanceof VanillaPerkAttributeType)
                .forEach(t -> ((VanillaPerkAttributeType) t).refreshAttribute(player));

        for (PerkAttributeType type : RegistriesAS.PERK_ATTRIBUTE_TYPES.getValues()) {
            if (type.hasTypeApplied(player, LogicalSide.CLIENT)) {
                PerkStatistic strPerkStat = interpreter.getValue(type);
                if (strPerkStat != null) {
                    statistics.add(strPerkStat);
                }
            }
        }

        //1.16.5: I18n#format -> 1.20.1: I18n#get
        statistics.sort(Comparator.comparing(perkStatistic -> I18n.get(perkStatistic.getUnlocPerkTypeName())));
    }

    //1.16.5: Screen#render(MatrixStack, int, int, float) -> 1.20.1: Screen#render(GuiGraphics, int, int, float).
    //Everything below this entrypoint keeps taking the PoseStack the 1.16.5 code took - GuiGraphics#pose() is it.
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pTicks) {
        super.render(guiGraphics, mouseX, mouseY, pTicks);

        PoseStack renderStack = guiGraphics.pose();

        float width = 275;
        float height = 344;

        this.setBlitOffset(150);
        TexturesAS.TEX_GUI_PARCHMENT_BLANK.bindTexture();
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        RenderingGuiUtils.drawRect(renderStack, guiLeft + guiWidth / 2F - width / 2F, guiTop + guiHeight / 2F - height / 2F, this.getGuiZLevel(),
                width, height);
        RenderSystem.disableBlend();
        this.setBlitOffset(0);

        drawHeader(renderStack);
        drawPageText(renderStack, mouseX, mouseY);
    }

    private void drawHeader(PoseStack renderStack) {
        //1.16.5: ITextProperties -> 1.20.1: FormattedText
        FormattedText title = Component.translatable("perk.reader.astralsorcery.gui");
        //1.16.5: FontRenderer#trimStringToWidth(ITextProperties, int) -> 1.20.1: Font#split(FormattedText, int)
        //1.16.5: IReorderingProcessor -> 1.20.1: FormattedCharSequence
        List<FormattedCharSequence> lines = font.split(title, Mth.floor(HEADER_WIDTH / 1.4F));
        int step = 14;
        float offsetTop = guiTop + 15 - (lines.size() * step) / 2F;

        renderStack.pushPose(); //1.16.5: MatrixStack#push()
        renderStack.translate(0, offsetTop, 0);

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            //1.16.5: FontRenderer#func_243245_a(IReorderingProcessor) -> 1.20.1: Font#width(FormattedCharSequence)
            float offsetLeft = width / 2F - (font.width(line) * 1.4F) / 2F;

            renderStack.pushPose();
            renderStack.translate(offsetLeft, i * step, 0);
            renderStack.scale(1.4F, 1.4F, 1F);
            RenderingDrawUtils.renderStringAt(line, renderStack, font, 0xEE333333, false);
            renderStack.popPose();
        }
        renderStack.popPose(); //1.16.5: MatrixStack#pop()
    }

    private void drawPageText(PoseStack renderStack, int mouseX, int mouseY) {
        if (nameStrWidth == -1 || valueStrWidth == -1 || suffixStrWidth == -1) {
            buildDisplayWidth();
        }

        Map<Rectangle, PerkStatistic> valueStrMap = Maps.newHashMap();
        int offsetY = guiTop + 40;
        int offsetX = guiLeft + guiWidth / 2 - DEFAULT_WIDTH / 2;
        int line = 0;
        for (PerkStatistic stat : statistics) {
            FormattedText statName = Component.translatable(stat.getUnlocPerkTypeName());
            List<FormattedCharSequence> statistics = font.split(statName, Mth.floor(HEADER_WIDTH / 1.5F));
            for (int i = 0; i < statistics.size(); i++) {
                FormattedCharSequence statistic = statistics.get(i);

                int drawX = offsetX;
                if (i > 0) {
                    drawX += 10;
                }
                renderStack.pushPose();
                renderStack.translate(drawX, offsetY + ((line + i) * 10), this.getGuiZLevel());
                RenderingDrawUtils.renderStringAt(statistic, renderStack, font, 0xEE333333, false);
                renderStack.popPose();
            }

            renderStack.pushPose();
            renderStack.translate(offsetX + nameStrWidth, offsetY + (line * 10), this.getGuiZLevel());
            //1.16.5: new StringTextComponent(..) -> 1.20.1: Component.literal(..)
            RenderingDrawUtils.renderStringAt(Component.literal(stat.getPerkValue()), renderStack, font, 0xEE333333, false);
            renderStack.popPose();

            //1.16.5: FontRenderer#getStringWidth(String) -> 1.20.1: Font#width(String)
            int strLength = font.width(stat.getPerkValue());
            Rectangle rctValue = new Rectangle(offsetX + nameStrWidth, offsetY + (line * 10), strLength, 8);
            valueStrMap.put(rctValue, stat);

            line += statistics.size();
            if (!stat.getSuffix().isEmpty()) {
                renderStack.pushPose();
                renderStack.translate(offsetX + 25, offsetY + (line * 10), this.getGuiZLevel());
                RenderingDrawUtils.renderStringAt(Component.literal(stat.getSuffix()), renderStack, font, 0xEE333333, false);
                renderStack.popPose();

                line++;
            }
        }

        for (Rectangle rct : valueStrMap.keySet()) {
            if (rct.contains(mouseX, mouseY)) {
                PerkStatistic stat = valueStrMap.get(rct);
                drawCalculationDescription(renderStack, rct.x + rct.width + 2, rct.y + 15, stat);
            }
        }
    }

    private void drawCalculationDescription(PoseStack renderStack, int x, int y, PerkStatistic stat) {
        PerkAttributeType type = stat.getType();
        PerkAttributeReader reader = type.getReader();
        if (reader == null) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        PerkAttributeMap attrMap = PerkAttributeHelper.getOrCreateMap(player, LogicalSide.CLIENT);

        List<FormattedText> information = Lists.newArrayList();
        information.add(Component.translatable("perk.reader.astralsorcery.description.head",
                PerkAttributeReader.formatDecimal(reader.getDefaultValue(attrMap, player, LogicalSide.CLIENT))));
        information.add(Component.translatable("perk.reader.astralsorcery.description.addition",
                PerkAttributeReader.formatDecimal(reader.getModifierValueForMode(attrMap, player, LogicalSide.CLIENT,
                        ModifierType.ADDITION) - 1)));
        information.add(Component.translatable("perk.reader.astralsorcery.description.increase",
                PerkAttributeReader.formatDecimal(reader.getModifierValueForMode(attrMap, player, LogicalSide.CLIENT,
                        ModifierType.ADDED_MULTIPLY))));
        information.add(Component.translatable("perk.reader.astralsorcery.description.moreless",
                PerkAttributeReader.formatDecimal(reader.getModifierValueForMode(attrMap, player, LogicalSide.CLIENT,
                        ModifierType.STACKING_MULTIPLY))));

        if (!stat.getSuffix().isEmpty() || !stat.getPostProcessInfo().isEmpty()) {
            //1.16.5: StringTextComponent.EMPTY -> 1.20.1: CommonComponents.EMPTY
            information.add(CommonComponents.EMPTY);
        }
        if (!stat.getSuffix().isEmpty()) {
            information.add(Component.literal(stat.getSuffix()));
        }
        if (!stat.getPostProcessInfo().isEmpty()) {
            information.add(Component.literal(stat.getPostProcessInfo()));
        }

        RenderingDrawUtils.renderBlueTooltipComponents(renderStack, x, y, this.getGuiZLevel(), information, this.font, false);
    }

    private void buildDisplayWidth() {
        nameStrWidth = -1;
        valueStrWidth = -1;
        suffixStrWidth = -1;

        for (PerkStatistic stat : this.statistics) {
            FormattedText typeName = Component.translatable(stat.getUnlocPerkTypeName());
            //1.16.5: FontRenderer#getStringPropertyWidth(ITextProperties) -> 1.20.1: Font#width(FormattedText)
            int nameWidth = Math.min(font.width(typeName), ((int) (HEADER_WIDTH / 1.5F)));
            int valueWidth = font.width(stat.getPerkValue());
            int suffixWidth = font.width(stat.getSuffix());

            if (nameWidth > nameStrWidth) {
                nameStrWidth = nameWidth;
            }
            if (valueWidth > valueStrWidth) {
                valueStrWidth = valueWidth;
            }
            if (suffixWidth > suffixStrWidth) {
                suffixStrWidth = suffixWidth;
            }
        }

        nameStrWidth += 6;
        valueStrWidth += 6;
        suffixStrWidth += 6;
    }
}
