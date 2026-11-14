/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;


import static hellfirepvp.astralsorcery.common.lib.StructureTypesAS.*;

/** Built-in declarations; canonical storage and Forge publication are owned by RegistriesAS. */
public class RegistryStructureTypes {

    private static boolean initialized;

    private RegistryStructureTypes() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        register(EMPTY);

        register(PTYPE_ALTAR_ATTUNEMENT);
        register(PTYPE_ALTAR_CONSTELLATION);
        register(PTYPE_ALTAR_TRAIT);
        register(PTYPE_RITUAL_PEDESTAL);
        register(PTYPE_INFUSER);
        register(PTYPE_ENHANCED_COLLECTOR_CRYSTAL);
        register(PTYPE_SPECTRAL_RELAY);
        register(PTYPE_ATTUNEMENT_ALTAR);
        register(PTYPE_CELESTIAL_GATEWAY);
        register(PTYPE_SINGULARITY);
        register(PTYPE_FOUNTAIN);
    }

    private static StructureType register(StructureType type) {
        return RegistriesAS.STRUCTURE_TYPES.register(type);
    }
}
