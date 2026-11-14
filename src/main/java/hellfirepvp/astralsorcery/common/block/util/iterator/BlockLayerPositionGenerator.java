package hellfirepvp.astralsorcery.common.block.util.iterator;

import hellfirepvp.astralsorcery.common.block.util.BlockGeometry;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

public class BlockLayerPositionGenerator extends BlockPositionGenerator {

    private int layer = 0;

    private final LinkedList<BlockPos> currentPositions = new LinkedList<>();

    @Override
    protected BlockPos genNext(Vector3 offset, double radius) {
        int size = Mth.floor(radius);

        while (currentPositions.isEmpty()) {
            generatePositions(size);
        }
        return this.currentPositions.pop();
    }

    private void generatePositions(int maxLayers) {
        if (maxLayers <= 0) {
            this.currentPositions.add(BlockPos.ZERO);
            return;
        }
        this.layer++;
        if (this.layer > maxLayers) {
            this.layer = -maxLayers;
        }
        Collection<BlockPos> positions = BlockGeometry.getPlane(Direction.UP, maxLayers);
        positions.forEach(pos -> this.currentPositions.add(pos.offset(0, this.layer, 0)));
        Collections.shuffle(this.currentPositions, new Random(0xF518E23A05B27C19L));
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putInt("layer", this.layer);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        this.layer = nbt.getInt("layer");
    }
}
