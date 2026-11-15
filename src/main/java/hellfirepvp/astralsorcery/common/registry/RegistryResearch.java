/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.registry.research.BuiltinResearch;

/** Common setup entry point; all registered items and structure types must already be available. */
public final class RegistryResearch {
    private static final BuiltinResearch RESEARCH = new BuiltinResearch();

    private RegistryResearch() {}

    public static void init() {
        RESEARCH.initialize();
    }
}
