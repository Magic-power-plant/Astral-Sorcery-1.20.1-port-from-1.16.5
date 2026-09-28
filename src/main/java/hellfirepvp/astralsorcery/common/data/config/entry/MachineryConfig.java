package hellfirepvp.astralsorcery.common.data.config.entry;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import net.minecraftforge.common.ForgeConfigSpec;

public class MachineryConfig extends ConfigEntry {

    public static final MachineryConfig CONFIG = new MachineryConfig();

    private MachineryConfig() {
        super("machinery");
    }

    @Override
    public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
    }

}
