/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.crafting.nojson.AttunementCraftingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.LiquidStarlightCraftingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.WorldFreezingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.WorldMeltableRegistry;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.*;
import hellfirepvp.astralsorcery.common.lib.AltarRecipeEffectsAS;
import hellfirepvp.astralsorcery.common.util.NameUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRecipeTypes
 * Created by HellFirePvP
 * Date: 13.08.2019 / 19:10
 */
// Port notes for 1.16.5 common/registry/RegistryRecipeTypes.java (92 lines):
//
//   init()               (1.16.5 ref:39-55)  -> the four non-json registry inits are ported verbatim below. The
//                                               five ResolvingRecipeType assignments are not - see
//                                               TODO_PORT[AS-REG-12].
//   initAltarEffects()   (ref:57-79)         -> ported below, all twenty effects in reference order.
//   registerEffect(T)    (ref:81-85)         -> the name is still NameUtil.fromClass(effect, "Effect"), but
//                                               AltarRecipeEffect no longer extends ForgeRegistryEntry (deleted in
//                                               1.19) and there is no registry primer; the instances are collected
//                                               here and flushed into RegistriesAS.REGISTRY_ALTAR_EFFECTS by
//                                               RegistryRegistries#registerAltarRecipeEffects(RegisterEvent), the
//                                               same shape RegistryPerkAttributeTypes/-Converters use in this tree.
//   register(S)          (ref:87-90)         -> dead code already in 1.16.5 (nothing called it) and structurally
//                                               impossible now: Registry.register(Registry.RECIPE_TYPE, ..) at
//                                               mod-construction time is replaced by
//                                               common/crafting/RecipeTypesAS (DeferredRegister<RecipeType<?>>),
//                                               which registers altar / well / infusion / liquid_interaction /
//                                               block_transmutation - the exact five names of ref:40-48.
//
//TODO_PORT[AS-REG-12]: the five ResolvingRecipeType instances of 1.16.5 init()
// (common/registry/RegistryRecipeTypes.java:40-48) are not created. In 1.16.5 they were assigned to the static
// fields of common/lib/RecipeTypesAS (TYPE_WELL / TYPE_INFUSION / TYPE_BLOCK_TRANSMUTATION / TYPE_ALTAR /
// TYPE_LIQUID_INTERACTION) and each one carried the recipe class plus the match predicate used by
// ResolvingRecipeType#findRecipe / #findMatchingRecipes:
//   TYPE_WELL                = ("well", WellLiquefaction.class, (r, c) -> r.matches(c.getInput()))
//   TYPE_INFUSION            = ("infusion", LiquidInfusion.class,
//                               (r, c) -> r.matches(c.getInfuser(), c.getCrafter(), c.getSide()))
//   TYPE_BLOCK_TRANSMUTATION = ("block_transmutation", BlockTransmutation.class,
//                               (r, c) -> r.matches(c.getWorld(), c.getPos(), c.getState(), c.getConstellation()))
//   TYPE_ALTAR               = ("altar", SimpleAltarRecipe.class,
//                               (r, c) -> r.matches(c.getSide(), c.getCrafter(), c.getAltar(),
//                                                   c.ignoreStarlightRequirement()))
//   TYPE_LIQUID_INTERACTION  = ("liquid_interaction", LiquidInteraction.class,
//                               (r, c) -> r.matches(c.getContentTank1(), c.getContentTank2()))
// The 1.20.1 tree has no common/lib/RecipeTypesAS to assign them to (common/crafting/RecipeTypesAS holds the
// vanilla RecipeType objects instead), and common/crafting/helper/ResolvingRecipeType is still a stub whose
// getAllRecipes()/findRecipe(..) return empty/null, so constructing them here would only produce five objects
// nobody can reach and nothing could resolve. Both files are outside common/registry/** and are reported as
// external changes; the consumers waiting on them are already marked (SimpleAltarRecipe TODO_PORT[AS-ALT-04],
// integration/jei/CategoryAltar, integration/jei/CategoryLiquidInteraction, integration/crt/AltarManager).
//
//TODO_PORT[AS-REG-13]: 1.16.5 assigned every effect below to a static field of common/lib/AltarRecipeEffectsAS
// (ref:58-78). That holder does not exist in the 1.20.1 tree, so the effects are only reachable through
// RegistriesAS.REGISTRY_ALTAR_EFFECTS#getValue(key) once RegistryRegistries has flushed them. Creating
// common/lib/AltarRecipeEffectsAS is reported as an external change - common/crafting/recipe/SimpleAltarRecipe
// (its BUILTIN_*/ALTAR_DEFAULT_* uses, commented out at SimpleAltarRecipe.java:94-104) is blocked on it.
public class RegistryRecipeTypes {

