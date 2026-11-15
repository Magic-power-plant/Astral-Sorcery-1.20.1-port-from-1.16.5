package hellfirepvp.astralsorcery.common.registry;

import com.google.gson.JsonParser;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstellationAssemblyTest {
    @Test
    void allSixteenGeometriesRetainTheirIdsOrderStarsConnectionsAndKinds() throws Exception {
        RegistryConstellations.init();
        var values = RegistriesAS.CONSTELLATIONS.getValues();
        var resource = getClass().getResourceAsStream("/registry/constellations.json");
        assertNotNull(resource);
        try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            var expected = JsonParser.parseReader(reader).getAsJsonArray();
            assertEquals(16, expected.size());
            assertEquals(expected.size(), values.size());
            for (int i = 0; i < expected.size(); i++) {
                var data = expected.get(i).getAsJsonObject();
                var actual = values.get(i);
                String name = data.get("name").getAsString();
                assertEquals(name, actual.getSimpleName());
                assertEquals("astralsorcery:" + name, actual.getRegistryName().toString());
                assertEquals("astralsorcery.constellation." + name, actual.getTranslationKey());
                if (i > 0) assertTrue(values.get(i - 1).getSortingId() < actual.getSortingId());
                var stars = new ArrayList<StarLocation>();
                for (var star : data.getAsJsonArray("stars")) {
                    var pair = star.getAsJsonArray();
                    stars.add(new StarLocation(pair.get(0).getAsInt(), pair.get(1).getAsInt()));
                }
                assertEquals(stars, actual.getStars(), name);
                var connections = data.getAsJsonArray("connections");
                assertEquals(connections.size(), actual.getStarConnections().size(), name);
                for (int j = 0; j < connections.size(); j++) {
                    var pair = connections.get(j).getAsJsonArray();
                    var edge = actual.getStarConnections().get(j);
                    assertEquals(stars.get(pair.get(0).getAsInt()), edge.from, name);
                    assertEquals(stars.get(pair.get(1).getAsInt()), edge.to, name);
                }
                String kind = data.get("kind").getAsString();
                assertEquals(kind.equals("Major"), actual instanceof IMajorConstellation, name);
                assertEquals(kind.equals("Minor"), actual instanceof IMinorConstellation, name);
                assertEquals(kind.equals("WeakSpecial"), actual instanceof Constellation.WeakSpecial, name);
            }
        }
        RegistryConstellations.init();
        for (var value : values) assertSame(value, RegistriesAS.CONSTELLATIONS.getValue(value.getRegistryName()));
    }

    @Test
    void earlyGeometryInitializationDoesNotResolveItemOrBlockRegistries() throws Exception {
        URL location = RegistryConstellations.class.getProtectionDomain().getCodeSource().getLocation();
        List<String> denied = new ArrayList<>();
        String root = "hellfirepvp.astralsorcery.common.";
        try (var loader = new URLClassLoader(new URL[]{location}, getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(root + "lib.ItemsAS") || name.equals(root + "lib.BlocksAS")
                        || name.equals("net.minecraft.world.item.Items")
                        || name.equals("net.minecraft.world.level.block.Blocks")) {
                    denied.add(name);
                    throw new ClassNotFoundException("Early registry access denied: " + name);
                }
                if (name.startsWith(root + "registry.") || name.startsWith(root + "constellation.")
                        || name.equals(root + "lib.ConstellationsAS") || name.equals(root + "lib.RegistriesAS")) {
                    Class<?> type = findLoadedClass(name);
                    if (type == null) type = findClass(name);
                    if (resolve) resolveClass(type);
                    return type;
                }
                return super.loadClass(name, resolve);
            }
        }) {
            var entry = Class.forName(root + "registry.RegistryConstellations", true, loader);
            assertSame(loader, entry.getClassLoader());
            entry.getMethod("init").invoke(null);
            entry.getMethod("init").invoke(null);
            var holder = Class.forName(root + "lib.ConstellationsAS", true, loader);
            assertNotNull(holder.getField("discidia").get(null));
            assertNotNull(holder.getField("vorux").get(null));
            assertTrue(denied.isEmpty(), denied.toString());
        }
    }
}
