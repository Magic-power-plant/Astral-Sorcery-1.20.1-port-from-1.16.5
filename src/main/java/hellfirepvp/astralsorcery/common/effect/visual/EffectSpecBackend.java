package hellfirepvp.astralsorcery.common.effect.visual;

/** Resolves a data description to a fresh spawn builder; implementations own rendering and validation. */
@FunctionalInterface
public interface EffectSpecBackend<T extends EffectHandle> {

    EffectBuilder<? extends T> createEffectBuilder(EffectSpec spec);
}
