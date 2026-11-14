package hellfirepvp.astralsorcery.common.effect;

import javax.annotation.Nonnull;

/**
 * Typed factory and re-registration capability. Implementations supply the rendering backend without
 * exposing its contexts, textures or registration machinery in the common API. Extension renderers
 * can implement this interface without changing Effects or adding a global service registration.
 */
public interface EffectTemplate<T extends EffectHandle> {

    /** Each call must return a fresh builder: owner/position/limit settings must not leak between spawns. */
    @Nonnull
    EffectBuilder<T> createEffectBuilder();

    /** Re-submit the same removed handle, preserving its age, configuration and identity. */
    void refreshEffect(@Nonnull T effect);
}
