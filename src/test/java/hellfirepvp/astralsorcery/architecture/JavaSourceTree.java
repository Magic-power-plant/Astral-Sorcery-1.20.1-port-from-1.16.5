package hellfirepvp.astralsorcery.architecture;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Parses Java syntax without loading Minecraft, resolving dependencies or running processors. */
public final class JavaSourceTree {
    public static final Path ROOT = Path.of("src/main/java");
    public record Source(Path path, String text, CompilationUnitTree tree) {
        public List<ClassTree> types() {
            return tree.getTypeDecls().stream().filter(ClassTree.class::isInstance)
                    .map(ClassTree.class::cast).toList();
        }

        public MethodTree method(String name) {
            return types().stream().flatMap(type -> type.getMembers().stream())
                    .filter(MethodTree.class::isInstance).map(MethodTree.class::cast)
                    .filter(method -> method.getName().contentEquals(name)).findFirst().orElseThrow();
        }
    }

    private JavaSourceTree() {}

    public static List<Source> production() throws IOException {
        if (!Files.isDirectory(ROOT)) throw new IOException("Missing production source root: " + ROOT);
        try (var files = Files.walk(ROOT)) {
            return read(files.filter(path -> path.toString().endsWith(".java")).sorted().toList());
        }
    }

    public static List<Source> read(List<Path> paths) throws IOException {
        var inputs = new ArrayList<JavaFileObject>();
        for (Path path : paths) inputs.add(input(path.toUri(), Files.readString(path, StandardCharsets.UTF_8)));
        return parse(inputs);
    }

    public static Source sample(String text) throws IOException {
        return parse(List.of(input(URI.create("file:///ArchitectureSample.java"), text))).get(0);
    }

    private static JavaFileObject input(URI uri, String text) {
        return new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return text; }
        };
    }

    private static List<Source> parse(List<JavaFileObject> inputs) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Architecture tests require the Java 17 JDK");
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (var manager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            var task = (JavacTask) compiler.getTask(null, manager, diagnostics,
                    List.of("-proc:none", "--release", "17"), null, inputs);
            var sources = new ArrayList<Source>();
            for (var unit : task.parse()) {
                sources.add(new Source(Path.of(unit.getSourceFile().toUri()),
                        unit.getSourceFile().getCharContent(false).toString(), unit));
            }
            var errors = diagnostics.getDiagnostics().stream()
                    .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR).toList();
            if (!errors.isEmpty()) throw new IOException("Invalid Java source: " + errors);
            return List.copyOf(sources);
        }
    }
}
