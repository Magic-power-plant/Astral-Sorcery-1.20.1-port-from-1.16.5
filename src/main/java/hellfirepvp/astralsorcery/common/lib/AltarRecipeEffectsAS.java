/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarRecipeEffectsAS
 * Created by HellFirePvP
 * Date: 27.09.2019 / 22:42
 */
//AS-LIB-ARE-01 (resolved): these fields are assigned by common/registry/RegistryRecipeTypes#initAltarEffects,
// which writes each newly created effect into the matching static handle exactly as 1.16.5 does
// (1.16.5 common/registry/RegistryRecipeTypes.java:58-78). That method runs from
// RegistryRegistries#registerAltarRecipeEffects during RegisterEvent - mod construction - so every field here is
// populated before the datapack constructs any AltarRecipe. While they were null, every
// addAltarEffect(AltarRecipeEffectsAS.X) call site had to stay commented out and no altar recipe carried a
// single crafting effect.
public class AltarRecipeEffectsAS {

    private AltarRecipeEffectsAS() {}

    public static AltarRecipeEffect BUILTIN_ATTUNEMENT_SPARKLE;
    public static AltarRecipeEffect BUILTIN_CONSTELLATION_LINES;
    public static AltarRecipeEffect BUILTIN_CONSTELLATION_FINISH;
    public static AltarRecipeEffect BUILTIN_DISCOVERY_CENTRAL_BEAM;
    public static AltarRecipeEffect BUILTIN_TRAIT_FOCUS_CIRCLE;
    public static AltarRecipeEffect BUILTIN_TRAIT_RELAY_HIGHLIGHT;

    public static AltarRecipeEffect ALTAR_DEFAULT_LIGHTBEAM;
    public static AltarRecipeEffect ALTAR_DEFAULT_SPARKLE;
    public static AltarRecipeEffect ALTAR_FOCUS_SPARKLE;
    public static AltarRecipeEffect ALTAR_RANDOM_SPARKLE;
    public static AltarRecipeEffect FOCUS_DUST_SWIRL;
    public static AltarRecipeEffect FOCUS_EDGE;
    public static AltarRecipeEffect GATEWAY_EDGE;
    public static AltarRecipeEffect LARGE_DUST_SWIRL;
    public static AltarRecipeEffect LIQUID_BURST;
    public static AltarRecipeEffect LUMINESCENCE_FLARE;
    public static AltarRecipeEffect PILLAR_LIGHTBEAMS;
    public static AltarRecipeEffect PILLAR_SPARKLE;
    public static AltarRecipeEffect UPGRADE_ALTAR;
    public static AltarRecipeEffect VORTEX_PLANE;

}
