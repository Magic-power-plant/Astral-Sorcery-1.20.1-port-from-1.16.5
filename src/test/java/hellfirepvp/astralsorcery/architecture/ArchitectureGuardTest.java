package hellfirepvp.astralsorcery.architecture;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
