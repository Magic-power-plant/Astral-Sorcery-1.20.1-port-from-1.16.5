/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;


import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.*;

/** Built-in declarations; canonical storage and Forge publication are owned by RegistriesAS. */
public class RegistryCrystalPropertyUsages {

    private static boolean initialized;

    private RegistryCrystalPropertyUsages() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        USE_RITUAL_EFFECT = createUsage("ritual.effect");
        USE_RITUAL_RANGE = createUsage("ritual.range");
        USE_COLLECTOR_CRYSTAL = createUsage("collector");
        USE_LENS_TRANSFER = createUsage("lens.transfer");
        USE_LENS_EFFECT = createUsage("lens.effect");
        USE_TOOL_DURABILITY = createUsage("tool.durability");
        USE_TOOL_EFFECTIVENESS = createUsage("tool.effectiveness");
    }

    private static PropertyUsage createUsage(String plainName) {
        return RegistriesAS.CRYSTAL_PROPERTY_USAGES.register(new PropertyUsage(AstralSorcery.key(plainName)));
    }
}
