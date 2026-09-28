package hellfirepvp.astralsorcery.common.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

public interface ReadWriteLockable {

    ReadWriteLock getLock();

    default <T> T write(Supplier<T> fn) {
        return this.lock(this.getLock()::writeLock, fn);
    }

    default void write(Runnable run) {
        this.lock(this.getLock()::writeLock, MiscUtils.nullSupplier(run));
    }

    default <T> T read(Supplier<T> fn) {
        return this.lock(this.getLock()::readLock, fn);
    }

    default void read(Runnable run) {
        this.lock(this.getLock()::readLock, MiscUtils.nullSupplier(run));
    }

    default <T> T lock(Supplier<Lock> lock, Supplier<T> fn) {
        lock.get().lock();
        try {
            return fn.get();
        } finally {
            lock.get().unlock();
        }
    }
}
