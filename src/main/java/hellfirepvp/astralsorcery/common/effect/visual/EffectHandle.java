package hellfirepvp.astralsorcery.common.effect.visual;

/**
 * Lifecycle capability shared by particles and effect sources. State belongs to the implementation's
 * abstract entity base; this contract neither ticks an effect nor owns a second lifecycle.
 */
public interface EffectHandle {

    /** Whether the effect is currently absent from its effect handler (also true before registration). */
    boolean isRemoved();

    /** Request removal through the implementation's normal tick/refresh policy. */
    void requestRemoval();
}
