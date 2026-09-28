/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.freezing;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FluidFreezingRecipe
 * Created by HellFirePvP
 * Date: 30.11.2019 / 21:09
 */
public class FluidFreezingRecipe extends BlockFreezingRecipe {

    public FluidFreezingRecipe() {
        super(AstralSorcery.key("all_fluids_freezing"),
                // 1.16.5 FluidState#getBlockState() is FluidState#createLegacyBlock() in official mappings
                (world, pos, state) -> state.getFluidState().isSource() &&
                        state.getFluidState().createLegacyBlock().equals(state),
                (worldPos, state) -> {
                    // 1.16.5 Fluid#getAttributes() / FluidAttributes were removed in 1.20.1; the replacement is
                    // Fluid#getFluidType() and FluidType#getTemperature(FluidState, BlockAndTintGetter, BlockPos)
                    // (Level implements BlockAndTintGetter via LevelReader, WorldBlockPos is a BlockPos)
                    FluidType fType = state.getFluidState().getType().getFluidType();
                    if (fType.getTemperature(state.getFluidState(), worldPos.getWorld(), worldPos) <= 300) {
                        return Blocks.ICE.defaultBlockState();
                    } else if (fType.getTemperature(state.getFluidState(), worldPos.getWorld(), worldPos) >= 500) {
                        return Blocks.OBSIDIAN.defaultBlockState();
                    }
                    return state;
                });
    }
}
