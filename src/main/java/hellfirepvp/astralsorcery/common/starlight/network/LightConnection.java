package hellfirepvp.astralsorcery.common.starlight.network;

import net.minecraft.core.BlockPos;

public class LightConnection {

    private final BlockPos from;
    private final BlockPos to;

    public LightConnection(BlockPos from, BlockPos to) {
        this.from = from;
        this.to = to;
    }

    public BlockPos getStart() {
        return from;
    }

    public BlockPos getEnd() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightConnection that = (LightConnection) o;
        return from.equals(that.from) && to.equals(that.to);
    }

    @Override
    public int hashCode() {
        return 31 * from.hashCode() + to.hashCode();
    }
}
