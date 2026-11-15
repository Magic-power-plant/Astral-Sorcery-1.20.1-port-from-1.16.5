package hellfirepvp.astralsorcery.common.registry;

import com.google.gson.JsonParser;
import com.sun.source.tree.*;
import hellfirepvp.astralsorcery.architecture.JavaSourceTree;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Inspects journal declarations without bootstrapping Forge item registries or client renderers. */
class ResearchAssemblyTest {
    private static final Path REGISTRY = JavaSourceTree.ROOT.resolve("hellfirepvp/astralsorcery/common/registry");
    private static final Map<String, String> CHAPTERS = Map.of("Discovery", "DISCOVERY", "Crafting", "BASIC_CRAFT",
            "Attunement", "ATTUNEMENT", "Constellation", "CONSTELLATION", "Radiance", "RADIANCE");

    @Test
    void allChaptersKeepHistoricalNodeOrderLayoutPagesLookupsAndGraph() throws Exception {
        var resource = getClass().getResourceAsStream("/registry/research.json");
        assertNotNull(resource);
        var allKeys = new HashSet<String>();
        int count = 0;
        try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            var baseline = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals(CHAPTERS.keySet(), baseline.keySet());
            for (var chapter : CHAPTERS.entrySet()) {
                var actual = nodes(chapter.getKey(), chapter.getValue());
                var expected = baseline.getAsJsonArray(chapter.getKey());
                assertEquals(expected.size(), actual.size(), chapter.getKey());
                int i = 0;
                for (var node : actual.values()) {
                    var data = expected.get(i++).getAsJsonObject();
                    assertTrue(allKeys.add(node.key), "Duplicate research key: " + node.key);
                    assertEquals(data.get("key").getAsString(), node.key);
                    assertEquals(data.get("x").getAsDouble(), node.x, node.key);
                    assertEquals(data.get("y").getAsDouble(), node.y, node.key);
                    assertEquals(data.get("pages").getAsInt(), node.pages, node.key);
                    assertEquals(data.get("lookups").getAsInt(), node.lookups, node.key);
                    assertEquals(data.getAsJsonArray("lookupInputs").asList().stream().map(v -> v.getAsString()).toList(),
                            node.lookupInputs, node.key);
                    assertEquals(data.getAsJsonArray("texts").asList().stream().map(v -> v.getAsString()).toList(),
                            node.texts, node.key);
                    var sources = node.sources.stream().map(variable -> {
                        assertTrue(actual.containsKey(variable), "Unknown source of " + node.key + ": " + variable);
                        return actual.get(variable).key;
                    }).toList();
                    assertEquals(data.getAsJsonArray("sources").asList().stream().map(v -> v.getAsString()).toList(),
                            sources, node.key);
                    count++;
                }
                var positions = new HashSet<String>();
                for (var node : actual.values()) {
                    assertTrue(positions.add(node.x + ":" + node.y), "Overlapping nodes in " + chapter.getKey());
                    assertAcyclic(node, actual, new HashSet<>());
                }
            }
        }
        assertEquals(76, count);
    }

    @Test
    void chapterAndConstellationAssemblyOrderAndLateSignaturePhaseStayExplicit() throws Exception {
        assertEquals(List.of("DiscoveryResearch.registerEntries()", "CraftingResearch.registerEntries()",
                        "AttunementResearch.registerEntries()", "ConstellationResearch.registerEntries()",
                        "RadianceResearch.registerEntries()"), calls("research/BuiltinResearch.java", "registerEntries"));
        assertEquals(List.of("MajorConstellations.registerEntries()", "WeakConstellations.registerEntries()",
                        "MinorConstellations.registerEntries()"), calls("constellation/BuiltinConstellations.java", "registerEntries"));
        assertEquals(List.of("init()", "SIGNATURE_ITEMS.initialize()"),
                calls("RegistryConstellations.java", "initSignatureItems"));
        assertEquals(List.of("GEOMETRY.initialize()"), calls("RegistryConstellations.java", "init"));
        assertEquals(List.of("RESEARCH.initialize()"), calls("RegistryResearch.java", "init"));
    }

    @Test
    void signatureItemsRetainAllSixtyFourOrderedInputs() throws Exception {
        var actual = calls("constellation/ConstellationSignatureItems.java", "registerEntries");
        var resource = getClass().getResourceAsStream("/registry/signature-items.txt");
        assertNotNull(resource);
        try (resource) {
            var expected = new String(resource.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
            assertEquals(64, expected.size());
            assertEquals(expected, actual);
        }
    }

    private static Map<String, Node> nodes(String chapter, String progression) throws Exception {
        var body = source("research/" + chapter + "Research.java").method("registerEntries").getBody();
        Map<String, Node> nodes = new LinkedHashMap<>();
        for (var statement : body.getStatements()) {
            if (!(statement instanceof VariableTree variable) || !variable.getType().toString().equals("ResearchNode")) continue;
            ExpressionTree current = variable.getInitializer();
            var chain = new ArrayList<MethodInvocationTree>();
            while (current instanceof MethodInvocationTree call) {
                chain.add(call);
                current = ((MemberSelectTree) call.getMethodSelect()).getExpression();
            }
            var constructor = assertInstanceOf(NewClassTree.class, current);
            assertEquals("ResearchNode", constructor.getIdentifier().toString());
            var args = constructor.getArguments();
            var node = new Node((String) ((LiteralTree) args.get(1)).getValue(), number(args.get(2)), number(args.get(3)));
            var lookupPages = new ArrayList<Integer>();
            int registrations = 0;
            Collections.reverse(chain);
            for (var call : chain) {
                String operation = ((MemberSelectTree) call.getMethodSelect()).getIdentifier().toString();
                if (operation.equals("register")) {
                    assertEquals("ResearchProgression." + progression, call.getArguments().get(0).toString(), node.key);
                    registrations++;
                } else if (operation.equals("addPage")) {
                    node.pages++;
                    var page = assertInstanceOf(MethodInvocationTree.class, call.getArguments().get(0));
                    if (page.getMethodSelect().toString().equals("text")) {
                        node.texts.add((String) ((LiteralTree) page.getArguments().get(0)).getValue());
                    }
                } else if (operation.equals("addTomeLookup")) {
                    node.lookups++;
                    lookupPages.add(((Number) ((LiteralTree) call.getArguments().get(1)).getValue()).intValue());
                    // Lookup unlock tiers can intentionally be later than the node's own chapter (ATT_CRYSTAL).
                    node.lookupInputs.add(call.getArguments().get(0) + "|" + call.getArguments().get(1) + "|"
                            + ((MemberSelectTree) call.getArguments().get(2)).getIdentifier());
                }
            }
            assertEquals(1, registrations, node.key);
            assertTrue(node.pages > 0, node.key);
            for (int page : lookupPages) assertTrue(page >= 0 && page < node.pages, "Invalid tome page: " + node.key);
            nodes.put(variable.getName().toString(), node);
        }
        for (var statement : body.getStatements()) {
            if (!(statement instanceof ExpressionStatementTree expression)
                    || !(expression.getExpression() instanceof MethodInvocationTree call)
                    || !(call.getMethodSelect() instanceof MemberSelectTree method)
                    || !method.getIdentifier().contentEquals("addSourceConnectionFrom")) continue;
            var target = nodes.get(method.getExpression().toString());
            assertNotNull(target, "Unknown graph target: " + method);
            call.getArguments().forEach(source -> target.sources.add(source.toString()));
        }
        assertFalse(nodes.isEmpty(), chapter);
        return nodes;
    }

    private static void assertAcyclic(Node node, Map<String, Node> nodes, HashSet<String> path) {
        assertTrue(path.add(node.key), "Cycle at " + node.key);
        for (String source : node.sources) assertAcyclic(nodes.get(source), nodes, path);
        path.remove(node.key);
    }

    private static double number(ExpressionTree expression) {
        return Double.parseDouble(expression.toString().replace("F", "").replace("f", ""));
    }

    private static JavaSourceTree.Source source(String file) throws Exception {
        return JavaSourceTree.read(List.of(REGISTRY.resolve(file))).get(0);
    }

    private static List<String> calls(String file, String method) throws Exception {
        return source(file).method(method).getBody().getStatements().stream()
                .map(statement -> assertInstanceOf(ExpressionStatementTree.class, statement).getExpression().toString()).toList();
    }

    private static final class Node {
        final String key;
        final double x, y;
        int pages, lookups;
        final List<String> texts = new ArrayList<>();
        final List<String> sources = new ArrayList<>();
        final List<String> lookupInputs = new ArrayList<>();
        Node(String key, double x, double y) { this.key = key; this.x = x; this.y = y; }
    }
}
