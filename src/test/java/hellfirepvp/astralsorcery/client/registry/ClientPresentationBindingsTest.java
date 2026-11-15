package hellfirepvp.astralsorcery.client.registry;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Verify registrations without constructing Minecraft renderers or launching a GL context. */
class ClientPresentationBindingsTest {
    private static final String ROOT = "hellfirepvp/astralsorcery/";
    private static final String CLIENT = ROOT + "client/registry/";

    @Test
    void allSixMenusKeepTheirScreensIncludingTheObservatoryAdapter() throws Exception {
        assertEquals(Map.of("TOME", "ScreenContainerTome", "OBSERVATORY", "ScreenObservatory",
                "ALTAR_DISCOVERY", "ScreenContainerAltarDiscovery", "ALTAR_ATTUNEMENT", "ScreenContainerAltarAttunement",
                "ALTAR_CONSTELLATION", "ScreenContainerAltarConstellation", "ALTAR_RADIANCE", "ScreenContainerAltarRadiance"),
                bindings("RegistryMenuTypes", "MenuTypesAS", "register"));
        assertNull(getClass().getClassLoader().getResource(ROOT + "common/registry/RegistryMenuTypes.class"));
    }

    @Test
    void allTwelveEntityRenderersKeepTheirOriginalProviders() throws Exception {
        assertEquals(Map.ofEntries(
                Map.entry("NOCTURNAL_SPARK", "RenderEntityEmpty"), Map.entry("ILLUMINATION_SPARK", "RenderEntityEmpty"),
                Map.entry("FLARE", "RenderEntityEmpty"), Map.entry("SPECTRAL_TOOL", "RenderEntitySpectralTool"),
                Map.entry("ITEM_HIGHLIGHT", "RenderEntityItemHighlighted"),
                Map.entry("ITEM_EXPLOSION_RESISTANT", "RenderEntityItemHighlighted"),
                Map.entry("ITEM_CRYSTAL", "RenderEntityItemHighlighted"), Map.entry("ITEM_STARMETAL_INGOT", "ItemEntityRenderer"),
                Map.entry("OBSERVATORY_HELPER", "RenderEntityEmpty"), Map.entry("GRAPPLING_HOOK", "RenderEntityGrapplingHook"),
                Map.entry("SHOOTING_STAR", "RenderEntityEmpty"), Map.entry("ITEM_DAZZLING_GEM", "RenderEntityItemHighlighted")),
                bindings("RegistryEntityRenderers", "EntityTypesAS", "registerEntityRenderer"));
        var attributes = read(ROOT + "common/registry/RegistryEntityTypes");
        assertTrue(attributes.methods.stream().anyMatch(method -> method.name.equals("initAttributes")));
        assertFalse(attributes.methods.stream().anyMatch(method -> method.name.equals("initClient")));
    }

    @Test
    void colorsAndRenderersAreAttachedOnceAndSetupWorkIsEnqueued() throws Exception {
        ClassNode proxy = read(ROOT + "client/ClientProxy");
        var attach = method(proxy, "attachLifecycle");
        List<String> listeners = handles(attach).stream().map(h -> h.getOwner() + "#" + h.getName()).toList();
        for (String expected : List.of("RegistryItemModels#registerColors", "RegistryBlocks#registerColors",
                "RegistryEntityRenderers#initClient", "RegistryBlockEntityTypes#initClient")) {
            assertEquals(1, listeners.stream().filter((CLIENT + expected)::equals).count(), expected);
        }
        var setup = method(proxy, "onClientSetup");
        assertEquals(1, calls(setup).stream().filter(call -> call.name.equals("enqueueWork")).count());
        List<Handle> work = handles(setup);
        assertEquals(1, work.size());
        List<String> queued = calls(method(proxy, work.get(0).getName())).stream()
                .map(call -> call.owner + "#" + call.name).toList();
        assertEquals(List.of(CLIENT + "RegistryMenuTypes#initClient", CLIENT + "RegistryBlockRenderTypes#initBlocks",
                CLIENT + "RegistryBlockRenderTypes#initFluids", CLIENT + "RegistryItemModels#registerItemProperties"), queued);
    }

