/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SingleBiomeManager
 * Created by HellFirePvP
 * Date: 04.06.2020 / 21:19
 *
 * Embedded VERBATIM from ObserverLib (hellfirepvp.observerlib.api.util.SingleBiomeManager); ObserverLib is a
 * source-only reference for this port, not a runtime dependency. Only the package declaration, the license header
 * and the {@link OnlyIn} annotation differ - the body is unmodified so it can be re-synced later.
 *
 * A {@link BiomeManager} that reports one biome everywhere, so {@link StructureRenderWorld} can answer biome
 * queries without a real level.
 */
@OnlyIn(Dist.CLIENT)
public class SingleBiomeManager extends BiomeManager {

    private final Holder<Biome> globalBiome;

    public SingleBiomeManager(Holder<Biome> globalBiome) {
        super((x, y, z) -> globalBiome, 0);
        this.globalBiome = globalBiome;
    }

    @Override
    public BiomeManager withDifferentSource(NoiseBiomeSource p_186688_) {
        return this;
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pPos) {
        return this.globalBiome;
    }
}
