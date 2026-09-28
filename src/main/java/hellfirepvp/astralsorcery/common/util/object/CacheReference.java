package hellfirepvp.astralsorcery.common.util.object;

import java.util.function.Supplier;

public class CacheReference<T> implements Supplier<T> {

    private final Supplier<T> objectSupplier;
    private T object = null;

    public CacheReference(Supplier<T> objectSupplier) {
        this.objectSupplier = objectSupplier;
    }

    @Override
    public T get() {
        if (object == null) {
            object = objectSupplier.get();
        }
        return object;
    }
}
