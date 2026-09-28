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
 * Class: RegistryFluids
 * Created by HellFirePvP
 * Date: 20.09.2019 / 21:53
 */
// Port notes for 1.16.5 common/registry/RegistryFluids.java (89 lines):
//
// Every member of the 1.16.5 class has an exact counterpart in the 1.20.1 tree already, so this class is
// intentionally left without members - adding any would create a second registration path for the one fluid and
// register it twice:
//
//   FLUID_BLOCKS            (1.16.5 ref:41, 52) -> common/lib/FluidsAS#LIQUID_STARLIGHT_BLOCK, which registers the
//                                                  BlockLiquidStarlight straight into BlocksAS.BLOCKS under
//                                                  "liquid_starlight" (1.16.5 got the same name from
//                                                  NameUtil.fromClass(block, "Block")). The list existed only so
//                                                  RegistryBlocks#registerFluidBlocks could flush it.
//   FLUID_HOLDER_ITEMS      (ref:42, 53)        -> common/lib/FluidsAS#LIQUID_STARLIGHT_BUCKET, registered into
//                                                  ItemsAS.ITEMS as "liquid_starlight_bucket" (same name 1.16.5
//                                                  derived from NameUtil.fromClass(item, "Item")). The list existed
//                                                  only for RegistryItems#registerFluidContainerItems.
//   registerFluids()        (ref:46-54)         -> common/lib/FluidsAS: LIQUID_STARLIGHT_SOURCE /
//                                                  LIQUID_STARLIGHT_FLOWING are DeferredRegister entries of
//                                                  ForgeRegistries.FLUIDS. 1.16.5 instantiated
//                                                  FluidLiquidStarlight.Source/.Flowing; those two nested classes
//                                                  only re-implemented what ForgeFlowingFluid.Source/.Flowing
//                                                  already do (getLevel / isSource / the LEVEL flowing state), so
//                                                  the 1.20.1 tree uses the Forge classes directly and nothing
//                                                  behavioural is lost.
//   makeProperties()        (ref:56-61)         -> common/lib/FluidsAS#liquidStarlightProperties(). The 1.16.5
//                                                  ForgeFlowingFluid.Properties(still, flowing, FluidAttributes)
//                                                  constructor was replaced in 1.19.2 by
//                                                  Properties(Supplier<FluidType>, still, flowing): the attributes
//                                                  moved onto the FluidType.
//   makeProperties(Class, Function, ..)
//                           (ref:63-72)        -> gone with FluidAttributes. FluidLiquidStarlight#addAttributes
//                                                  (rarity EPIC / luminosity 15 / density 1001 / viscosity 300 /
//                                                  temperature 40) is now the FluidType.Properties chain in
//                                                  common/fluid/FluidLiquidStarlight, which additionally carries
//                                                  the bucket fill/empty SoundActions 1.16.5 had no slot for.
//   builderFor(String)      (ref:74-78)        -> FluidAttributes.builder(still, flowing) became the client-only
//                                                  IClientFluidTypeExtensions#getStillTexture/#getFlowingTexture
//                                                  override in common/fluid/FluidLiquidStarlight, with the same
//                                                  two paths astralsorcery:fluid/liquid_starlight_{still,flowing}.
//   registerFluid(T[, name])(ref:80-88)        -> DeferredRegister#register; Fluid#setRegistryName and the
//                                                  registry primer are both gone (IForgeRegistryEntry was deleted
//                                                  in 1.19).
//
//VERIFY_PORT[AS-REG-14]: the flowing fluid's registry name changed in the 1.20.1 tree. 1.16.5 derived it from
// NameUtil.fromClass(fluid, "Fluid", "Source") on FluidLiquidStarlight$Flowing, which yields
// "astralsorcery:liquid_starlight_flowing"; common/lib/FluidsAS registers it as
// "astralsorcery:flowing_liquid_starlight" instead (the source fluid, "astralsorcery:liquid_starlight", is
// unchanged). Renaming it back belongs to common/lib/FluidsAS and is reported as an external change.
public final class RegistryFluids {

    private RegistryFluids() {}
}
