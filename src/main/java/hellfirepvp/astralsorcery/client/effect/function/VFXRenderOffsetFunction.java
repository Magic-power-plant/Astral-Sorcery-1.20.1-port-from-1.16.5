package hellfirepvp.astralsorcery.client.effect.function;

import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.common.util.data.Vector3;

import javax.annotation.Nonnull;

public interface VFXRenderOffsetFunction<T extends EntityVisualFX> {

    VFXRenderOffsetFunction<?> IDENTITY = (fx, iPos, pTicks) -> iPos;

    @Nonnull
    Vector3 changeRenderPosition(@Nonnull T fx, Vector3 interpolatedPos, float pTicks);

}
