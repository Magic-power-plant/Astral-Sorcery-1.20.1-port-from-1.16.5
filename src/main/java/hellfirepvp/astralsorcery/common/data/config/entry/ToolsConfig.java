package hellfirepvp.astralsorcery.common.data.config.entry;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import net.minecraftforge.common.ForgeConfigSpec;

public class ToolsConfig extends ConfigEntry {

    public static final ToolsConfig CONFIG = new ToolsConfig();

    public ForgeConfigSpec.DoubleValue capeChaosResistance;

    private ToolsConfig() {
        super("tools");
    }

    @Override
    public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
        capeChaosResistance = cfgBuilder
                .comment("Sets the amount of damage reduction a player gets when being hit by a DE chaos-damage-related damagetype.")
                .translation(translationKey("capeChaosResistance"))
                .defineInRange("capeChaosResistance", 0.8, 0, 1);
    }

}
