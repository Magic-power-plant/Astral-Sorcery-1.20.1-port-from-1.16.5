/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.mantle;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MantleEffectHelper
 * Created by HellFirePvP
 */
public class MantleEffectHelper {

    public static void register(IWeakConstellation constellation, MantleEffect effect) {
        if (!constellation.getRegistryName().equals(effect.getAssociatedConstellation().getRegistryName())) {
            throw new IllegalArgumentException("Mantle effect constellation does not match registration key");
        }
        RegistriesAS.MANTLE_EFFECTS.register(effect);
    }

    @Nullable
    public static MantleEffect getEffect(IWeakConstellation constellation) {
        return RegistriesAS.MANTLE_EFFECTS.getValue(constellation.getRegistryName());
    }

    private MantleEffectHelper() {}
}
