/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.common.block.base.BlockFakedState;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.utility.TranslucentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Port of 1.16.5 {@code BlockTranslucentBlock}: a faked-state helper block placed by the illumination wand.
 */
public class BlockTranslucentBlock extends BlockFakedState {

    public BlockTranslucentBlock() {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(-1.0F, 6_000_000.0F)
                .lightLevel(state -> 12)
                .noOcclusion()
                .noLootTable());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        this.playParticles(world, pos, rand);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TranslucentBlockEntity(pos, state);
    }

    @Nullable
    @Override
    //1.16.5 needs no ticker hook: TileTranslucentBlock extends TileEntityTick, which implemented
    //ITickableTileEntity, so the world ticked it automatically. 1.20.1 routes ticking through
    //EntityBlock#getTicker, so the tick counter TileEntityTick keeps (and the sky/multiblock caches built on it)
    //only advances if the block hands one out.
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypesAS.TRANSLUCENT_BLOCK.get() ? (tickerLevel, tickerPos, tickerState, blockEntity) -> {
            if (blockEntity instanceof TranslucentBlockEntity translucent) {
                translucent.tick();
            }
        } : null;
    }
}
