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
 * Class: RegistryMaterials
 * Created by HellFirePvP
 * Date: 30.05.2019 / 22:58
 *
 * @see hellfirepvp.astralsorcery.common.block.properties.PropertiesMarble
 * @see hellfirepvp.astralsorcery.common.block.properties.PropertiesWood
 */
//BLOCKED_PORT[AS-REG-17]: init() cannot exist on 1.20.1 (1.16.5 common/registry/RegistryMaterials.java:27-36).
//
// Every statement of the 1.16.5 init() builds a net.minecraft.block.material.Material
// (1.19: net.minecraft.world.level.material.Material) through common/block/base/MaterialBuilderAS#build. That
// class was deleted in 1.20 - verified against forge-1.20.1-47.4.20_mapped_official_1.20.1.jar, whose
// net/minecraft/world/level/material/ package holds only the fluid classes plus MapColor, FogType and
// PushReaction - and with it went both the three common/lib/MaterialsAS fields this method assigned
// (BLOCKED_PORT[AS-LIB-MAT-01] on that class) and MaterialBuilderAS#build itself. There is no 1.20.1 object to
// assign, so the method is not reproduced; it is not a silent omission but a deleted vanilla type.
//
// The three materials' actual contents are not lost - they were folded into the two Block.Properties factories
// that were their only consumers, because BlockBehaviour.Properties now carries per-block what Material used to
// carry per-material:
//
//   MARBLE        = new MaterialBuilderAS(MaterialColor.WHITE_TERRACOTTA).build()   (1.16.5 ref:28-29)
//                 -> common/block/properties/PropertiesMarble#defaultMarble: Properties#of() (all of
//                    MaterialBuilderAS' defaults - isSolid, isOpaque, blocksMovement, PushReaction.NORMAL - are
//                    the Properties#of() defaults too) + mapColor(MapColor.TERRACOTTA_WHITE).
//   BLACK_MARBLE  = new MaterialBuilderAS(MaterialColor.BLACK).build()              (1.16.5 ref:30-31)
//                 -> PropertiesMarble#defaultBlackMarble, same shape + mapColor(MapColor.COLOR_BLACK).
//   INFUSED_WOOD  = new MaterialBuilderAS(MaterialColor.BROWN).flammable().build()  (1.16.5 ref:33-35)
//                 -> common/block/properties/PropertiesWood#defaultInfusedWood: Properties#of() +
//                    mapColor(MapColor.COLOR_BROWN) + ignitedByLava(), the 1.20.1 spelling of the material's
//                    canBurn flag.
//
// MaterialColor is MapColor in 1.20.1 and the constants were re-ordered in name only
// (WHITE_TERRACOTTA -> TERRACOTTA_WHITE, BLACK -> COLOR_BLACK, BROWN -> COLOR_BROWN).
//
// 1.16.5 called this from CommonProxy#initialize (1.16.5 common/CommonProxy.java:164), before RegistryBlocks ran,
// precisely because the materials had to exist before any Block.Properties was built. Nothing has to run early
// any more, so the 1.20.1 CommonProxy correctly has no call.
public final class RegistryMaterials {

    private RegistryMaterials() {}
}
