package hellfirepvp.astralsorcery.common.effect;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class EffectsArchitectureTest {

    private static final String API = "hellfirepvp.astralsorcery.common.effect.";
    private static final List<String> TYPES = List.of(
            "Effects", "EffectHandle", "EffectBuilder", "EffectTemplate", "EffectSource");

    @Test
    void commonFacadeLoadsAndResolvesItsSignaturesWithClientClassesDenied() throws Exception {
        var location = Effects.class.getProtectionDomain().getCodeSource().getLocation();
        List<String> attemptedClientLoads = new ArrayList<>();
        try (URLClassLoader loader = new URLClassLoader(new java.net.URL[]{location},
                Effects.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("hellfirepvp.astralsorcery.client.")
                        || name.startsWith("net.minecraft.client.")
                        || name.startsWith("com.mojang.blaze3d.")) {
                    attemptedClientLoads.add(name);
                    throw new ClassNotFoundException("Client classes denied: " + name);
                }
                if (TYPES.stream().anyMatch(type -> name.equals(API + type))) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) loaded = findClass(name);
                    if (resolve) resolveClass(loaded);
                    return loaded;
                }
                return super.loadClass(name, resolve);
            }
        }) {
            for (String name : TYPES) {
                Class<?> type = Class.forName(API + name, true, loader);
                assertSame(loader, type.getClassLoader());
                type.getGenericInterfaces();
                for (var method : type.getDeclaredMethods()) {
                    method.getGenericParameterTypes();
                    method.getGenericReturnType();
                }
                for (var parameter : type.getTypeParameters()) parameter.getBounds();
                assertNoClientSymbols(type);
            }
            assertTrue(attemptedClientLoads.isEmpty(), attemptedClientLoads.toString());
        }
    }

    @Test
    void commonCodeCannotBypassFacadeThroughClientHelper() throws Exception {
        Path source = Path.of("src/main/java/hellfirepvp/astralsorcery/common");
        assertTrue(Files.isDirectory(source));
        Pattern helper = Pattern.compile("\\bEffectHelper\\b");
        try (var paths = Files.walk(source)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                assertFalse(helper.matcher(Files.readString(path, StandardCharsets.UTF_8)).find(),
                        () -> "Use the common effects facade: " + path);
            }
        }
        Path classes = Path.of(Effects.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve("hellfirepvp/astralsorcery/common");
        try (var paths = Files.walk(classes)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".class")).toList()) {
                String bytes = new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1);
                assertFalse(bytes.contains("hellfirepvp/astralsorcery/client/effect/handler/EffectHelper"),
                        () -> "Direct helper dependency in compiled common class: " + path);
            }
        }
    }

    private static void assertNoClientSymbols(Class<?> type) throws IOException {
        try (var resource = type.getResourceAsStream("/" + type.getName().replace('.', '/') + ".class")) {
            assertNotNull(resource);
            String bytes = new String(resource.readAllBytes(), StandardCharsets.ISO_8859_1);
            for (String forbidden : List.of("hellfirepvp/astralsorcery/client/",
                    "net/minecraft/client/", "com/mojang/blaze3d/")) {
                assertFalse(bytes.contains(forbidden), () -> type.getName() + " depends on " + forbidden);
            }
        }
    }
}
