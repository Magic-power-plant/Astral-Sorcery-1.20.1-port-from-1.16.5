package hellfirepvp.astralsorcery.common.effect.visual;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.Vec3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Common spawn configuration. The returned handle retains its concrete type so an effect can be
 * configured after spawning, even when the renderer rejects registration (paused, capped, etc.).
 * Renderer-specific properties remain on the implementation's builder.
 */
public interface EffectBuilder<T extends EffectHandle> {

    EffectBuilder<T> setOwner(@Nullable UUID owner);

    EffectBuilder<T> setPosition(@Nonnull Vec3i position);

    EffectBuilder<T> setIgnoreLimit(boolean ignoreLimit);

    /**
     * Create a non-null handle and submit it to the effect handler. As with the existing renderer,
     * the spawn position becomes the registration position; acceptance is not guaranteed.
     */
    @Nonnull
    T spawn(@Nonnull Vector3 position);
}
