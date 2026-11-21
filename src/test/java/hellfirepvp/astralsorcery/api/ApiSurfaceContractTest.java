package hellfirepvp.astralsorcery.api;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/** Reads class files without initializing Minecraft or loading physical-client classes. */
class ApiSurfaceContractTest {
    private static final String ROOT = "hellfirepvp.astralsorcery.";
    private static final List<String> TYPES = List.of(
            "api.AstralSorceryApi", "api.event.RegisterAstralSorceryExtensionsEvent",
            "api.crafting.IAltarRecipeEffectRenderer",
            "api.constellation.ConstellationItem", "api.constellation.IApplicableItemEffect", "api.constellation.IEngravingEffect",
            "api.crystal.CrystalAttributeItem", "api.crystal.CrystalAttributeGenItem",
            "api.effect.Effects", "api.effect.EffectSpec", "api.effect.EffectSpecBackend",
            "api.perk.IPerkAttributeType", "api.perk.IPerkAttributeModifier", "api.perk.IPerkAttributeReader",
            "api.perk.IPerkConverter", "api.perk.IPlayerPerkData", "api.perk.source.ModifierSource",
            "api.perk.source.ModifierSourceProvider", "api.progression.IPlayerProgression",
            "api.structure.IStructureType", "api.world.IWorldRecipe", "api.world.IWorldRecipeRegistry",
            "api.sync.SyncTarget", "api.sync.SyncDataWriter", "api.sync.SyncDataReader", "api.sync.SyncDataProvider",
            "common.crystal.CrystalAttributeItem", "common.crystal.CrystalAttributeGenItem",
            "common.constellation.ConstellationItem",
            "common.starlight.IStarlightTransmission", "common.starlight.IStarlightSource",
            "common.starlight.IStarlightReceiver", "common.starlight.IIndependentStarlightSource",
            "common.starlight.transmission.IPrismTransmissionNode", "common.starlight.transmission.ITransmissionNode",
            "common.starlight.transmission.ITransmissionSource", "common.starlight.transmission.ITransmissionReceiver",
            "common.starlight.transmission.registry.TransmissionProvider",
            "common.starlight.transmission.registry.SourceClassRegistry$SourceProvider",
            "common.data.sync.base.AbstractData", "common.data.sync.base.ClientData",
            "common.data.sync.base.AbstractDataProvider", "common.data.sync.base.ClientDataReader",
            "common.data.sync.SyncDataHolder");
    private static final Set<String> HOLDER_METHODS = Set.of(
            "executeServer", "computeServer", "executeClient", "computeClient", "register", "getReader");
    private static final int ACCESS = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC
            | Opcodes.ACC_FINAL | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE | Opcodes.ACC_VARARGS;

    @Test void preservesPublishedDescriptorsGenericSignaturesAndRequiredMethods() throws IOException {
        Set<String> expected;
        try (InputStream input = getClass().getResourceAsStream("/api/api-1.0.surface")) {
            assertNotNull(input, "The reviewed API 1.0 baseline must be checked in");
            expected = new TreeSet<>(new String(input.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(line -> !line.isBlank() && !line.startsWith("#")).toList());
        }
        Set<String> actual = surface();
        Set<String> removed = new TreeSet<>(expected);
        removed.removeAll(actual);
        assertTrue(removed.isEmpty(), () -> "Incompatible API changes: " + removed);
        Set<String> additions = new TreeSet<>(actual);
        additions.removeAll(expected);
        Set<String> publishedOwners = expected.stream().filter(line -> line.startsWith("TYPE "))
                .map(line -> line.split(" ", 3)[1]).collect(java.util.stream.Collectors.toSet());
        assertTrue(additions.stream().filter(line -> line.startsWith("ABSTRACT_METHOD "))
                        .noneMatch(line -> publishedOwners.contains(line.substring("ABSTRACT_METHOD ".length()).split("#", 2)[0])),
                () -> "Existing implementers must not gain required methods: " + additions);
        assertTrue(actual.stream().filter(line -> !line.contains(
                        "hellfirepvp.astralsorcery.api.crafting.IAltarRecipeEffectRenderer"))
                        .noneMatch(line -> line.contains("net/minecraft/client/")
                                || line.contains("hellfirepvp/astralsorcery/client/")),
                "Published signatures must be common-side safe");
    }

    private static Set<String> surface() throws IOException {
        Set<String> result = new TreeSet<>();
        for (String name : TYPES) {
            String owner = ROOT + name;
            try (InputStream input = ApiSurfaceContractTest.class.getClassLoader()
                    .getResourceAsStream(owner.replace('.', '/') + ".class")) {
                if (input == null) throw new IOException("Missing API type: " + owner);
                new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override public void visit(int version, int access, String name, String signature,
                                                String superName, String[] interfaces) {
                        result.add("TYPE " + owner + " " + (access & ACCESS) + " " + text(signature));
                        result.add("PARENTS " + owner + " " + superName + " " + (interfaces.length == 0 ? "-" : String.join(",", interfaces)));
                    }
                    @Override public FieldVisitor visitField(int access, String name, String descriptor,
                                                             String signature, Object value) {
                        if (visible(access) && !owner.endsWith(".SyncDataHolder")) {
                            result.add("FIELD " + owner + "#" + name + " " + (access & ACCESS) + " "
                                    + descriptor + " " + text(signature) + " " + (owner.endsWith(".AstralSorceryApi") ? "versioned" : String.valueOf(value)));
                        }
                        return null;
                    }
                    @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                               String signature, String[] exceptions) {
                        if (visible(access) && (!owner.endsWith(".SyncDataHolder") || HOLDER_METHODS.contains(name))) {
                            String prefix = (access & Opcodes.ACC_ABSTRACT) != 0 ? "ABSTRACT_METHOD " : "METHOD ";
                            result.add(prefix + owner + "#" + name + " " + (access & ACCESS) + " "
                                    + descriptor + " " + text(signature) + " "
                                    + (exceptions == null || exceptions.length == 0 ? "-" : String.join(",", exceptions)));
                        }
                        return null;
                    }
                }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
        }
        return result;
    }

    private static boolean visible(int access) {
        return (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0;
    }

    private static String text(String value) { return value == null ? "-" : value; }

    /** Explicit review aid only; ordinary tests never rewrite the baseline. */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Expected a candidate output path");
        Files.writeString(Path.of(args[0]), "# API 1.0 candidate: review compatibility before accepting.\n"
                + String.join("\n", surface()) + "\n", StandardCharsets.UTF_8);
    }
}
