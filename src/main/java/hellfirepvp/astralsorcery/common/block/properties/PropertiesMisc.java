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
import net.minecraft.world.level.material.PushReaction;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PropertiesMisc
 * Created by HellFirePvP
 * Date: 21.07.2019 / 08:25
 */
//net.minecraft.block.material.Material was deleted in 1.20, so every 1.16.5
//`Block.Properties.create(Material[, MaterialColor])` here becomes the no-arg BlockBehaviour.Properties#of() plus
//explicit builder calls for the values the Material used to supply. Only two of a Material's flags were ever visible
//through Properties and both have a 1.20.1 builder equivalent:
// - the map colour (create(Material) took material.getColor(); the two-arg form took the explicit MaterialColor).
//   1.16.5 MaterialColor -> 1.20.1 MapColor: AIR -> NONE, SAND -> SAND, STONE -> STONE, FOLIAGE -> PLANT,
//   IRON -> METAL, GOLD -> GOLD.
// - PushReaction (Material#getPushReaction()) -> Properties#pushReaction(PushReaction), and Material#isReplaceable()
//   -> Properties#replaceable(). Material.AIR is `doesNotBlockMovement().notOpaque().notSolid().replaceable()` and
//   Material.PLANTS is `doesNotBlockMovement().notSolid().pushDestroys()`, so those two carry over below.
//TODO_PORT[AS-BLK-15]: the remaining Material flags (isSolid / isOpaque / isLiquid) were consumed through
//`BlockState#getMaterial()` by callers, not by Properties, and 1.20.1 removed the concept outright - the equivalent
//1.20.1 information is derived per-state from the collision/occlusion shapes. Nothing here can restate them.
//Other renames used below: 1.16.5 `hardnessAndResistance` -> 1.20.1 `strength`,
//`doesNotBlockMovement` -> `noCollission`, `tickRandomly` -> `randomTicks`, `SoundType.PLANT` -> `SoundType.GRASS`.
//Forge deleted ToolType in 1.18, so `.harvestTool(ToolType.SHOVEL/PICKAXE)` and `.harvestLevel(1)` are now the
//minecraft:mineable/* and forge:needs_*_tool block tags and have to be carried in data instead.
public class PropertiesMisc {

    public static Block.Properties defaultAir() {
        return Block.Properties.of()
                .mapColor(MapColor.NONE)
                .replaceable()
                .noCollission();
    }

    public static Block.Properties defaultSand() {
        return Block.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.5F)
                .sound(SoundType.SAND);
    }

    public static Block.Properties defaultRock() {
        return Block.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE);
    }

    public static Block.Properties defaultMetal(MapColor color) {
        return Block.Properties.of()
                .mapColor(color)
                .strength(1.5F, 6.0F)
                .sound(SoundType.METAL);
    }

    public static Block.Properties defaultPlant() {
        return Block.Properties.of()
                .mapColor(MapColor.PLANT)
                .pushReaction(PushReaction.DESTROY)
                .noCollission()
                .strength(0)
                .sound(SoundType.GRASS);
    }

    public static Block.Properties defaultTickingPlant() {
        return Block.Properties.of()
                .mapColor(MapColor.PLANT)
                .pushReaction(PushReaction.DESTROY)
                .noCollission()
                .randomTicks()
                .strength(0)
                .sound(SoundType.GRASS);
    }

    public static Block.Properties defaultGoldMachinery() {
        return Block.Properties.of()
                .mapColor(MapColor.GOLD)
                .strength(1.0F, 4.0F)
                .sound(SoundType.STONE);
    }

}
