package hellfirepvp.astralsorcery.architecture;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/** No new common-to-client edges; cleaned packages cannot opt back in through the baseline. */
class ClientImportsArchitectureTest {
    private static final String COMMON = "hellfirepvp.astralsorcery.common";
    private static final Path BASELINE = Path.of("src/test/resources/architecture/client-imports.baseline");
    private static final List<String> CLIENT = List.of("hellfirepvp.astralsorcery.client",
            "net.minecraft.client", "net.minecraftforge.client", "com.mojang.blaze3d", "org.lwjgl");
    private static List<JavaSourceTree.Source> sources;

    @BeforeAll
    static void scanCommonSources() throws Exception {
        sources = JavaSourceTree.production().stream().filter(source -> {
            String name = source.tree().getPackageName().toString();
            return name.equals(COMMON) || name.startsWith(COMMON + ".");
        }).toList();
        assertFalse(sources.isEmpty(), "Common source scan must not pass vacuously");
    }

    @Test
    void commonRootRegistryAssemblyAndPerkModelsHaveNoClientReferences() {
        Set<String> violations = new TreeSet<>();
        for (var source : sources) {
            if (strict(source)) violations.addAll(entries(source));
        }
        assertEquals(Set.of(), violations, "Cleaned packages may not add baseline exceptions");
    }

    @Test
    void remainingCommonClientReferencesMatchTheReviewedBaselineExactly() throws Exception {
        Set<String> actual = new TreeSet<>();
        for (var source : sources) actual.addAll(entries(source));
        assertTrue(Files.isRegularFile(BASELINE), "Missing reviewed baseline: " + BASELINE);
        List<String> lines = Files.readAllLines(BASELINE, StandardCharsets.UTF_8).stream()
                .map(String::strip).filter(line -> !line.isEmpty() && !line.startsWith("#")).toList();
        Set<String> expected = new TreeSet<>(lines);
        assertEquals(expected.size(), lines.size(), "Duplicate baseline entries");
        Set<String> added = new TreeSet<>(actual);
        added.removeAll(expected);
        Set<String> removed = new TreeSet<>(expected);
        removed.removeAll(actual);
        if (!added.isEmpty() || !removed.isEmpty()) {
            Path report = Path.of("build/reports/architecture/client-imports.actual");
            Files.createDirectories(report.getParent());
            Files.write(report, actual, StandardCharsets.UTF_8);
        }
        assertTrue(added.isEmpty() && removed.isEmpty(), () ->
                "New client dependencies (move to client): " + added +
                "\nStale baseline entries (delete after cleanup): " + removed +
                "\nActual edges: build/reports/architecture/client-imports.actual");
    }

    @Test
    void syntaxScanCoversStaticWildcardAndFullyQualifiedReferencesButIgnoresCommentsAndStrings() throws Exception {
        var source = JavaSourceTree.sample("""
                package hellfirepvp.astralsorcery.common.example;
                import net.minecraft.client.Minecraft;
                import hellfirepvp.astralsorcery.client.screen.*;
                import static com.mojang.blaze3d.systems.RenderSystem.enableBlend;
                class Example {
                    // net.minecraft.client.Comment is not a dependency.
                    String text = "net.minecraft.client.StringLiteral";
                    net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event;
                    void draw() { org.lwjgl.opengl.GL11.glEnable(0); }
                }
                """);
        assertEquals(Set.of("import net.minecraft.client.Minecraft",
                "import hellfirepvp.astralsorcery.client.screen.*",
                "import static com.mojang.blaze3d.systems.RenderSystem.enableBlend",
                "reference net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers",
                "reference org.lwjgl.opengl.GL11.glEnable"), references(source));
        assertTrue(references(JavaSourceTree.sample("""
                package hellfirepvp.astralsorcery.common;
                import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
                import net.minecraft.clientish.Example;
                class Clean { String text = "hellfirepvp.astralsorcery.client.Missing"; }
                """)).isEmpty());
    }

    @Test
    void strictBoundaryIncludesNestedRegistryPackagesButNotAllLegacyCommonPackages() throws Exception {
        for (String pkg : List.of(COMMON, COMMON + ".registry", COMMON + ".registry.research",
                COMMON + ".perk.tree", COMMON + ".perk.tree.extension")) {
            assertTrue(strict(JavaSourceTree.sample("package " + pkg + "; class Example {}")), pkg);
        }
        assertFalse(strict(JavaSourceTree.sample("package " + COMMON + ".item; class Example {}")));
        assertFalse(strict(JavaSourceTree.sample("package " + COMMON + ".registryextra; class Example {}")));
    }

    private static boolean strict(JavaSourceTree.Source source) {
        String pkg = source.tree().getPackageName().toString();
        String type = pkg + "." + source.path().getFileName().toString().replace(".java", "");
        return pkg.equals(COMMON) || inPackage(pkg, COMMON + ".registry")
                || inPackage(pkg, COMMON + ".perk.tree")
                || type.equals(COMMON + ".perk.AllocationStatus")
                || type.equals(COMMON + ".data.research.ResearchNode")
                || type.equals(COMMON + ".network.play.server.PktOpenGui");
    }

    private static boolean inPackage(String name, String pkg) {
        return name.equals(pkg) || name.startsWith(pkg + ".");
    }

    private static boolean clientName(String name) {
        return CLIENT.stream().anyMatch(prefix -> inPackage(name, prefix));
    }

    private static Set<String> entries(JavaSourceTree.Source source) {
        String path = JavaSourceTree.ROOT.toAbsolutePath().normalize().relativize(source.path())
                .toString().replace('\\', '/');
        Set<String> result = new TreeSet<>();
        references(source).forEach(reference -> result.add(path + " -> " + reference));
        return result;
    }

    private static Set<String> references(JavaSourceTree.Source source) {
        Set<String> references = new TreeSet<>();
        new TreeScanner<Void, Void>() {
            @Override public Void visitImport(ImportTree tree, Void unused) {
                String name = tree.getQualifiedIdentifier().toString();
                if (clientName(name)) references.add("import " + (tree.isStatic() ? "static " : "") + name);
                return null;
            }

            @Override public Void visitMemberSelect(MemberSelectTree tree, Void unused) {
                String name = qualifiedName(tree);
                if (name != null && clientName(name)) {
                    references.add("reference " + name);
                    return null;
                }
                return super.visitMemberSelect(tree, unused);
            }
        }.scan(source.tree(), null);
        return references;
    }

    private static String qualifiedName(Tree tree) {
        if (tree instanceof IdentifierTree identifier) return identifier.getName().toString();
        if (tree instanceof MemberSelectTree select) {
            String prefix = qualifiedName(select.getExpression());
            return prefix == null ? null : prefix + "." + select.getIdentifier();
        }
        return null;
    }
}
