package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ConstellationRegistry {
    private static final Map<ResourceLocation, IConstellation> constellations = new HashMap<>();
    private static final SortedSet<IMajorConstellation> majorConstellations = new TreeSet<>();
    private static final SortedSet<IWeakConstellation> weakConstellations = new TreeSet<>();
    private static final SortedSet<IMinorConstellation> minorConstellations = new TreeSet<>();
    private static final SortedSet<IConstellationSpecialShowup> specialShowupConstellations = new TreeSet<>();

    public static <T extends IConstellation> void addConstellation(T constellation) {
        if (constellation instanceof IWeakConstellation weak) {
            if (constellation instanceof IMajorConstellation major) {
                majorConstellations.add(major);
            }
            weakConstellations.add(weak);
        } else if (constellation instanceof IMinorConstellation minor) {
            minorConstellations.add(minor);
        } else {
            throw new IllegalArgumentException("Tried to register non-minor, non-weak and non-major constellation.");
        }
        if (constellation instanceof IConstellationSpecialShowup specialShowup) {
            specialShowupConstellations.add(specialShowup);
        }
        constellations.put(constellation.getRegistryName(), constellation);
    }

    public static void register(IWeakConstellation constellation) {
        addConstellation(constellation);
    }

    public static void register(IConstellation constellation) {
        addConstellation(constellation);
    }

    @Nullable
    public static IConstellation getConstellation(@Nullable ResourceLocation id) {
        return id == null ? null : constellations.get(id);
    }

    public static Collection<IConstellationSpecialShowup> getSpecialShowupConstellations() {
        return Collections.unmodifiableCollection(specialShowupConstellations);
    }

    public static Collection<IWeakConstellation> getWeakConstellations() {
        return Collections.unmodifiableCollection(weakConstellations);
    }

    public static Collection<IMajorConstellation> getMajorConstellations() {
        return Collections.unmodifiableCollection(majorConstellations);
    }

    public static Collection<IMinorConstellation> getMinorConstellations() {
        return Collections.unmodifiableCollection(minorConstellations);
    }

    public static Collection<IConstellation> getAllConstellations() {
        List<IConstellation> all = new ArrayList<>(constellations.values());
        Collections.sort(all);
        return Collections.unmodifiableList(all);
    }

    public static Iterable<IWeakConstellation> getAll() {
        return getWeakConstellations();
    }
}
