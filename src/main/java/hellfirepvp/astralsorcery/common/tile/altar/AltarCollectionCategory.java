package hellfirepvp.astralsorcery.common.tile.altar;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class AltarCollectionCategory {

    public static final AltarCollectionCategory HEIGHT              = new AltarCollectionCategory(AstralSorcery.key("height"));
    public static final AltarCollectionCategory FOSIC_FIELD         = new AltarCollectionCategory(AstralSorcery.key("fosic_field"));
    public static final AltarCollectionCategory RELAY               = new AltarCollectionCategory(AstralSorcery.key("relay"));
    public static final AltarCollectionCategory FOCUSED_NETWORK     = new AltarCollectionCategory(AstralSorcery.key("focused_network"));
    public static final AltarCollectionCategory CONDENSED_STARLIGHT = new AltarCollectionCategory(AstralSorcery.key("condensed_starlight"));

    private final ResourceLocation key;

    public AltarCollectionCategory(ResourceLocation key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AltarCollectionCategory that = (AltarCollectionCategory) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
