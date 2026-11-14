package hellfirepvp.astralsorcery.common.effect;

/** A lifecycle-managed effect producer, such as an orbital particle source. */
public interface EffectSource extends EffectHandle {

    /** Submit this source using the backend's usual scheduling and registration policy. */
    void spawnEffectSource();
}
