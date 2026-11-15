package hellfirepvp.astralsorcery.client.registry;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClientRendererBindingsTest {
    private static final String ROOT = "hellfirepvp/astralsorcery/";
    private static final String BINDINGS = ROOT + "client/registry/RegistryBlockEntityTypes";

    @Test
    void allFifteenRendererBindingsRetainTheirTypeAndProvider() throws Exception {
        ClassNode node = readClass(BINDINGS);
        assertTrue(node.visibleAnnotations.stream().anyMatch(annotation -> annotation.desc
                .equals("Lnet/minecraftforge/api/distmarker/OnlyIn;")));
        var method = node.methods.stream().filter(m -> m.name.equals("initClient")).findFirst().orElseThrow();
        Map<String, String> actual = new LinkedHashMap<>();
        String type = null;
        String renderer = null;
        for (var instruction : method.instructions) {
            if (instruction instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC
                    && field.owner.equals(ROOT + "common/lib/BlockEntityTypesAS")) {
                type = field.name;
            } else if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                for (Object argument : dynamic.bsmArgs) {
                    if (argument instanceof Handle handle && handle.getName().equals("<init>")) {
                        renderer = handle.getOwner().substring((ROOT + "client/render/blockentity/").length());
                    }
                }
            } else if (instruction instanceof MethodInsnNode call
                    && call.name.equals("registerBlockEntityRenderer")) {
                assertNotNull(type);
                assertNotNull(renderer);
                assertNull(actual.put(type, renderer), "Duplicate binding for " + type);
                type = renderer = null;
            }
        }
        assertEquals(Map.ofEntries(
                Map.entry("ALTAR", "RenderAltar"),
                Map.entry("ATTUNEMENT_ALTAR", "RenderAttunementAltar"),
                Map.entry("CHALICE", "RenderChalice"),
                Map.entry("COLLECTOR_CRYSTAL", "RenderCollectorCrystal"),
                Map.entry("INFUSER", "RenderInfuser"),
                Map.entry("LENS", "RenderLens"),
                Map.entry("OBSERVATORY", "RenderObservatory"),
                Map.entry("PRISM", "RenderPrism"),
                Map.entry("REFRACTION_TABLE", "RenderRefractionTable"),
                Map.entry("RITUAL_PEDESTAL", "RenderRitualPedestal"),
                Map.entry("SPECTRAL_RELAY", "RenderSpectralRelay"),
                Map.entry("TELESCOPE", "RenderTelescope"),
                Map.entry("TRANSLUCENT_BLOCK", "RenderFakedState"),
                Map.entry("TREE_BEACON_COMPONENT", "RenderFakedState"),
                Map.entry("WELL", "RenderWell")), actual);
    }

    @Test
    void bindingListenerIsAttachedOnceFromTheClientLifecycle() throws Exception {
        var lifecycle = readClass(ROOT + "client/ClientProxy").methods.stream()
                .filter(method -> method.name.equals("attachLifecycle")).findFirst().orElseThrow();
        int listeners = 0;
        for (var instruction : lifecycle.instructions) {
            if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                for (Object argument : dynamic.bsmArgs) {
                    if (argument instanceof Handle handle && handle.getOwner().equals(BINDINGS)) {
                        assertEquals("initClient", handle.getName());
                        listeners++;
                    }
                }
            }
        }
        assertEquals(1, listeners);
        assertNull(getClass().getClassLoader().getResource(ROOT + "common/registry/RegistryBlockEntityTypes.class"));
        Path common = Path.of(getClass().getClassLoader().getResource(ROOT + "common/registry/RegistryBootstrap.class").toURI())
                .getParent().getParent();
        try (var classes = Files.walk(common)) {
            for (Path path : classes.filter(p -> p.toString().endsWith(".class")).toList()) {
                assertFalse(new String(Files.readAllBytes(path), StandardCharsets.ISO_8859_1).contains(BINDINGS),
                        "Common class depends on renderer bindings: " + path);
            }
        }
    }

    private ClassNode readClass(String name) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(stream, name);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, 0);
            return node;
        }
    }
}
