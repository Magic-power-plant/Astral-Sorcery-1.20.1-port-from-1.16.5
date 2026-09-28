package hellfirepvp.astralsorcery.common.data.config.entry;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import net.minecraftforge.common.ForgeConfigSpec;

public class PerkConfig extends ConfigEntry {

    public static final PerkConfig CONFIG = new PerkConfig();

    public ForgeConfigSpec.IntValue perkLevelCap;

    private PerkConfig() {
        super("perks");
    }

    @Override
    public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
        perkLevelCap = cfgBuilder
                .comment("Sets the max level for the perk tree levels.")
                .translation(translationKey("perkLevelCap"))
                .defineInRange("perkLevelCap", 40, 10, 100);
    }

}
