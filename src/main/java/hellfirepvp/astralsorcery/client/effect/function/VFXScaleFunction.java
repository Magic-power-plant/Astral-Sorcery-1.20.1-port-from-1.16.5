package hellfirepvp.astralsorcery.client.effect.function;

import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.util.RenderingVectorUtils;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public interface VFXScaleFunction<T extends EntityVisualFX> {

    VFXScaleFunction<EntityVisualFX> IDENTITY = (fx, scaleIn, pTicks) -> scaleIn;

    VFXScaleFunction<EntityVisualFX> SHRINK = (fx, scaleIn, pTicks) -> {
        float prevAge = Math.max(0F, ((float) fx.getAge() - 1)) / ((float) fx.getMaxAge());
        float currAge = Math.max(0F, ((float) fx.getAge())) / ((float) fx.getMaxAge());
        return (float) (scaleIn * (1 - (RenderingVectorUtils.interpolate(prevAge, currAge, pTicks))));
    };

    VFXScaleFunction<EntityVisualFX> SHRINK_EXP = (fx, scaleIn, pTicks) -> {
        return Mth.sqrt(SHRINK.getScale(fx, scaleIn, pTicks));
    };

    float getScale(@Nonnull T fx, float scaleIn, float pTicks);

    default VFXScaleFunction<T> andThen(VFXScaleFunction<T> multiplied) {
        VFXScaleFunction<T> existing = this;
        return (fx, scaleIn, pTicks) -> multiplied.getScale(fx, existing.getScale(fx, scaleIn, pTicks), pTicks);
    }
}
