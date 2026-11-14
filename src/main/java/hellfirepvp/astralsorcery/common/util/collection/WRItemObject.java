package hellfirepvp.astralsorcery.common.util.collection;

import net.minecraft.util.random.WeightedEntry;

public class WRItemObject<T> extends WeightedEntry.IntrusiveBase {

    private final T object;

    public WRItemObject(int itemWeightIn, T value) {
        super(itemWeightIn);
        this.object = value;
    }

    public T getValue() {
        return object;
    }
}
