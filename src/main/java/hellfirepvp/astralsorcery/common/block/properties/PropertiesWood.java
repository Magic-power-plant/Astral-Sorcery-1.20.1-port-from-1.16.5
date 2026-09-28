/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.properties;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PropertiesWood
 * Created by HellFirePvP
 * Date: 20.07.2019 / 20:00
 */
//1.16.5 built this from `Block.Properties.create(MaterialsAS.INFUSED_WOOD)` and additionally called
//`.harvestTool(ToolType.AXE)`. In 1.20.1:
// - net.minecraft.block.material.Material (and therefore MaterialsAS.INFUSED_WOOD) was deleted in 1.20, so the
//   builder entry point is the no-arg BlockBehaviour.Properties#of(). MaterialsAS.INFUSED_WOOD was
//   `new MaterialBuilderAS(MaterialColor.BROWN).flammable().build()` (1.16.5 RegistryMaterials.java:33-35), i.e. all
//   of MaterialBuilderAS' defaults (solid, opaque, blocks movement, PushReaction.NORMAL - which are also the 1.20.1
//   Properties#of() defaults) plus a brown map colour and the flammable flag. The single-argument
//   Properties#create(Material) took the map colour straight off the material, so both have to be restated here:
//   1.16.5 MaterialColor.BROWN -> 1.20.1 MapColor.COLOR_BROWN, and 1.16.5 Material#isFlammable() (the `canBurn`
//   flag) -> 1.20.1 Properties#ignitedByLava().
// - Forge deleted ToolType in 1.18; the harvest tool is expressed through the minecraft:mineable/axe block tag, so
//   `.harvestTool(ToolType.AXE)` has to be carried in data and is not expressible here.
// - 1.16.5 `hardnessAndResistance(f, f)` is 1.20.1 `strength(f, f)`.
public class PropertiesWood {

    public static Block.Properties defaultInfusedWood() {
        return Block.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .ignitedByLava()
                .strength(2.5F, 7F)
                .sound(SoundType.WOOD);
    }

}
