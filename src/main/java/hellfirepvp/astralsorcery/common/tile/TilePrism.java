package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.common.block.tile.BlockPrism;
import hellfirepvp.astralsorcery.common.item.lens.LensColorType;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.tile.network.StarlightTransmissionPrism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Port of 1.16.5 {@code TilePrism}: a lens that allows multiple links and toggles the
 * {@link BlockPrism#HAS_COLORED_LENS} state when a colored lens is (un)installed.
 */
public class TilePrism extends TileLens {

    public TilePrism(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.PRISM.get(), pos, state);
    }

    @Override
    public boolean isSingleLink() {
        return false;
    }

    @Override
    public LensColorType setColorType(@Nullable LensColorType colorType) {
        LensColorType returned = super.setColorType(colorType);
        if (getLevel() == null) {
            return returned;
        }
        BlockState thisState = getLevel().getBlockState(getBlockPos());
        if (!(thisState.getBlock() instanceof BlockPrism)) {
            return returned;
        }

        if (thisState.getValue(BlockPrism.HAS_COLORED_LENS) && colorType == null && returned != null) {
            getLevel().setBlock(getBlockPos(), thisState.setValue(BlockPrism.HAS_COLORED_LENS, false), Block.UPDATE_ALL_IMMEDIATE);
        } else if (!thisState.getValue(BlockPrism.HAS_COLORED_LENS) && colorType != null && returned == null) {
            getLevel().setBlock(getBlockPos(), thisState.setValue(BlockPrism.HAS_COLORED_LENS, true), Block.UPDATE_ALL_IMMEDIATE);
        }
        return returned;
    }

    @Override
    public Direction getPlacedAgainst() {
        BlockState state = level.getBlockState(getBlockPos());
        if (!(state.getBlock() instanceof BlockPrism)) {
            return Direction.DOWN;
        }
        return state.getValue(BlockPrism.PLACED_AGAINST);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void onDataReceived() {
        super.onDataReceived();

        getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL_IMMEDIATE);
    }

    @Nonnull
    @Override
    public IPrismTransmissionNode provideTransmissionNode(BlockPos at) {
        return new StarlightTransmissionPrism(at, this.getAttributes());
    }
}
