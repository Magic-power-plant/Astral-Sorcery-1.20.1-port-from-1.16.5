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

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PropertiesGlass
 * Created by HellFirePvP
 * Date: 10.07.2019 / 21:03
 */
//1.16.5 built this from `Block.Properties.create(Material.GLASS)`. net.minecraft.block.material.Material was deleted
//in 1.20, so the entry point is the no-arg BlockBehaviour.Properties#of().
//Material.GLASS is `new Material.Builder(MaterialColor.AIR).notSolid().build()`, so the only Properties-level value
//it contributed through the single-argument create(Material) is the map colour MaterialColor.AIR, which is exactly
//1.20.1's Properties#of() default of MapColor.NONE - hence no mapColor call is needed.
//VERIFY_PORT[AS-BLK-14]: Material.GLASS' `notSolid` was a *material* flag, not the Properties#notSolid() that
//1.16.5's vanilla glass block additionally called, and coatedGlass() never called Properties#notSolid() itself. The
//1.20.1 replacement for a material-level "not solid" is Properties#noOcclusion(), but adding it here would change
//every coated-glass block (spectral relay, celestial gateway, illuminator, lens, prism, ritual link) from occluding
//to non-occluding, which 1.16.5's Properties did not do either. Left matching the reference's Properties calls
//exactly; needs an in-game look at those six blocks to confirm no render regression versus 1.16.5.
//1.16.5 `hardnessAndResistance(f, f)` is 1.20.1 `strength(f, f)`.
public class PropertiesGlass {

    public static Block.Properties coatedGlass() {
        return Block.Properties.of()
                .strength(1F, 5F)
                .sound(SoundType.GLASS);
    }

}
