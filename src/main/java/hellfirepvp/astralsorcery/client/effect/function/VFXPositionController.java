package hellfirepvp.astralsorcery.client.effect.function;

import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.common.util.data.Vector3;

import javax.annotation.Nonnull;

public interface VFXPositionController<T extends EntityVisualFX> {

    VFXPositionController<?> CONSTANT = (fx, position, motionToBeMoved) -> position.add(motionToBeMoved);

    @Nonnull
    Vector3 updatePosition(@Nonnull T fx, @Nonnull Vector3 position, @Nonnull Vector3 motionToBeMoved);

}
