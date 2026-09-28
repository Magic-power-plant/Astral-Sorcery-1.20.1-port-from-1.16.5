package hellfirepvp.astralsorcery.common.tile.altar;

import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundTag;

import java.util.Deque;
import java.util.LinkedList;

public class DeferredStarlightStorage {

    private final Deque<Integer> starlightStorage = new LinkedList<>();
    private final int ticksDeferred;

    public DeferredStarlightStorage(int ticksDeferred) {
        Preconditions.checkArgument(ticksDeferred > 0, "DeferredStarlightStorage must be set to defer by at least 1 tick.");
        this.ticksDeferred = ticksDeferred;
    }

    public void setStoredStarlight(int starlight) {
        while (this.starlightStorage.size() >= this.ticksDeferred) {
            this.starlightStorage.removeLast();
        }
        this.starlightStorage.addFirst(starlight);
    }

    public int getStoredStarlight() {
        if (this.starlightStorage.isEmpty()) {
            return 0;
        }
        return starlightStorage.getLast();
    }

    public void readNBT(CompoundTag compound) {
        this.starlightStorage.clear();
        this.starlightStorage.addLast(compound.getInt("starlightStorage"));
    }

    public void writeNBT(CompoundTag compound) {
        compound.putInt("starlightStorage", this.getStoredStarlight());
    }
}
