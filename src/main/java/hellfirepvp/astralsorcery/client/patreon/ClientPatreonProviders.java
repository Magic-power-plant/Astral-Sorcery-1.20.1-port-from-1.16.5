/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon;
import hellfirepvp.astralsorcery.client.patreon.effect.ClientPatreonEffect;
import hellfirepvp.astralsorcery.client.patreon.provider.*;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectDefinition;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectProvider;
import java.util.ArrayList;

/** The only dispatch table that knows about render providers. Called on the client game thread. */
public final class ClientPatreonProviders {
    private ClientPatreonProviders() {}
    public static ClientPatreonEffect create(PatreonEffectDefinition definition) throws Exception {
        PatreonEffectProvider<? extends ClientPatreonEffect> provider = switch (definition.getType()) {
            case FLARE -> new ProviderFlare();
            case FLARE_DYNAMIC_COLOR -> new ProviderFlareDynamicColor();
            case FLARE_CRYSTAL -> new ProviderFlareCrystal();
            case TREE_BEACON_COLOR -> new ProviderTreeBeaconColor();
            case CRYSTAL_FOOTPRINTS -> new ProviderCrystalFootprints();
            case HELMET -> new ProviderHelmetRender();
            case WRAITH_WINGS -> new ProviderWraithWings();
            case CELESTIAL_WINGS -> new ProviderCelestialWings();
            case STAR_HALO -> new ProviderStarHalo();
            case NEBULA_CLOUD -> new ProviderNebulaCloud();
            case STARRY_LAYER -> new ProviderStarryPlayerLayer();
            case BLOCK_RING -> new ProviderBlockRing();
        };
        return provider.buildEffect(definition.getOwnerUUID(), new ArrayList<>(definition.getParameters()));
    }
}
