package hellfirepvp.astralsorcery.common.block.util.iterator;

import hellfirepvp.astralsorcery.common.block.util.BlockGeometry;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockSpherePositionGenerator extends BlockPositionGenerator {

    private int currentRadius = 0;

    private final List<BlockPos> currentPositions = new ArrayList<>();

    @Override
    public BlockPos genNext(Vector3 offset, double radius) {
        if (this.currentRadius > radius) {
            this.currentPositions.clear();
        }

        while (currentPositions.isEmpty()) {
            generatePositions(radius);
        }
        return offset.add(currentPositions.remove(0)).toBlockPos();
    }

    private void generatePositions(double maxRadius) {
        if (maxRadius <= 0) {
            this.currentPositions.add(BlockPos.ZERO);
            return;
        }
        if (this.currentRadius >= maxRadius || this.currentRadius < 0) {
            this.currentRadius = 0;
        }
        this.currentRadius++;

        this.currentPositions.addAll(BlockGeometry.getHollowSphere(this.currentRadius, this.currentRadius - 1));
        Collections.shuffle(this.currentPositions, new Random(0xF518E23A05B27C19L));
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putInt("currentRadius", this.currentRadius);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        this.currentRadius = nbt.getInt("currentRadius");
    }
}
