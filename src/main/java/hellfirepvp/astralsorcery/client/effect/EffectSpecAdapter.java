package hellfirepvp.astralsorcery.client.effect;

import hellfirepvp.astralsorcery.api.effect.EffectSpec;

/** Template-specific geometry capability. Common visual properties are applied by the registry first. */
public interface EffectSpecAdapter<T extends EntityVisualFX> {

    /** Reject missing or unsupported geometry before a particle is created or submitted. */
    void validate(EffectSpec spec);

    /** Initialize the concrete particle before registration; do not register or tick it here. */
    void apply(T effect, EffectSpec spec);
}
