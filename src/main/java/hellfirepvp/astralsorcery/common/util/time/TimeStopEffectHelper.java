package hellfirepvp.astralsorcery.common.util.time;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import javax.annotation.Nonnull;

public class TimeStopEffectHelper {

    @Nonnull
    private final BlockPos position;
    private final float range;
    private final TimeStopZone.EntityTargetController targetController;

    private TimeStopEffectHelper(@Nonnull BlockPos position, float range, TimeStopZone.EntityTargetController targetController) {
        this.position = position;
        this.range = range;
        this.targetController = targetController;
    }

    static TimeStopEffectHelper fromZone(TimeStopZone zone) {
        return new TimeStopEffectHelper(zone.offset, zone.range, zone.targetController);
    }

    @Nonnull
    public BlockPos getPosition() {
        return position;
    }

    public float getRange() {
        return range;
    }

    public TimeStopZone.EntityTargetController getTargetController() {
        return targetController;
    }

    @Nonnull
    public CompoundTag serializeNBT() {
        CompoundTag out = new CompoundTag();
        NBTHelper.writeBlockPosToNBT(this.position, out);
        out.putFloat("range", this.range);
        out.put("targetController", this.targetController.serializeNBT());
        return out;
    }

    @Nonnull
    public static TimeStopEffectHelper deserializeNBT(CompoundTag cmp) {
        BlockPos at = NBTHelper.readBlockPosFromNBT(cmp);
        float range = cmp.getFloat("range");
        return new TimeStopEffectHelper(at, range, TimeStopZone.EntityTargetController.deserializeNBT(cmp.getCompound("targetController")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeStopEffectHelper that = (TimeStopEffectHelper) o;

        return Float.compare(that.range, range) == 0 &&
                position.equals(that.position);
    }

    @Override
    public int hashCode() {
        int result = position.hashCode();
        result = 31 * result + (range != +0.0f ? Float.floatToIntBits(range) : 0);
        return result;
    }

}
