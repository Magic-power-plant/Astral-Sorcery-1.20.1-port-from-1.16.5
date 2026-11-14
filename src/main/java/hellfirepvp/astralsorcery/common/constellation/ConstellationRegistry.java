package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

/** Sorted domain views of the canonical constellation registry. */
public final class ConstellationRegistry {
    private ConstellationRegistry() {}

    public static <T extends IConstellation> void addConstellation(T constellation) {
        register(constellation);
    }

    public static void register(IWeakConstellation constellation) {
        register((IConstellation) constellation);
    }

    public static void register(IConstellation constellation) {
        if (!(constellation instanceof IWeakConstellation) && !(constellation instanceof IMinorConstellation)) {
            throw new IllegalArgumentException("Tried to register non-minor, non-weak and non-major constellation.");
        }
        RegistriesAS.CONSTELLATIONS.register(constellation);
    }

    @Nullable
    public static IConstellation getConstellation(@Nullable ResourceLocation id) {
        return RegistriesAS.CONSTELLATIONS.getValue(id);
    }

    private static <T extends IConstellation> Collection<T> ofType(Class<T> type) {
        return RegistriesAS.CONSTELLATIONS.getValues().stream()
                .filter(type::isInstance).map(type::cast).sorted().toList();
    }

    public static Collection<IConstellationSpecialShowup> getSpecialShowupConstellations() {
        return ofType(IConstellationSpecialShowup.class);
    }

    public static Collection<IWeakConstellation> getWeakConstellations() {
        return ofType(IWeakConstellation.class);
    }

    public static Collection<IMajorConstellation> getMajorConstellations() {
        return ofType(IMajorConstellation.class);
    }

    public static Collection<IMinorConstellation> getMinorConstellations() {
        return ofType(IMinorConstellation.class);
    }

    public static Collection<IConstellation> getAllConstellations() {
        return ofType(IConstellation.class);
    }

    public static Iterable<IWeakConstellation> getAll() {
        return getWeakConstellations();
    }
}
