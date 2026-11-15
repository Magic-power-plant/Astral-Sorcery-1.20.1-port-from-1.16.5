package hellfirepvp.astralsorcery.common.registry;

/**
 * Owns one built-in assembly phase, separate from Forge registry publication.
 * Successful assembly is idempotent. Reentrant or failed assembly cannot be retried:
 * it may already have assigned holders, appended journal nodes or added signature items.
 * Reloadable data and extension registration windows do not use this lifecycle.
 */
public abstract class AbstractRegistryInitializer {
    private enum State { NEW, INITIALIZING, READY, FAILED }

    private State state = State.NEW;
    private Throwable failure;

    public final synchronized void initialize() {
        if (state == State.READY) return;
        if (state != State.NEW) {
            throw new IllegalStateException(getClass().getSimpleName() +
                    ": cannot initialize in state " + state, failure);
        }
        state = State.INITIALIZING;
        try {
            registerEntries();
            state = State.READY;
        } catch (RuntimeException | Error cause) {
            failure = cause;
            state = State.FAILED;
            throw cause;
        }
    }

    protected abstract void registerEntries();
}
