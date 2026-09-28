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
 * Class: PropertiesMarble
 * Created by HellFirePvP
 * Date: 10.07.2019 / 21:02
 */
//1.16.5 built these from `Block.Properties.create(MaterialsAS.MARBLE)` / `(MaterialsAS.BLACK_MARBLE)` and
//additionally called `.harvestLevel(0)` and `.harvestTool(ToolType.PICKAXE)`. In 1.20.1:
// - net.minecraft.block.material.Material (and therefore MaterialsAS.MARBLE / BLACK_MARBLE) was deleted in 1.20, so
//   the builder entry point is the no-arg BlockBehaviour.Properties#of(). Both materials were plain
//   `new MaterialBuilderAS(<color>).build()` (1.16.5 RegistryMaterials.java:29-32), i.e. nothing but
//   MaterialBuilderAS' defaults - solid, opaque, blocks movement, PushReaction.NORMAL - which are also the 1.20.1
//   Properties#of() defaults. The one value they did carry is the map colour, which the single-argument
//   Properties#create(Material) copied off the material, so it has to be restated here:
//   1.16.5 MaterialColor.WHITE_TERRACOTTA -> 1.20.1 MapColor.TERRACOTTA_WHITE, and
//   1.16.5 MaterialColor.BLACK -> 1.20.1 MapColor.COLOR_BLACK.
// - Forge deleted ToolType in 1.18; harvest tool and harvest level are expressed through the
//   minecraft:mineable/pickaxe and forge:needs_*_tool block tags. `.harvestLevel(0)` was "any tool tier", which
//   is the default when no forge:needs_*_tool tag is present, so only the mineable/pickaxe tag has to be carried
//   in data - it is not expressible here.
// - 1.16.5 `setRequiresTool()` is 1.20.1 `requiresCorrectToolForDrops()`.
// - 1.16.5 `hardnessAndResistance(f, f)` is 1.20.1 `strength(f, f)`.
public class PropertiesMarble {

    public static Block.Properties defaultMarble() {
        return Block.Properties.of()
                .mapColor(MapColor.TERRACOTTA_WHITE)
                .strength(3F, 5F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }

    public static Block.Properties defaultBlackMarble() {
        return Block.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(3F, 5F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }

}
