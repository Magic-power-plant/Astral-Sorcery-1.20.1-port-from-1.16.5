package hellfirepvp.astralsorcery.common.effect.visual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.PrimitiveCodec;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Validation primitives kept separate to avoid initialization cycles between the nested data codecs. */
final class EffectSpecCodecs {
    static final Codec<Float> FINITE_FLOAT = checked(Codec.FLOAT, Float::isFinite, "finite float");
    static final Codec<Float> SCALE = checked(FINITE_FLOAT, n -> n >= 0, "non-negative scale");
    static final Codec<Float> ALPHA = checked(FINITE_FLOAT, n -> n >= 0 && n <= 1, "alpha in [0, 1]");
    private static final Codec<Integer> INTEGER = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Integer> read(DynamicOps<T> ops, T input) {
            return ops.getNumberValue(input).flatMap(number -> {
                double value = number.doubleValue();
                return Double.isFinite(value) && value == Math.rint(value)
                        && value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE
                        ? DataResult.success(number.intValue())
                        : DataResult.error(() -> "Expected a 32-bit integer: " + number);
            });
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Integer value) {
            return ops.createInt(value);
        }
    };
    static final Codec<Integer> AGE = checked(INTEGER, n -> n > 0, "positive max_age");
    static final Codec<Integer> RGB = checked(INTEGER, n -> n >= 0 && n <= 0xFFFFFF, "24-bit RGB color");
    static final Codec<Double> FINITE_DOUBLE = checked(Codec.DOUBLE, Double::isFinite, "finite double");
    static final Codec<Double> WIDTH = checked(FINITE_DOUBLE, n -> n >= 0, "non-negative beam width");

    private EffectSpecCodecs() {}

    static void require(boolean valid, String message) {
        if (!valid) throw new IllegalArgumentException(message);
    }

    static boolean validBeamOffset(EffectSpec.Vector vector) {
        double lengthSquared = vector.x() * vector.x() + vector.y() * vector.y() + vector.z() * vector.z();
        return lengthSquared > 0 && Double.isFinite(lengthSquared);
    }

    static <T> Codec<T> checked(Codec<T> codec, Predicate<T> valid, String description) {
        return codec.flatXmap(value -> valid.test(value) ? DataResult.success(value)
                        : DataResult.error(() -> "Expected " + description + ": " + value),
                value -> valid.test(value) ? DataResult.success(value)
                        : DataResult.error(() -> "Expected " + description + ": " + value));
    }

    static <E extends Enum<E>> Codec<E> names(Class<E> type) {
        return Codec.STRING.comapFlatMap(name -> {
            for (E value : type.getEnumConstants()) {
                if (value.name().toLowerCase(Locale.ROOT).equals(name)) return DataResult.success(value);
            }
            return DataResult.error(() -> "Unknown " + type.getSimpleName() + ": " + name);
        }, value -> value.name().toLowerCase(Locale.ROOT));
    }

    /** Missing is allowed; a present but malformed value must never silently inherit a default. */
    static <A> MapCodec<Optional<A>> optional(Codec<A> codec, String name) {
        return new MapCodec<>() {
            @Override
            public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
                T value = input.get(name);
                if (value == null) {
                    // JsonOps MapLike#get treats JSON null like an absent value; entries retain the key.
                    boolean present = input.entries().anyMatch(entry ->
                            ops.getStringValue(entry.getFirst()).result().filter(name::equals).isPresent());
                    return present ? DataResult.error(() -> name + ": null is not a valid value")
                            : DataResult.success(Optional.empty());
                }
                return codec.parse(ops, value).map(Optional::of).mapError(error -> name + ": " + error);
            }

            @Override
            public <T> RecordBuilder<T> encode(Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return input.isEmpty() ? prefix : prefix.add(name, codec.encodeStart(ops, input.orElseThrow()));
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString(name));
            }
        };
    }
}
