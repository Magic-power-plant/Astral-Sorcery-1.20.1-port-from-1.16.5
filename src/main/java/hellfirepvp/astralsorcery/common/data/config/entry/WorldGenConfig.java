package hellfirepvp.astralsorcery.common.data.config.entry;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import net.minecraftforge.common.ForgeConfigSpec;

public class WorldGenConfig extends ConfigEntry {

    public static final WorldGenConfig CONFIG = new WorldGenConfig();

    private WorldGenConfig() {
        super("worldgen");
    }

    @Override
    public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
    }

}
