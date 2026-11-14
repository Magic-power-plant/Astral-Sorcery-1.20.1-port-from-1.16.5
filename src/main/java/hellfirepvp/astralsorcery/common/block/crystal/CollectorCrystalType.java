package hellfirepvp.astralsorcery.common.block.crystal;

import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.world.level.material.MapColor;

import java.awt.Color;

/**
 * Port of 1.16.5 {@code CollectorCrystalType}. Constant names match upstream ({@code ROCK_CRYSTAL} / {@code CELESTIAL_CRYSTAL});
 * the previous target-only {@code ROCK}/{@code CELESTIAL} aliases are kept so existing call sites compile.
 */
public enum CollectorCrystalType {

    ROCK_CRYSTAL(ColorsAS.ROCK_CRYSTAL, MapColor.TERRACOTTA_WHITE),
    CELESTIAL_CRYSTAL(ColorsAS.CELESTIAL_CRYSTAL, MapColor.COLOR_CYAN);

    public static final CollectorCrystalType ROCK = ROCK_CRYSTAL;
    public static final CollectorCrystalType CELESTIAL = CELESTIAL_CRYSTAL;

    private final Color displayColor;
    private final MapColor matColor;

    CollectorCrystalType(Color displayColor, MapColor matColor) {
        this.displayColor = displayColor;
        this.matColor = matColor;
    }

    public Color getDisplayColor() {
        return displayColor;
    }

    public MapColor getMaterialColor() {
        return matColor;
    }

    public boolean isRock() {
        return this == ROCK_CRYSTAL;
    }
}
