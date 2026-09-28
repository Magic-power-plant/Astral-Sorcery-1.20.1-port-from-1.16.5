/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRecipeSerializers
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:32
 */
// Port notes for 1.16.5 common/registry/RegistryRecipeSerializers.java (47 lines):
//
// All seven registrations of init() (1.16.5 ref:31-40) already exist in the 1.20.1 tree as DeferredRegister
// entries of common/crafting/RecipeSerializersAS, so this class is intentionally left without members: a second
// init() would register the same serializers a second time and fail on the duplicate keys.
//
//   WELL_LIQUEFACTION_SERIALIZER        (ref:32) -> RecipeSerializersAS.LIGHTWELL   ("astralsorcery:lightwell")
//   LIQUID_INFUSION_SERIALIZER          (ref:33) -> RecipeSerializersAS.INFUSER     ("astralsorcery:infuser")
//   BLOCK_TRANSMUTATION_SERIALIZER      (ref:34) -> RecipeSerializersAS.BLOCK_TRANSMUTATION
//   ALTAR_RECIPE_SERIALIZER             (ref:35) -> RecipeSerializersAS.ALTAR       ("astralsorcery:altar")
//   LIQUID_INTERACTION_SERIALIZER       (ref:36) -> RecipeSerializersAS.LIQUID_INTERACTION
//   CUSTOM_CHANGE_WAND_COLOR_SERIALIZER (ref:38) -> RecipeSerializersAS.CHANGE_WAND_COLOR
//   CUSTOM_CHANGE_GATEWAY_COLOR_SERIALIZER
//                                       (ref:39) -> RecipeSerializersAS.CHANGE_GATEWAY_COLOR
// The seven names are unchanged: 1.16.5 took them from the ResourceLocation constants at the top of
// common/lib/RecipeSerializersAS, which each serializer passed up to CustomRecipeSerializer(name) ->
// setRegistryName (common/crafting/helper/CustomRecipeSerializer.java:25).
//
//   register(T)  (ref:42-45) -> DeferredRegister#register. ForgeRegistryEntry/#setRegistryName and the registry
//                               primer are gone (IForgeRegistryEntry deleted in 1.19), so CustomRecipeSerializer
//                               no longer carries a name at all; the name lives in the register() call.
//                               IRecipeSerializer<R extends IRecipe<C extends IInventory>> is
//                               RecipeSerializer<R extends Recipe<C extends Container>>.
//   RecipeDyeableChangeColor.IlluminationWandColorSerializer / .CelestialGatewayColorSerializer
//                            -> common/crafting/DyeableChangeColorRecipe.WandColorSerializer /
//                               .GatewayColorSerializer in the 1.20.1 tree.
//
// Divergence worth flagging (common/crafting is outside this slice, reported as an external change):
// RecipeSerializersAS additionally registers "astralsorcery:well" from the very same
// WellLiquefaction.Serializer as "astralsorcery:lightwell". 1.16.5 has one well/lightwell serializer only
// (ref:32, name from RecipeSerializersAS.WELL_LIQUEFACTION = "lightwell"); the extra entry has no reference
// counterpart.
public final class RegistryRecipeSerializers {

    private RegistryRecipeSerializers() {}
}
