package hellfirepvp.astralsorcery.common.data.config.entry;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import net.minecraftforge.common.ForgeConfigSpec;

public class LightNetworkConfig extends ConfigEntry {

    public static final LightNetworkConfig CONFIG = new LightNetworkConfig();

    public ForgeConfigSpec.BooleanValue performNetworkIntegrityCheck;

    private LightNetworkConfig() {
        super("lightnetwork");
    }

    @Override
    public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
        performNetworkIntegrityCheck = cfgBuilder
                .comment("NOTE: ONLY run this once and set it to false again afterwards, nothing will be gained by setting this to true permanently, just longer loading times. When set to true and the server started, this will perform an integrity check over all nodes of the starlight network whenever a world gets loaded, removing invalid ones in the process. This might, depending on network sizes, take a while. It'll leave a message in the console when it's done. After this check has been run, you might need to tear down and rebuild your starlight network in case something doesn't work anymore.")
                .translation(translationKey("performNetworkIntegrityCheck"))
                .define("performNetworkIntegrityCheck", false);
    }

}
