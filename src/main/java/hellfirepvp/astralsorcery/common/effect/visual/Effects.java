package hellfirepvp.astralsorcery.common.effect.visual;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Common entry point for creating, refreshing and submitting visual effects.
 *
 * The facade depends only on capabilities; the supplied template/source selects the backend. Loading
 * this API does not load a client renderer, and there is no client singleton or mutable global backend.
 * Existing client templates and sources still require a client execution context: keep their caller's
 * client-side guards. This is not a packet API and does not forward effects from a server to clients.
 */
public final class Effects {

    private Effects() {}

    @Nonnull
    public static <T extends EffectHandle> EffectBuilder<T> of(@Nonnull EffectTemplate<T> template) {
        return Objects.requireNonNull(Objects.requireNonNull(template, "template").createEffectBuilder(),
                "effect builder");
    }

    /** Resolve immutable data using an explicitly selected backend; no global backend installation. */
    @Nonnull
    public static <T extends EffectHandle> EffectBuilder<? extends T> of(
            @Nonnull EffectSpec spec, @Nonnull EffectSpecBackend<T> backend) {
        Objects.requireNonNull(spec, "spec");
        return Objects.requireNonNull(Objects.requireNonNull(backend, "backend").createEffectBuilder(spec),
                "effect builder");
    }

    public static <T extends EffectHandle> void refresh(@Nonnull T effect, @Nonnull EffectTemplate<T> template) {
        Objects.requireNonNull(effect, "effect");
        Objects.requireNonNull(template, "template");
        if (effect.isRemoved()) {
            template.refreshEffect(effect);
        }
    }

    /** Return the original concrete source so callers can keep configuring it and track its lifecycle. */
    @Nonnull
    public static <S extends EffectSource> S spawnSource(@Nonnull S source) {
        Objects.requireNonNull(source, "source").spawnEffectSource();
        return source;
    }
}
