package hellfirepvp.astralsorcery.client.effect;

import hellfirepvp.astralsorcery.client.effect.context.base.BatchRenderContext;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXScaleFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.api.effect.EffectSpec;
import hellfirepvp.astralsorcery.api.effect.EffectSpecBackend;
import net.minecraft.resources.ResourceLocation;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Typed bindings to the existing renderer. No effect queue, lifecycle state or cached asset contexts. */
public final class EffectSpecRegistry implements EffectSpecBackend<EntityVisualFX> {

    private final Map<ResourceLocation, Binding<?>> bindings = new LinkedHashMap<>();

    /** Register during client setup. Suppliers must return the current context after texture reloads. */
    public <T extends EntityVisualFX> void register(ResourceLocation id,
            Supplier<? extends BatchRenderContext<T>> context, EffectSpecAdapter<T> adapter) {
        Objects.requireNonNull(id, "id");
        Binding<T> binding = new Binding<>(Objects.requireNonNull(context, "context"),
                Objects.requireNonNull(adapter, "adapter"));
        if (bindings.putIfAbsent(id, binding) != null) {
            throw new IllegalArgumentException("Duplicate effect template: " + id);
        }
    }

    @Override
    public EffectHelper.Builder<? extends EntityVisualFX> createEffectBuilder(EffectSpec spec) {
        Objects.requireNonNull(spec, "spec");
        Binding<?> binding = bindings.get(spec.template());
        if (binding == null) throw new IllegalArgumentException("Unknown effect template: " + spec.template());
        return binding.createBuilder(spec);
    }

    private record Binding<T extends EntityVisualFX>(Supplier<? extends BatchRenderContext<T>> context,
                                                      EffectSpecAdapter<T> adapter) {
        EffectHelper.Builder<T> createBuilder(EffectSpec spec) {
            adapter.validate(spec);
            BatchRenderContext<T> current = context.get();
            if (current == null) {
                throw new IllegalStateException("Effect template is not initialized: " + spec.template());
            }
            return new EffectHelper.Builder<>(current, effect -> {
                applyProperties(effect, spec.properties());
                adapter.apply(effect, spec);
            });
        }
    }

    private static void applyProperties(EntityVisualFX effect, EffectSpec.Properties properties) {
        properties.maxAge().ifPresent(effect::setMaxAge);
        properties.scale().ifPresent(effect::setScaleMultiplier);
        properties.alpha().ifPresent(effect::setAlphaMultiplier);
        properties.color().ifPresent(rgb -> effect.color(VFXColorFunction.constant(new Color(rgb))));
        properties.motion().ifPresent(vector -> effect.setMotion(vector.toVector3()));
        properties.gravity().ifPresent(effect::setGravityStrength);
        properties.alphaMode().ifPresent(mode -> effect.alpha(switch (mode) {
            case CONSTANT -> VFXAlphaFunction.CONSTANT;
            case FADE_OUT -> VFXAlphaFunction.FADE_OUT;
            case PYRAMID -> VFXAlphaFunction.PYRAMID;
        }));
        properties.scaleMode().ifPresent(mode -> effect.scale(switch (mode) {
            case IDENTITY -> VFXScaleFunction.IDENTITY;
            case SHRINK -> VFXScaleFunction.SHRINK;
            case SHRINK_EXP -> VFXScaleFunction.SHRINK_EXP;
        }));
    }
}
