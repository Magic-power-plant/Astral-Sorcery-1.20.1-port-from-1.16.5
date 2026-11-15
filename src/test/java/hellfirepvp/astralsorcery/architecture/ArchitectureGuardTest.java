package hellfirepvp.astralsorcery.architecture;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Repository-wide structural rules; specialized loading/behavior tests remain with their domains. */
class ArchitectureGuardTest {
    private static final String ROOT = "hellfirepvp.astralsorcery";
    private static final Set<String> RETIRED = Set.of("InternalRegistryPrimer", "PrimerEventHandler",
            "RegistryEnchantments", "RegistryFluids", "RegistryGameRules", "RegistryMaterials",
            "RegistryRecipeSerializers", "RegistrySounds", "RegistryStructures", "ContainerTypesAS");
    private static List<JavaSourceTree.Source> sources;

    @BeforeAll
    static void parseProductionSources() throws Exception {
        sources = JavaSourceTree.production();
        assertFalse(sources.isEmpty(), "Production scan must not pass vacuously");
    }

    @Test
    void packageIndexCountsMatchTheProductionTree() throws Exception {
        Map<Path, List<JavaSourceTree.Source>> packages = new LinkedHashMap<>();
        for (var source : sources) packages.computeIfAbsent(source.path().getParent(), ignored -> new ArrayList<>()).add(source);
        long businessFiles = sources.stream().filter(source ->
                !source.path().getFileName().toString().equals("package-info.java") && !source.types().isEmpty()).count();
        String documentation = Files.readString(Path.of("docs/packages.md"));
        var counts = Pattern.compile("有 \\*\\*(\\d+) 个实际声明包\\*\\*、([\\d,]+) 个业务 Java 源文件").matcher(documentation);
        assertTrue(counts.find(), "packages.md must publish package and source-file counts");
        assertEquals(packages.size(), Integer.parseInt(counts.group(1)), "packages.md package count is stale");
        assertEquals(businessFiles, Long.parseLong(counts.group(2).replace(",", "")),
                "packages.md business Java source count is stale");
    }

    @Test
    void packageIndexSectionCountsMatchTheDeclaredPackages() throws Exception {
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("入口与扩展 API", 0);
        expected.put("客户端", 0);
        expected.put("公共实现", 0);
        expected.put("Mixin", 0);
        Set<String> seenPackages = new HashSet<>();
        for (var source : sources) {
            if (source.path().getFileName().toString().equals("package-info.java") || source.types().isEmpty()) continue;
            String pkg = source.tree().getPackageName().toString();
            if (!seenPackages.add(pkg)) continue;
            String section = pkg.equals(ROOT) || pkg.equals(ROOT + ".api") || pkg.startsWith(ROOT + ".api.") ? "入口与扩展 API"
                    : pkg.equals(ROOT + ".client") || pkg.startsWith(ROOT + ".client.") ? "客户端"
                    : pkg.equals(ROOT + ".common") || pkg.startsWith(ROOT + ".common.") ? "公共实现"
                    : pkg.equals(ROOT + ".mixin") || pkg.startsWith(ROOT + ".mixin.") ? "Mixin" : null;
            assertNotNull(section, "Uncategorized package: " + pkg);
            expected.compute(section, (ignored, count) -> count + 1);
        }
        String documentation = Files.readString(Path.of("docs/packages.md"));
        for (var entry : expected.entrySet()) {
            var section = Pattern.compile("(?m)^### " + Pattern.quote(entry.getKey()) + "（(\\d+)）$")
                    .matcher(documentation);
            assertTrue(section.find(), "Missing package-index section: " + entry.getKey());
            int start = section.end();
            var next = Pattern.compile("(?m)^### ").matcher(documentation);
            int end = next.find(start) ? next.start() : documentation.length();
            int rows = (int) Pattern.compile("(?m)^\\| \\[`").matcher(documentation.substring(start, end)).results().count();
            assertEquals(entry.getValue(), Integer.parseInt(section.group(1)), entry.getKey() + " heading count is stale");
            assertEquals(entry.getValue(), rows, entry.getKey() + " package-index rows are stale");
        }
    }

    @Test
    void everyProductionPackageMatchesItsDirectoryAndHasExactlyOneNonOrphanDocument() {
        Map<Path, List<JavaSourceTree.Source>> packages = new LinkedHashMap<>();
        Path root = JavaSourceTree.ROOT.toAbsolutePath().normalize();
        for (var source : sources) {
            Path directory = source.path().getParent();
            assertNotNull(source.tree().getPackageName(), source.path().toString());
            String name = source.tree().getPackageName().toString();
            assertTrue(name.equals(ROOT) || name.startsWith(ROOT + "."), source.path().toString());
            assertEquals(root.relativize(directory).toString().replace('\\', '.').replace('/', '.'),
                    name, source.path().toString());
            packages.computeIfAbsent(directory, ignored -> new ArrayList<>()).add(source);
        }
        for (var entry : packages.entrySet()) {
            var docs = entry.getValue().stream().filter(source ->
                    source.path().getFileName().toString().equals("package-info.java")).toList();
            assertEquals(1, docs.size(), "Missing package documentation: " + entry.getKey());
            assertTrue(entry.getValue().stream().anyMatch(source -> !source.types().isEmpty()),
                    "Orphan package documentation: " + entry.getKey());
            assertTrue(docs.get(0).text().stripLeading().startsWith("/**"),
                    "Package documentation requires Javadoc: " + entry.getKey());
        }
    }

