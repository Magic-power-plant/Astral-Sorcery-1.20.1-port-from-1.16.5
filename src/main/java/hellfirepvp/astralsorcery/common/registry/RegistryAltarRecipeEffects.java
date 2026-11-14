/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.*;
import hellfirepvp.astralsorcery.common.lib.AltarRecipeEffectsAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;

/** Altar effect declarations, independent of vanilla recipe types and non-JSON recipes. */
public final class RegistryAltarRecipeEffects {
    private static boolean initialized;

    private RegistryAltarRecipeEffects() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        AltarRecipeEffectsAS.BUILTIN_ATTUNEMENT_SPARKLE = registerEffect("built_in_effect_attunement_sparkle", new BuiltInEffectAttunementSparkle());
        AltarRecipeEffectsAS.BUILTIN_CONSTELLATION_LINES = registerEffect("built_in_effect_constellation_lines", new BuiltInEffectConstellationLines());
        AltarRecipeEffectsAS.BUILTIN_CONSTELLATION_FINISH = registerEffect("built_in_effect_constellation_finish", new BuiltInEffectConstellationFinish());
        AltarRecipeEffectsAS.BUILTIN_DISCOVERY_CENTRAL_BEAM = registerEffect("built_in_effect_discovery_central_beam", new BuiltInEffectDiscoveryCentralBeam());
        AltarRecipeEffectsAS.BUILTIN_TRAIT_FOCUS_CIRCLE = registerEffect("built_in_effect_trait_focus_circle", new BuiltInEffectTraitFocusCircle());
        AltarRecipeEffectsAS.BUILTIN_TRAIT_RELAY_HIGHLIGHT = registerEffect("built_in_effect_trait_relay_highlight", new BuiltInEffectTraitRelayHighlight());

        AltarRecipeEffectsAS.ALTAR_DEFAULT_LIGHTBEAM = registerEffect("altar_default_lightbeams", new EffectAltarDefaultLightbeams());
        AltarRecipeEffectsAS.ALTAR_DEFAULT_SPARKLE = registerEffect("altar_default_sparkle", new EffectAltarDefaultSparkle());
        AltarRecipeEffectsAS.ALTAR_FOCUS_SPARKLE = registerEffect("altar_focus_sparkle", new EffectAltarFocusSparkle());
        AltarRecipeEffectsAS.ALTAR_RANDOM_SPARKLE = registerEffect("altar_random_sparkle", new EffectAltarRandomSparkle());
        AltarRecipeEffectsAS.FOCUS_DUST_SWIRL = registerEffect("focus_dust_swirl", new EffectFocusDustSwirl());
        AltarRecipeEffectsAS.FOCUS_EDGE = registerEffect("focus_edge", new EffectFocusEdge());
        AltarRecipeEffectsAS.GATEWAY_EDGE = registerEffect("gateway_edge", new EffectGatewayEdge());
        AltarRecipeEffectsAS.LARGE_DUST_SWIRL = registerEffect("large_dust_swirl", new EffectLargeDustSwirl());
        AltarRecipeEffectsAS.LIQUID_BURST = registerEffect("liquid_burst", new EffectLiquidBurst());
        AltarRecipeEffectsAS.LUMINESCENCE_FLARE = registerEffect("luminescence_flare", new EffectLuminescenceFlare());
        AltarRecipeEffectsAS.PILLAR_LIGHTBEAMS = registerEffect("pillar_lightbeams", new EffectPillarLightbeams());
        AltarRecipeEffectsAS.PILLAR_SPARKLE = registerEffect("pillar_sparkle", new EffectPillarSparkle());
        AltarRecipeEffectsAS.UPGRADE_ALTAR = registerEffect("upgrade_altar", new EffectUpgradeAltar());
        AltarRecipeEffectsAS.VORTEX_PLANE = registerEffect("vortex_plane", new EffectVortexPlane());
    }

    private static <T extends AltarRecipeEffect> T registerEffect(String id, T effect) {
        effect.setRegistryName(AstralSorcery.key(id));
        return RegistriesAS.ALTAR_RECIPE_EFFECTS.register(effect);
    }
}
