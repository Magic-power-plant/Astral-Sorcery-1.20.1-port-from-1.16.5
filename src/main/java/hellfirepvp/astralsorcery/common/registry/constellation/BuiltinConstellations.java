/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry.constellation;

import hellfirepvp.astralsorcery.common.registry.AbstractRegistryInitializer;

/** Owns built-in geometry initialization; item registries are not touched. */
public final class BuiltinConstellations extends AbstractRegistryInitializer {
    @Override
    protected void registerEntries() {
        MajorConstellations.registerEntries();
        WeakConstellations.registerEntries();
        MinorConstellations.registerEntries();
    }
}
