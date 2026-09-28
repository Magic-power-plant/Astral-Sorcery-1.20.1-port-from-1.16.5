package hellfirepvp.astralsorcery;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinConnector
 * Created by HellFirePvP
 *
 * Production fallback for mixin config discovery. Declared through the "MixinConnector" jar
 * manifest attribute (see build.gradle) and invoked by
 * org.spongepowered.asm.launch.platform.MixinConnectorManager once FML offers the mod jar to
 * Mixin's launch plugin.
 *
 * The config is normally already registered by then:
 *  - dev runs get "--mixin.config astralsorcery.mixins.json" from MixinGradle (Mixin's ModLauncher
 *    transformation service is named "mixin" and declares the "config" option),
 *  - production jars additionally carry the "MixinConfigs" manifest attribute.
 * Registering the same config twice is harmless: Mixins#registerConfiguration returns immediately
 * for names that are already contained in its registeredConfigs set.
 *
 * The path is resolved as a classpath resource, so it must stay identical to the location of
 * src/main/resources/astralsorcery.mixins.json (resource root, not assets/astralsorcery/).
 */
public class MixinConnector implements IMixinConnector {

    private static final String MIXIN_CONFIG = AstralSorcery.MODID + ".mixins.json";

    @Override
    public void connect() {
        Mixins.addConfiguration(MIXIN_CONFIG);
    }
}
