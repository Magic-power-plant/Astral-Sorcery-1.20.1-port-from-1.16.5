package hellfirepvp.astralsorcery.common.effect.mob;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MobEffectArchitectureTest {
    private static final String ROOT = "hellfirepvp.astralsorcery.common.effect.mob.";
    private static final List<String> TYPES = List.of("EffectCustomTexture", "EffectBleed",
            "EffectCheatDeath", "EffectDropModifier");
    private static final List<String> CLIENT = List.of("hellfirepvp.astralsorcery.client.",
            "net.minecraft.client.", "net.minecraftforge.client.", "com.mojang.blaze3d.", "org.lwjgl.");

    @Test
    void gameplaySignaturesResolveWithoutClientClassesAfterRemovingTheClientHook() throws Exception {
        List<String> denied = new ArrayList<>();
        ClassLoader loader = new ClassLoader(getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (CLIENT.stream().anyMatch(name::startsWith)) {
                    denied.add(name);
                    throw new ClassNotFoundException("Client class denied: " + name);
                }
                if (name.startsWith(ROOT)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) {
                        try {
                            byte[] bytes = serverDefinition(name);
                            loaded = defineClass(name, bytes, 0, bytes.length);
                        } catch (IOException exception) {
                            throw new ClassNotFoundException(name, exception);
                        }
                    }
                    if (resolve) resolveClass(loaded);
                    return loaded;
                }
                return super.loadClass(name, resolve);
            }
        };
        for (String name : TYPES) {
            // No FML boot here: inspect the server-shaped classes without initializing Minecraft.
            Class<?> type = Class.forName(ROOT + name, false, loader);
            assertSame(loader, type.getClassLoader());
            type.getDeclaredConstructors();
            type.getDeclaredFields();
            for (var method : type.getDeclaredMethods()) {
                method.getGenericParameterTypes();
                method.getGenericReturnType();
            }
        }
        assertTrue(denied.isEmpty(), denied.toString());
    }

    @Test
    void clientBridgeIsTheOnlyRenderingDependencyInGameplayEffects() throws Exception {
        for (String name : TYPES) {
            String bytes = new String(serverDefinition(ROOT + name), StandardCharsets.ISO_8859_1);
            for (String forbidden : CLIENT) {
                assertFalse(bytes.contains(forbidden.replace('.', '/')), name + " depends on " + forbidden);
            }
        }
    }

    @Test
    void infusionUsesOnlyTheCanonicalLiveState() throws Exception {
        String root = "hellfirepvp/astralsorcery/";
        assertNull(getClass().getClassLoader().getResource(root
                + "common/crafting/recipe/infusion/ActiveLiquidInfusionRecipe.class"));
        ClassNode infuser = readClass(root + "common/blockentity/infusion/InfuserBlockEntity.class");
        assertEquals("L" + root + "common/crafting/ActiveLiquidInfusionRecipe;",
                infuser.fields.stream().filter(field -> field.name.equals("activeRecipe")).findFirst().orElseThrow().desc);
        try (var sources = Files.walk(Path.of("src/main/java"))) {
            assertEquals(1, sources.filter(path -> path.getFileName().toString()
                    .equals("ActiveLiquidInfusionRecipe.java")).count());
        }
    }

    private byte[] serverDefinition(String name) throws IOException {
        ClassNode node = readClass(name.replace('.', '/') + ".class");
        if (name.equals(ROOT + "EffectCustomTexture")) {
            var hooks = node.methods.stream().filter(method -> method.name.equals("initializeClient")).toList();
            assertEquals(1, hooks.size());
            var hook = hooks.get(0);
            assertNotNull(hook.visibleAnnotations);
            var onlyIn = hook.visibleAnnotations.stream().filter(annotation -> annotation.desc
                    .equals("Lnet/minecraftforge/api/distmarker/OnlyIn;")).findFirst().orElseThrow();
            assertArrayEquals(new String[]{"Lnet/minecraftforge/api/distmarker/Dist;", "CLIENT"},
                    (String[]) onlyIn.values.get(1));
            node.methods.remove(hook);
        }
        // Rebuild the constant pool so references from the stripped method do not remain in the scan.
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private ClassNode readClass(String resource) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream, resource);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, 0);
            return node;
        }
    }
}