    @Test
    void itemPropertiesAndServerDispenseBehaviorStayOnTheirRespectiveSides() throws Exception {
        var models = read(CLIENT + "RegistryItemModels");
        List<String> properties = new ArrayList<>();
        List<String> items = new ArrayList<>();
        for (var instruction : method(models, "registerItemProperties").instructions) {
            if (instruction instanceof LdcInsnNode constant && constant.cst instanceof String name) properties.add(name);
            if (instruction instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC
                    && (field.owner.equals(ROOT + "common/lib/ItemsAS") || field.owner.equals(ROOT + "common/lib/BlocksAS"))) {
                items.add(field.name);
            }
        }
        assertEquals(List.of("engraved", "written", "upgrade", "stage", "stage"), properties);
        assertEquals(List.of("INFUSED_GLASS", "KNOWLEDGE_SHARE", "RESONATOR", "CELESTIAL_CRYSTAL_CLUSTER", "GEM_CRYSTAL_CLUSTER"), items);
        var common = read(ROOT + "common/registry/RegistryItems");
        assertTrue(common.methods.stream().anyMatch(m -> m.name.equals("registerDispenseBehaviors")));
        assertFalse(common.methods.stream().anyMatch(m -> m.name.equals("registerItemProperties") || m.name.equals("registerColors")));
        assertNull(getClass().getClassLoader().getResource(ROOT + "common/registry/RegistryBlocks.class"));
    }

    private Map<String, String> bindings(String owner, String holder, String register) throws Exception {
        ClassNode node = read(CLIENT + owner);
        assertTrue(node.visibleAnnotations.stream().anyMatch(a -> a.desc.equals("Lnet/minecraftforge/api/distmarker/OnlyIn;")));
        Map<String, String> result = new LinkedHashMap<>();
        String type = null, provider = null;
        for (var instruction : method(node, "initClient").instructions) {
            if (instruction instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC
                    && field.owner.equals(ROOT + "common/lib/" + holder)) type = field.name;
            if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                for (Object argument : dynamic.bsmArgs) {
                    if (!(argument instanceof Handle handle)) continue;
                    if (handle.getName().equals("<init>")) provider = simpleName(handle.getOwner());
                    else if (handle.getOwner().equals(CLIENT + owner)) {
                        var adapter = method(node, handle.getName());
                        var constructors = calls(adapter).stream().filter(call -> call.name.equals("<init>")).toList();
                        assertEquals(1, constructors.size());
                        provider = simpleName(constructors.get(0).owner);
                    }
                }
            }
            if (instruction instanceof MethodInsnNode call && call.name.equals(register)) {
                assertNotNull(type);
                assertNotNull(provider);
                assertNull(result.put(type, provider), "Duplicate binding: " + type);
                type = provider = null;
            }
        }
        return result;
    }

    private static String simpleName(String owner) { return owner.substring(owner.lastIndexOf('/') + 1); }
    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(m -> m.name.equals(name)).findFirst().orElseThrow();
    }
    private static List<MethodInsnNode> calls(MethodNode method) {
        List<MethodInsnNode> result = new ArrayList<>();
        for (var instruction : method.instructions) if (instruction instanceof MethodInsnNode call) result.add(call);
        return result;
    }
    private static List<Handle> handles(MethodNode method) {
        List<Handle> result = new ArrayList<>();
        for (var instruction : method.instructions) if (instruction instanceof InvokeDynamicInsnNode dynamic) {
            for (Object argument : dynamic.bsmArgs) if (argument instanceof Handle handle) result.add(handle);
        }
        return result;
    }
    private ClassNode read(String name) throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(stream, name);
            var node = new ClassNode();
            new ClassReader(stream).accept(node, 0);
            return node;
        }
    }
}