    private static final List<AltarRecipeEffect> ALTAR_EFFECTS = new ArrayList<>();

    private RegistryRecipeTypes() {}

    public static void init() {
        //Other, non-json recipes/recipe-ish conversions
        LiquidStarlightCraftingRegistry.INSTANCE.init();
        AttunementCraftingRegistry.INSTANCE.init();
        WorldMeltableRegistry.INSTANCE.init();
        WorldFreezingRegistry.INSTANCE.init();
    }

    //Each effect is also written into its AltarRecipeEffectsAS handle, exactly as 1.16.5 does
    //(1.16.5 common/registry/RegistryRecipeTypes.java:58-78). Collecting them into ALTAR_EFFECTS alone left all
    //twenty static fields null, which is why every addAltarEffect(AltarRecipeEffectsAS.X) call site had to be
    //commented out and no altar recipe carried a single crafting effect. This runs from
    //RegistryRegistries#registerAltarRecipeEffects during RegisterEvent, i.e. at mod construction and therefore
    //strictly before the datapack builds any AltarRecipe, so the fields are populated by the time
    //AltarRecipe#addDefaultEffects reads them.
    public static void initAltarEffects() {
        AltarRecipeEffectsAS.BUILTIN_ATTUNEMENT_SPARKLE = registerEffect(new BuiltInEffectAttunementSparkle());
        AltarRecipeEffectsAS.BUILTIN_CONSTELLATION_LINES = registerEffect(new BuiltInEffectConstellationLines());
        AltarRecipeEffectsAS.BUILTIN_CONSTELLATION_FINISH = registerEffect(new BuiltInEffectConstellationFinish());
        AltarRecipeEffectsAS.BUILTIN_DISCOVERY_CENTRAL_BEAM = registerEffect(new BuiltInEffectDiscoveryCentralBeam());
        AltarRecipeEffectsAS.BUILTIN_TRAIT_FOCUS_CIRCLE = registerEffect(new BuiltInEffectTraitFocusCircle());
        AltarRecipeEffectsAS.BUILTIN_TRAIT_RELAY_HIGHLIGHT = registerEffect(new BuiltInEffectTraitRelayHighlight());

        AltarRecipeEffectsAS.ALTAR_DEFAULT_LIGHTBEAM = registerEffect(new EffectAltarDefaultLightbeams());
        AltarRecipeEffectsAS.ALTAR_DEFAULT_SPARKLE = registerEffect(new EffectAltarDefaultSparkle());
        AltarRecipeEffectsAS.ALTAR_FOCUS_SPARKLE = registerEffect(new EffectAltarFocusSparkle());
        AltarRecipeEffectsAS.ALTAR_RANDOM_SPARKLE = registerEffect(new EffectAltarRandomSparkle());
        AltarRecipeEffectsAS.FOCUS_DUST_SWIRL = registerEffect(new EffectFocusDustSwirl());
        AltarRecipeEffectsAS.FOCUS_EDGE = registerEffect(new EffectFocusEdge());
        AltarRecipeEffectsAS.GATEWAY_EDGE = registerEffect(new EffectGatewayEdge());
        AltarRecipeEffectsAS.LARGE_DUST_SWIRL = registerEffect(new EffectLargeDustSwirl());
        AltarRecipeEffectsAS.LIQUID_BURST = registerEffect(new EffectLiquidBurst());
        AltarRecipeEffectsAS.LUMINESCENCE_FLARE = registerEffect(new EffectLuminescenceFlare());
        AltarRecipeEffectsAS.PILLAR_LIGHTBEAMS = registerEffect(new EffectPillarLightbeams());
        AltarRecipeEffectsAS.PILLAR_SPARKLE = registerEffect(new EffectPillarSparkle());
        AltarRecipeEffectsAS.UPGRADE_ALTAR = registerEffect(new EffectUpgradeAltar());
        AltarRecipeEffectsAS.VORTEX_PLANE = registerEffect(new EffectVortexPlane());
    }

    public static Collection<AltarRecipeEffect> getAltarEffects() {
        return Collections.unmodifiableList(ALTAR_EFFECTS);
    }

    private static <T extends AltarRecipeEffect> T registerEffect(T recipeEffect) {
        recipeEffect.setRegistryName(NameUtil.fromClass(recipeEffect, "Effect"));
        ALTAR_EFFECTS.add(recipeEffect);
        return recipeEffect;
    }
}
