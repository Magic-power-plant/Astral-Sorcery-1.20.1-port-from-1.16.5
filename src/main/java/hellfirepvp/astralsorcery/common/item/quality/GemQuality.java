package hellfirepvp.astralsorcery.common.item.quality;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public enum GemQuality {

    BROKEN  (ChatFormatting.GRAY,  0.1F),
    FLAWED  (ChatFormatting.GRAY,  0.35F),
    MUNDANE (ChatFormatting.WHITE, 0.5F),
    CLEAR   (ChatFormatting.AQUA,  0.6F),
    FACETED (ChatFormatting.AQUA,  0.7F),
    GLEAMING(ChatFormatting.GOLD,  0.8F),
    FLAWLESS(ChatFormatting.GOLD,  1.0F);

    private final ChatFormatting color;
    private final float degree;

    GemQuality(ChatFormatting color, float degree) {
        this.color = color;
        this.degree = degree;
    }

    public float getDegree() {
        return degree;
    }

    public MutableComponent getDisplayName() {
        return Component.translatable("item.astralsorcery.gem_quality.%s", this.name().toLowerCase(Locale.ROOT))
                .withStyle(this.color);
    }
}
