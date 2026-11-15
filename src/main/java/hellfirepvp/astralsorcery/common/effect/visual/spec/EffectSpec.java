package hellfirepvp.astralsorcery.common.effect.visual.spec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

import static hellfirepvp.astralsorcery.common.effect.visual.spec.EffectSpecCodecs.*;

/**
 * Immutable, serializable description of one visual effect. No renderer, callback, world or lifecycle
 * state is retained. Omitted properties inherit the selected template's defaults, including sprite age.
 * Positions, ownership and spawn-limit policy belong to the builder used for each submission.
 */
public record EffectSpec(ResourceLocation template, Properties properties, Optional<Beam> beam) {

    public static final Codec<EffectSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("template").forGetter(EffectSpec::template),
            optional(Properties.CODEC, "properties").xmap(p -> p.orElse(Properties.EMPTY),
                    p -> p.equals(Properties.EMPTY) ? Optional.empty() : Optional.of(p)).forGetter(EffectSpec::properties),
            optional(Beam.CODEC, "beam").forGetter(EffectSpec::beam)
    ).apply(instance, EffectSpec::new));

    public EffectSpec {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(properties, "properties");
        Objects.requireNonNull(beam, "beam");
    }

    public static Builder builder(ResourceLocation template) {
        return new Builder(new EffectSpec(template, Properties.EMPTY, Optional.empty()));
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    /** Optional overrides, not a second set of renderer defaults. */
    public record Properties(Optional<Integer> maxAge, Optional<Float> scale, Optional<Float> alpha,
                             Optional<Integer> color, Optional<Vector> motion, Optional<Float> gravity,
                             Optional<AlphaMode> alphaMode, Optional<ScaleMode> scaleMode) {
        public static final Properties EMPTY = new Properties(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());

        public static final Codec<Properties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                optional(AGE, "max_age").forGetter(Properties::maxAge),
                optional(SCALE, "scale").forGetter(Properties::scale),
                optional(ALPHA, "alpha").forGetter(Properties::alpha),
                optional(RGB, "color").forGetter(Properties::color),
                optional(Vector.CODEC, "motion").forGetter(Properties::motion),
                optional(FINITE_FLOAT, "gravity").forGetter(Properties::gravity),
                optional(AlphaMode.CODEC, "alpha_mode").forGetter(Properties::alphaMode),
                optional(ScaleMode.CODEC, "scale_mode").forGetter(Properties::scaleMode)
        ).apply(instance, Properties::new));

        public Properties {
            Objects.requireNonNull(maxAge, "maxAge").ifPresent(n -> require(n > 0, "max_age must be positive"));
            Objects.requireNonNull(scale, "scale").ifPresent(n -> require(Float.isFinite(n) && n >= 0, "scale must be finite and non-negative"));
            Objects.requireNonNull(alpha, "alpha").ifPresent(n -> require(Float.isFinite(n) && n >= 0 && n <= 1, "alpha must be in [0, 1]"));
            Objects.requireNonNull(color, "color").ifPresent(n -> require(n >= 0 && n <= 0xFFFFFF, "color must be 24-bit RGB"));
            Objects.requireNonNull(motion, "motion");
            Objects.requireNonNull(gravity, "gravity").ifPresent(n -> require(Float.isFinite(n), "gravity must be finite"));
            Objects.requireNonNull(alphaMode, "alphaMode");
            Objects.requireNonNull(scaleMode, "scaleMode");
        }
    }

    /** Value vector: never retains or exposes the project's mutable Vector3. */
    public record Vector(double x, double y, double z) {
        public static final Codec<Vector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FINITE_DOUBLE.fieldOf("x").forGetter(Vector::x),
                FINITE_DOUBLE.fieldOf("y").forGetter(Vector::y),
                FINITE_DOUBLE.fieldOf("z").forGetter(Vector::z)
        ).apply(instance, Vector::new));

        public Vector {
            require(Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z), "vector must be finite");
        }

        public static Vector copyOf(Vector3 vector) {
            Objects.requireNonNull(vector, "vector");
            return new Vector(vector.getX(), vector.getY(), vector.getZ());
        }

        public Vector3 toVector3() {
            return new Vector3(x, y, z);
        }
    }

    /** Endpoint relative to the spawn position, so a description can be reused anywhere. */
    public record Beam(Vector endOffset, double fromSize, double toSize) {
        public static final Codec<Beam> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                checked(Vector.CODEC, EffectSpecCodecs::validBeamOffset, "non-zero, finite-length beam offset").fieldOf("end_offset").forGetter(Beam::endOffset),
                WIDTH.fieldOf("from_size").forGetter(Beam::fromSize),
                WIDTH.fieldOf("to_size").forGetter(Beam::toSize)
        ).apply(instance, Beam::new));

        public Beam {
            require(validBeamOffset(Objects.requireNonNull(endOffset, "endOffset")), "beam offset must have non-zero, finite length");
            require(Double.isFinite(fromSize) && fromSize >= 0, "from_size must be finite and non-negative");
            require(Double.isFinite(toSize) && toSize >= 0, "to_size must be finite and non-negative");
        }
    }

    public enum AlphaMode {
        CONSTANT, FADE_OUT, PYRAMID;
        public static final Codec<AlphaMode> CODEC = names(AlphaMode.class);
    }

    public enum ScaleMode {
        IDENTITY, SHRINK, SHRINK_EXP;
        public static final Codec<ScaleMode> CODEC = names(ScaleMode.class);
    }

    /** Mutable assembly only; build() always returns an independent immutable value. */
    public static final class Builder {
        private final ResourceLocation template;
        private Optional<Integer> maxAge;
        private Optional<Float> scale;
        private Optional<Float> alpha;
        private Optional<Integer> color;
        private Optional<Vector> motion;
        private Optional<Float> gravity;
        private Optional<AlphaMode> alphaMode;
        private Optional<ScaleMode> scaleMode;
        private Optional<Beam> beam;

        private Builder(EffectSpec spec) {
            template = spec.template();
            Properties p = spec.properties();
            maxAge = p.maxAge();
            scale = p.scale();
            alpha = p.alpha();
            color = p.color();
            motion = p.motion();
            gravity = p.gravity();
            alphaMode = p.alphaMode();
            scaleMode = p.scaleMode();
            beam = spec.beam();
        }

        public Builder maxAge(int value) { maxAge = Optional.of(value); return this; }
        public Builder scale(float value) { scale = Optional.of(value); return this; }
        public Builder alpha(float value) { alpha = Optional.of(value); return this; }
        public Builder color(int rgb) { color = Optional.of(rgb); return this; }
        public Builder motion(Vector3 value) { motion = Optional.of(Vector.copyOf(value)); return this; }
        public Builder gravity(float value) { gravity = Optional.of(value); return this; }
        public Builder alphaMode(AlphaMode value) { alphaMode = Optional.of(value); return this; }
        public Builder scaleMode(ScaleMode value) { scaleMode = Optional.of(value); return this; }
        public Builder beam(Vector3 endOffset, double fromSize, double toSize) {
            beam = Optional.of(new Beam(Vector.copyOf(endOffset), fromSize, toSize));
            return this;
        }

        public EffectSpec build() {
            return new EffectSpec(template, new Properties(maxAge, scale, alpha, color, motion,
                    gravity, alphaMode, scaleMode), beam);
        }
    }

}