    @Test
    void packageDocumentationReferencesExistingProductionTypes() {
        Pattern reference = Pattern.compile("@see\\s+(hellfirepvp\\.astralsorcery[\\w.]+)");
        for (var source : sources) {
            if (!source.path().getFileName().toString().equals("package-info.java")) continue;
            var matcher = reference.matcher(source.text());
            while (matcher.find()) {
                Path target = JavaSourceTree.ROOT.resolve(matcher.group(1).replace('.', '/') + ".java");
                assertTrue(Files.isRegularFile(target), source.path() + " has a stale @see: " + matcher.group(1));
            }
        }
    }

    @Test
    void utilityPackageDependenciesRemainAcyclic() {
        String functionPackage = ROOT + ".common.util.function";
        for (var source : sources) {
            if (!source.tree().getPackageName().toString().equals(functionPackage)) continue;
            assertFalse(source.text().contains("hellfirepvp.astralsorcery.common.util.collection"),
                    "util.function must not depend on util.collection: " + source.path());
        }
    }

    @Test
    void blockImplementationsCannotBeEmptyOrJustForwardConstructorParameters() {
        var violations = new ArrayList<String>();
        for (var source : sources) {
            String name = source.tree().getPackageName().toString();
            if (!name.equals(ROOT + ".common.block") && !name.startsWith(ROOT + ".common.block.")) continue;
            new TreeScanner<Void, Void>() {
                @Override public Void visitClass(ClassTree type, Void unused) {
                    if (isForwardingShell(type)) violations.add(source.path() + ": " + type.getSimpleName());
                    return super.visitClass(type, unused);
                }
            }.scan(source.tree(), null);
        }
        assertTrue(violations.isEmpty(), "Use the existing block factory instead of a shell: " + violations);
    }

    @Test
    void shellDetectionUsesSyntaxAndKeepsConfiguredOrBehavioralSubclasses() throws Exception {
        for (String declaration : List.of(
                "class Empty extends Block {}",
                "final class Empty extends Block { /* behavior? */ public Empty(Properties p) { super(p); } }",
                "class Empty extends Template { Empty(Properties a, int b) { super(a, b); } }")) {
            assertTrue(isForwardingShell(JavaSourceTree.sample(declaration).types().get(0)), declaration);
        }
        for (String declaration : List.of(
                "abstract class Base extends Block { Base(Properties p) { super(p); } }",
                "class Configured extends Block { Configured() { super(Properties.of().strength(3)); } }",
                "class Crystal extends Block { Crystal() { super(Type.ROCK); } }",
                "class Stateful extends Block { int age; Stateful(Properties p) { super(p); } }",
                "class Behavior extends Block { void tick() {} }",
                "class Capability extends Block implements Marker {}")) {
            assertFalse(isForwardingShell(JavaSourceTree.sample(declaration).types().get(0)), declaration);
        }
    }

    @Test
    void retiredRegistryTypesCannotReturnAsDeclarationsOrDependencies() {
        var violations = new ArrayList<String>();
        for (var source : sources) {
            new TreeScanner<Void, Void>() {
                private void check(CharSequence name) {
                    if (RETIRED.contains(name.toString())) violations.add(source.path() + ": " + name);
                }
                @Override public Void visitClass(ClassTree node, Void unused) {
                    check(node.getSimpleName()); return super.visitClass(node, unused);
                }
                @Override public Void visitIdentifier(IdentifierTree node, Void unused) {
                    check(node.getName()); return super.visitIdentifier(node, unused);
                }
                @Override public Void visitMemberSelect(MemberSelectTree node, Void unused) {
                    check(node.getIdentifier()); return super.visitMemberSelect(node, unused);
                }
            }.scan(source.tree(), null);
        }
        assertTrue(violations.isEmpty(), "Use canonical registry holders/assembly: " + violations);
        assertFalse(Files.exists(JavaSourceTree.ROOT.resolve(
                "hellfirepvp/astralsorcery/common/registry/RegistryBlockEntityTypes.java")),
                "Renderer binding belongs in client.registry");
    }

    @Test
    void allRegistryAssemblyModulesStayBoundedAndTheTwoFacadesStaySmall() {
        for (var source : sources) {
            String pkg = source.tree().getPackageName().toString();
            if (!pkg.equals(ROOT + ".common.registry") && !pkg.startsWith(ROOT + ".common.registry.")) continue;
            String file = source.path().getFileName().toString();
            int limit = Set.of("RegistryConstellations.java", "RegistryResearch.java").contains(file) ? 60 : 300;
            long lines = source.text().lines().count();
            assertTrue(lines <= limit, source.path() + " has " + lines + " lines (limit " + limit +
                    "); split by domain responsibility, not by moving the monolith into a helper");
        }
    }

    private static boolean isForwardingShell(ClassTree type) {
        if (type.getKind() != Tree.Kind.CLASS || type.getExtendsClause() == null
                || type.getSimpleName().isEmpty() || !type.getImplementsClause().isEmpty()
                || type.getModifiers().getFlags().contains(Modifier.ABSTRACT)) return false;
        for (Tree member : type.getMembers()) {
            if (member.getKind() == Tree.Kind.EMPTY_STATEMENT) continue;
            if (!(member instanceof MethodTree constructor) || constructor.getReturnType() != null) return false;
            var statements = constructor.getBody().getStatements();
            if (statements.isEmpty() && constructor.getParameters().isEmpty()) continue;
            if (statements.size() != 1 || !(statements.get(0) instanceof ExpressionStatementTree statement)
                    || !(statement.getExpression() instanceof MethodInvocationTree call)
                    || !call.getMethodSelect().toString().equals("super")
                    || call.getArguments().size() != constructor.getParameters().size()) return false;
            for (int i = 0; i < call.getArguments().size(); i++) {
                if (!(call.getArguments().get(i) instanceof IdentifierTree argument)
                        || !argument.getName().contentEquals(constructor.getParameters().get(i).getName())) return false;
            }
        }
        return true;
    }
}
