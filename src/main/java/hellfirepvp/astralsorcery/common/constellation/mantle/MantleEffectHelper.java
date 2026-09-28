/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.mantle;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MantleEffectHelper
 * Created by HellFirePvP
 */
//No 1.16.5 counterpart: there, MantleEffect extended ForgeRegistryEntry<MantleEffect> and was looked up with
//`RegistriesAS.REGISTRY_MANTLE_EFFECT.getValue(constellation.getRegistryName())`. Forge removed
//IForgeRegistryEntry/#setRegistryName in 1.19, so MantleEffect can no longer carry its own registry name.
//This map is the stand-in for that registry lookup and is keyed by the very same constellation registry name
//identity, so IWeakConstellation#getMantleEffect() resolves to the same instance it did in 1.16.5.
public class MantleEffectHelper {

    private static final Map<IWeakConstellation, MantleEffect> EFFECTS = new HashMap<>();

    public static void register(IWeakConstellation constellation, MantleEffect effect) {
        EFFECTS.put(constellation, effect);
    }

    @Nullable
    public static MantleEffect getEffect(IWeakConstellation constellation) {
        return EFFECTS.get(constellation);
    }

    private MantleEffectHelper() {}
}
