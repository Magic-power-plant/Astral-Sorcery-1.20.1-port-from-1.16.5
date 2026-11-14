package hellfirepvp.astralsorcery.common.util.color;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import javax.annotation.Nonnull;
import java.awt.*;

public class ColorUtils {

    public static Color blendColors(Color color1, Color color2, float color1Ratio) {
        return new Color(blendColors(color1.getRGB(), color2.getRGB(), color1Ratio), true);
    }

    public static int blendColors(int color1, int color2, float color1Ratio) {
        float ratio1 = Mth.clamp(color1Ratio, 0F, 1F);
        float ratio2 = 1F - ratio1;

        int a1 = (color1 & 0xFF000000) >> 24;
        int r1 = (color1 & 0x00FF0000) >> 16;
        int g1 = (color1 & 0x0000FF00) >>  8;
        int b1 = (color1 & 0x000000FF);

        int a2 = (color2 & 0xFF000000) >> 24;
        int r2 = (color2 & 0x00FF0000) >> 16;
        int g2 = (color2 & 0x0000FF00) >>  8;
        int b2 = (color2 & 0x000000FF);

        int a = Mth.clamp(Math.round(a1 * ratio1 + a2 * ratio2), 0, 255);
        int r = Mth.clamp(Math.round(r1 * ratio1 + r2 * ratio2), 0, 255);
        int g = Mth.clamp(Math.round(g1 * ratio1 + g2 * ratio2), 0, 255);
        int b = Mth.clamp(Math.round(b1 * ratio1 + b2 * ratio2), 0, 255);

        return a << 24 | r << 16 | g << 8 | b;
    }

    public static Color overlayColor(Color base, Color overlay) {
        return new Color(overlayColor(base.getRGB(), overlay.getRGB()), true);
    }

    public static int overlayColor(int base, int overlay) {
        int alpha = (base & 0xFF000000) >> 24;

        int baseR = (base & 0x00FF0000) >> 16;
        int baseG = (base & 0x0000FF00) >>  8;
        int baseB = (base & 0x000000FF);

        int overlayR = (overlay & 0x00FF0000) >> 16;
        int overlayG = (overlay & 0x0000FF00) >>  8;
        int overlayB = (overlay & 0x000000FF);

        int r = Math.round(baseR * (overlayR / 255F)) & 0xFF;
        int g = Math.round(baseG * (overlayG / 255F)) & 0xFF;
        int b = Math.round(baseB * (overlayB / 255F)) & 0xFF;

        return alpha << 24 | r << 16 | g << 8 | b;
    }

    @Nonnull
    public static MutableComponent getTranslation(DyeColor color) {
        return Component.translatable("color.minecraft.%s", color.getSerializedName());
    }

    @Nonnull
    public static Color flareColorFromDye(DyeColor color) {
        return ColorsAS.DYE_COLOR_PARTICLES[color.getId()];
    }

    @Nonnull
    public static ChatFormatting textFormattingForDye(DyeColor color) {
        switch (color) {
            case WHITE:
                return ChatFormatting.WHITE;
            case ORANGE:
                return ChatFormatting.GOLD;
            case MAGENTA:
                return ChatFormatting.DARK_PURPLE;
            case LIGHT_BLUE:
                return ChatFormatting.DARK_AQUA;
            case YELLOW:
                return ChatFormatting.YELLOW;
            case LIME:
                return ChatFormatting.GREEN;
            case PINK:
                return ChatFormatting.LIGHT_PURPLE;
            case GRAY:
                return ChatFormatting.DARK_GRAY;
            case LIGHT_GRAY:
                return ChatFormatting.GRAY;
            case CYAN:
                return ChatFormatting.BLUE;
            case PURPLE:
                return ChatFormatting.DARK_PURPLE;
            case BLUE:
                return ChatFormatting.DARK_BLUE;
            case BROWN:
                return ChatFormatting.GOLD;
            case GREEN:
                return ChatFormatting.DARK_GREEN;
            case RED:
                return ChatFormatting.DARK_RED;
            case BLACK:
                return ChatFormatting.DARK_GRAY;
            default:
                return ChatFormatting.WHITE;
        }
    }
}
