/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MaterialsAS
 * Created by HellFirePvP
 * Date: 30.05.2019 / 22:58
 */
//BLOCKED_PORT[AS-LIB-MAT-01]: the three constants of this holder cannot exist on 1.20.1
// (1.16.5 common/lib/MaterialsAS.java:22-28):
//
//    public static Material MARBLE;
//    public static Material BLACK_MARBLE;
//    public static Material INFUSED_WOOD;
//
// net.minecraft.block.material.Material (1.19: net.minecraft.world.level.material.Material) was deleted in
// 1.20 - verified against forge-1.20.1-47.4.20_mapped_official_1.20.1.jar, whose
// net/minecraft/world/level/material/ package only contains the fluid classes plus MapColor, FogType and
// PushReaction. Every property the Material carried is now set on BlockBehaviour.Properties directly:
//   map colour     -> Properties#mapColor(MapColor)
//   blocks motion  -> Properties#noCollission()/default
//   flammable      -> FireBlock#setFlammable data / Properties, not the material
//   push reaction  -> Properties#pushReaction(PushReaction)
//   sound          -> Properties#sound(SoundType)
// 1.16.5 filled the three fields in common/registry/RegistryMaterials#init from
// common/block/base/MaterialBuilderAS (MaterialColor.WHITE_TERRACOTTA / MaterialColor.BLACK /
// MaterialColor.BROWN + flammable); both of those classes are equally unportable and are gone from the
// 1.20.1 tree. The only two consumers, common/block/properties/PropertiesMarble and
// common/block/properties/PropertiesWood, already build their Block.Properties from the no-arg
// BlockBehaviour.Properties#of() and carry that translation in their own class comments, so nothing is left
// for this holder to hand out. The class is kept (empty) so the 1.16.5 name still resolves and this
// explanation stays attached to it.
public class MaterialsAS {

    private MaterialsAS() {}

}
