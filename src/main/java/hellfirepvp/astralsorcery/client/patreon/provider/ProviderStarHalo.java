/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.provider;
import hellfirepvp.astralsorcery.common.patreon.effect.FlareColor;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectProvider;
import hellfirepvp.astralsorcery.client.patreon.effect.TypeStarHalo;
import java.util.List;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ProviderStarHalo
 * Created by HellFirePvP
 * Date: 04.12.2020 / 18:12
 */
public class ProviderStarHalo implements PatreonEffectProvider<TypeStarHalo> {
    @Override
    public TypeStarHalo buildEffect(UUID playerUUID, List<String> effectParameters) throws Exception {
        UUID effectUUID = UUID.fromString(effectParameters.get(0));
        FlareColor fc = null;
        if (!"null".equals(effectParameters.get(1))) {
            fc = FlareColor.valueOf(effectParameters.get(1));
        }
        return new TypeStarHalo(effectUUID, fc, playerUUID);
    }
}
