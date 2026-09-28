package hellfirepvp.astralsorcery.common.structure.match.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleMatchableBlock implements MatchableState {

    private static final int CYCLE_STATES = 20;
    private final List<Block> matchingBlocks;
    private final List<BlockState> displayStates = new ArrayList<>();

    public SimpleMatchableBlock(Block... matchingBlocks) {
        this(Arrays.asList(matchingBlocks));
    }

    public SimpleMatchableBlock(List<Block> matchingBlocks) {
        this.matchingBlocks = matchingBlocks;
        for (Block block : matchingBlocks) {
            this.displayStates.addAll(block.getStateDefinition().getPossibleStates());
        }
    }

    @Nonnull
    @Override
    public BlockState getDescriptiveState(long tick) {
        int cycleState = Math.max(2, CYCLE_STATES / this.displayStates.size());
        int part = (int) (tick % (cycleState * this.displayStates.size()));
        return this.displayStates.get(part / cycleState);
    }

    @Override
    public boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state) {
        return this.matchingBlocks.contains(state.getBlock());
    }
}
