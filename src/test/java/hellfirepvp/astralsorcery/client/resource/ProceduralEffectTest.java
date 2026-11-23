package hellfirepvp.astralsorcery.client.resource;

import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ProceduralEffectTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/astralsorcery");
    private static final Path REFERENCES = Path.of("src/test/resources/effect_sheets");

    @Test
    void packagedFragmentAndItsMaterialImportsRemainSamplerFree() throws IOException {
        var root = "/assets/astralsorcery/shaders/";
        for (String program : Arrays.stream(ProceduralEffect.values()).map(effect -> effect.program).distinct().toList()) {
            var fragment = shaderSource(root + "core/" + program + ".fsh");
            var imports = Pattern.compile("(?m)^#moj_import <astralsorcery:([a-z_0-9]+\\.glsl)>\\s*$").matcher(fragment);
            var source = new StringBuilder(fragment);
            while (imports.find()) {
                source.append('\n').append(shaderSource(root + "include/" + imports.group(1)));
            }
            assertFalse(Pattern.compile("\\bsampler\\w*\\b|\\b(?:texture\\w*|texelFetch)\\s*\\(").matcher(source).find(), program);
            for (String suffix : Set.of("", "_additive")) {
                var definition = JsonParser.parseString(shaderSource(root + "core/" + program + suffix + ".json")).getAsJsonObject();
                assertEquals(0, definition.getAsJsonArray("samplers").size());
                assertEquals("astralsorcery:" + program, definition.get("fragment").getAsString());
                assertEquals("astralsorcery:effect_procedural", definition.get("vertex").getAsString());
                assertEquals(suffix.isEmpty() ? "1-srcalpha" : "1", definition.getAsJsonObject("blend").get("dstrgb").getAsString());
            }
        }
    }

    private static String shaderSource(String resource) throws IOException {
        try (var stream = ProceduralEffectTest.class.getResourceAsStream(resource)) {
            assertNotNull(stream, resource);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void allReferenceSheetsResolveToDistinctSamplerFreeMaterials() throws IOException {
        var seen = EnumSet.noneOf(ProceduralEffect.class);
        int frames = 0;
        try (var files = Files.walk(REFERENCES)) {
            for (var fixture : files.filter(path -> path.toString().endsWith(".png")).toList()) {
                var sprite = "textures/effect/" + REFERENCES.relativize(fixture).toString().replace('\\', '/');
                var effect = ProceduralEffect.forSprite(ResourceLocation.fromNamespaceAndPath("astralsorcery", sprite));
                assertNotNull(effect, sprite);
                assertTrue(seen.add(effect), sprite);
                assertNull(ProceduralEffect.forSprite(ResourceLocation.fromNamespaceAndPath("other", sprite)));
                assertFalse(Files.exists(ASSETS.resolve(sprite)), sprite);
                frames += effect.columns * effect.rows;
            }
        }
        assertEquals(EnumSet.allOf(ProceduralEffect.class), seen);
        assertEquals(39, seen.size());
        assertEquals(1856, frames);
    }

    @Test
    void onlyStaticTexturesRemainAndNoFrameMeshesArePackaged() throws IOException {
        try (var files = Files.walk(ASSETS.resolve("textures/effect"))) {
            var sprites = files.filter(path -> path.toString().endsWith(".png")).map(ASSETS::relativize)
                    .map(path -> path.toString().replace('\\', '/')).collect(Collectors.toSet());
            assertEquals(Set.of("textures/effect/lightning_part.png", "textures/effect/area_of_effect_cube.png",
                    "textures/effect/particle_small.png", "textures/effect/particle_large.png"), sprites);
            for (String sprite : sprites) {
                assertNull(ProceduralEffect.forSprite(ResourceLocation.fromNamespaceAndPath("astralsorcery", sprite)));
            }
        }
        try (var files = Files.walk(ASSETS)) {
            assertTrue(files.noneMatch(path -> path.toString().endsWith(".asv")));
        }
    }
}
