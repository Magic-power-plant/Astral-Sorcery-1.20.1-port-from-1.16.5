/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.registry.constellation.BuiltinConstellations;
import hellfirepvp.astralsorcery.common.registry.constellation.ConstellationSignatureItems;

/** Orders the independent early geometry and late signature-item initialization phases. */
public final class RegistryConstellations {
    private static final BuiltinConstellations GEOMETRY = new BuiltinConstellations();
    private static final ConstellationSignatureItems SIGNATURE_ITEMS = new ConstellationSignatureItems();

    private RegistryConstellations() {}

    public static void init() {
        GEOMETRY.initialize();
    }

    /** Call only after item registration when generating signature-item based recipes. */
    public static void initSignatureItems() {
        init();
        SIGNATURE_ITEMS.initialize();
    }
}
