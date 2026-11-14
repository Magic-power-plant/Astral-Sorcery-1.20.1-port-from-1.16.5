package hellfirepvp.astralsorcery.common.patreon;

import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectType;
import org.junit.jupiter.api.Test;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

class PatreonArchitectureTest {
    private static final String ROOT = "hellfirepvp.astralsorcery.";
    private static final List<String> FORBIDDEN = List.of(ROOT + "client.", "net.minecraft.client.",
            "net.minecraftforge.client.", "com.mojang.blaze3d.", "org.lwjgl.");

    @Test void sharedPatreonFactoriesAndStateLoadWithClientClassesDenied() throws Exception {
        URL location = PatreonEffectType.class.getProtectionDomain().getCodeSource().getLocation();
        List<String> denied = new ArrayList<>();
        try (URLClassLoader loader = new URLClassLoader(new URL[]{location}, getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (FORBIDDEN.stream().anyMatch(name::startsWith)) {
                    denied.add(name);
                    throw new ClassNotFoundException("Client type denied: " + name);
                }
                if (name.startsWith(ROOT + "common.patreon.")
                        || name.startsWith(ROOT + "common.util.data.Vector3")
                        || name.equals(ROOT + "common.util.nbt.NBTHelper")) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) loaded = findClass(name);
                    if (resolve) resolveClass(loaded);
                    return loaded;
                }
                return super.loadClass(name, resolve);
            }
        }) {
            for (String name : List.of("effect.FlareColor", "effect.PatreonEffect", "effect.PatreonEffectDefinition",
                    "effect.PatreonEffectType", "effect.PatreonEffectProvider", "effect.PatreonEffectHelper",
                    "entity.PatreonPartialEntity", "entity.PatreonFlare", "data.PatreonData", "data.PatreonDataManager")) {
                Class<?> type = Class.forName(ROOT + "common.patreon." + name, true, loader);
                assertSame(loader, type.getClassLoader());
                type.getDeclaredConstructors();
                type.getDeclaredFields();
                type.getDeclaredMethods();
            }
            Class<?> definition = loader.loadClass(ROOT + "common.patreon.effect.PatreonEffectDefinition");
            Class<?> types = loader.loadClass(ROOT + "common.patreon.effect.PatreonEffectType");
            Object dynamic = Arrays.stream(types.getEnumConstants()).filter(t -> t.toString().equals("FLARE_DYNAMIC_COLOR")).findFirst().orElseThrow();
            UUID owner = UUID.randomUUID();
            Object effect = definition.getConstructor(types, UUID.class, List.class)
                    .newInstance(dynamic, owner, List.of(UUID.randomUUID().toString()));
            Object entity = definition.getMethod("createEntity", UUID.class).invoke(effect, owner);
            assertNotNull(entity);
            entity.getClass().getMethod("writeToNBT", net.minecraft.nbt.CompoundTag.class)
                    .invoke(entity, new net.minecraft.nbt.CompoundTag());
            assertTrue(denied.isEmpty(), denied.toString());
        }
    }

    @Test void sharedUtilityAndPatreonBytecodeHaveNoRenderingReferences() throws Exception {
        Path classes = Path.of(PatreonEffectType.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve("hellfirepvp/astralsorcery");
        for (String directory : List.of("common/patreon", "common/util", "common/blockentity/capability",
                "common/blockentity/inventory", "common/blockentity/fluid")) {
            try (var files = Files.walk(classes.resolve(directory))) {
                for (Path file : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                    String bytecode = new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1);
                    for (String forbidden : FORBIDDEN) {
                        assertFalse(bytecode.contains(forbidden.replace('.', '/')), file + " depends on " + forbidden);
                    }
                }
            }
        }
    }

    @Test void packageDeclarationsAndPackageDocumentationMatchSourceLayout() throws Exception {
        Path root = Path.of("src/main/java");
        Map<Path, List<Path>> packages = new HashMap<>();
        Pattern declaration = Pattern.compile("(?m)^package\\s+([\\w.]+)\\s*;");
        try (var files = Files.walk(root)) {
            for (Path source : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String text = Files.readString(source, StandardCharsets.UTF_8);
                var matcher = declaration.matcher(text);
                assertTrue(matcher.find(), source.toString());
                assertEquals(root.relativize(source.getParent()).toString().replace('\\', '.'), matcher.group(1), source.toString());
                packages.computeIfAbsent(source.getParent(), key -> new ArrayList<>()).add(source);
            }
        }
        for (var entry : packages.entrySet()) {
            assertTrue(entry.getValue().stream().anyMatch(p -> p.getFileName().toString().equals("package-info.java")),
                    "Missing package documentation: " + entry.getKey());
            assertTrue(entry.getValue().size() > 1, "Orphan package documentation: " + entry.getKey());
        }
    }
}
