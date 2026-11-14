/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.patreon.effect;

/** Stable names from patreon.json; never construct physical-client providers here. */
public enum PatreonEffectType {
    FLARE, FLARE_DYNAMIC_COLOR, FLARE_CRYSTAL, TREE_BEACON_COLOR, CRYSTAL_FOOTPRINTS,
    HELMET, WRAITH_WINGS, CELESTIAL_WINGS, STAR_HALO, NEBULA_CLOUD, STARRY_LAYER, BLOCK_RING;

    public PatreonEffectProvider<PatreonEffectDefinition> getProvider() {
        return (owner, parameters) -> new PatreonEffectDefinition(this, owner, parameters);
    }
}
