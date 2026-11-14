/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.fountain;

import hellfirepvp.astralsorcery.common.crafting.nojson.FountainEffectRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.fountain.FountainEffect;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockFountainPrimeOre
 * Created by HellFirePvP
 * Date: 31.10.2020 / 17:36
 */
public class BlockFountainPrimeOre extends BlockFountainPrime {

    @Nonnull
    @Override
    //1.16.5 returns EFFECT_LIQUID here as well (BlockFountainPrimeOre.java:27); kept verbatim, including the
    //apparent copy/paste in the reference, so behaviour stays identical.
    public FountainEffect<?> provideEffect() {
        return FountainEffectRegistry.EFFECT_LIQUID;
    }
}
