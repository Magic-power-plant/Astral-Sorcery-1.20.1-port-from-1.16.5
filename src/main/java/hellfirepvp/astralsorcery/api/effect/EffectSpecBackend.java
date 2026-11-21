package hellfirepvp.astralsorcery.api.effect;

import hellfirepvp.astralsorcery.common.effect.visual.EffectBuilder;
import hellfirepvp.astralsorcery.common.effect.visual.EffectHandle;

/** Resolves a data description to a fresh spawn builder; implementations own rendering and validation. */
@FunctionalInterface
public interface EffectSpecBackend<T extends EffectHandle> {

    EffectBuilder<? extends T> createEffectBuilder(EffectSpec spec);
}
