package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.registry.RegistryConstellations;
import hellfirepvp.astralsorcery.common.registry.RegistryCrystalProperties;
import hellfirepvp.astralsorcery.common.registry.RegistryCrystalPropertyUsages;

/** Uses the actual construction-time registry path; no global Forge-registry proxy is injected. */
final class CrystalTestSupport {
    private CrystalTestSupport() {}

    static synchronized void init() {
        RegistryConstellations.init();
        RegistryCrystalPropertyUsages.init();
        RegistryCrystalProperties.init();
    }
}
