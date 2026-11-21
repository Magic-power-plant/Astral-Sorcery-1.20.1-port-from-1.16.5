package hellfirepvp.astralsorcery.common.effect.visual.spec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import hellfirepvp.astralsorcery.api.effect.EffectSpec;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EffectSpecTest {

    @Test
    void jsonAndNbtRoundTripsPreserveAllData() {
        EffectSpec spec = EffectSpec.builder(EffectTemplateIds.LIGHTBEAM)
                .maxAge(64).scale(0.5F).alpha(0.8F).color(0x79ACFE)
                .motion(new Vector3(0.1, 0.2, -0.3)).gravity(-0.03F)
                .alphaMode(EffectSpec.AlphaMode.FADE_OUT).scaleMode(EffectSpec.ScaleMode.SHRINK)
                .beam(new Vector3(0, 8, 2), 0.5, 1.5).build();
        JsonElement json = EffectSpec.CODEC.encodeStart(JsonOps.INSTANCE, spec).result().orElseThrow();
        assertEquals("fade_out", json.getAsJsonObject().getAsJsonObject("properties").get("alpha_mode").getAsString());
        assertEquals(spec, EffectSpec.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow());
        Tag nbt = EffectSpec.CODEC.encodeStart(NbtOps.INSTANCE, spec).result().orElseThrow();
        assertEquals(spec, EffectSpec.CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseThrow());
    }

    @Test
    void omittedPropertiesRemainOmittedInsteadOfOverridingTemplateDefaults() {
        JsonElement json = JsonParser.parseString("{\"template\":\"astralsorcery:generic_particle\"}");
        EffectSpec spec = EffectSpec.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();
        assertEquals(EffectSpec.Properties.EMPTY, spec.properties());
        assertTrue(spec.beam().isEmpty());
        assertEquals(json, EffectSpec.CODEC.encodeStart(JsonOps.INSTANCE, spec).result().orElseThrow());
    }

    @Test
    void snapshotsDoNotShareMutableVectorsOrBuilders() {
        Vector3 motion = new Vector3(1, 2, 3);
        Vector3 end = new Vector3(0, 8, 0);
        EffectSpec.Builder builder = EffectSpec.builder(EffectTemplateIds.LIGHTBEAM)
                .motion(motion).beam(end, 1, 2).scale(0.5F);
        motion.add(10, 10, 10);
        end.addY(10);
        EffectSpec first = builder.build();
        builder.scale(2).maxAge(80);
        EffectSpec second = builder.build();
        assertEquals(new EffectSpec.Vector(1, 2, 3), first.properties().motion().orElseThrow());
        assertEquals(new EffectSpec.Vector(0, 8, 0), first.beam().orElseThrow().endOffset());
        first.properties().motion().orElseThrow().toVector3().addY(500);
        assertEquals(2, first.properties().motion().orElseThrow().y());
        assertEquals(0.5F, first.properties().scale().orElseThrow());
        assertTrue(first.properties().maxAge().isEmpty());
        assertEquals(2F, second.properties().scale().orElseThrow());
        assertEquals(first, first.toBuilder().build());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"properties\":null}", "{\"properties\":42}",
            "{\"properties\":{\"max_age\":0}}", "{\"properties\":{\"max_age\":-1}}",
            "{\"properties\":{\"max_age\":1.5}}", "{\"properties\":{\"max_age\":4294967297}}",
            "{\"properties\":{\"scale\":null}}", "{\"properties\":{\"color\":4.5}}",
            "{\"properties\":{\"scale\":-0.01}}", "{\"properties\":{\"scale\":\"large\"}}",
            "{\"properties\":{\"alpha\":1.1}}", "{\"properties\":{\"color\":16777216}}",
            "{\"properties\":{\"motion\":{\"x\":1}}}",
            "{\"properties\":{\"alpha_mode\":\"fade_typo\"}}",
            "{\"properties\":{\"scale_mode\":\"shrink_typo\"}}",
            "{\"beam\":{}}", "{\"beam\":null}",
            "{\"beam\":{\"end_offset\":{\"x\":0,\"y\":0,\"z\":0},\"from_size\":1,\"to_size\":1}}",
            "{\"beam\":{\"end_offset\":{\"x\":0,\"y\":1,\"z\":0},\"from_size\":-1,\"to_size\":1}}"
    })
    void presentInvalidValuesFailInsteadOfSilentlyFallingBack(String fields) {
        JsonObject json = JsonParser.parseString(fields).getAsJsonObject();
        json.addProperty("template", "astralsorcery:generic_particle");
        assertTrue(EffectSpec.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent(), fields);
    }

    @Test
    void invalidProgrammaticValuesAndNonFiniteEncodedNumbersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE).maxAge(0).build());
        assertThrows(IllegalArgumentException.class, () -> EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE).alpha(Float.NaN).build());
        assertThrows(IllegalArgumentException.class, () -> EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE).scale(Float.POSITIVE_INFINITY).build());
        assertThrows(IllegalArgumentException.class, () -> EffectSpec.builder(EffectTemplateIds.GENERIC_PARTICLE).color(-1).build());
        assertThrows(IllegalArgumentException.class, () -> new EffectSpec.Vector(Double.NaN, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new EffectSpec.Beam(new EffectSpec.Vector(0, 0, 0), 1, 1));
        assertThrows(NullPointerException.class, () -> EffectSpec.builder(null));
        JsonObject properties = new JsonObject();
        properties.addProperty("gravity", Float.POSITIVE_INFINITY);
        JsonObject json = new JsonObject();
        json.addProperty("template", "astralsorcery:generic_particle");
        json.add("properties", properties);
        assertTrue(EffectSpec.CODEC.parse(JsonOps.INSTANCE, json).error().isPresent());
    }

    @Test
    void codecPreservesExtensionTemplateIdsWithoutConsultingARenderer() {
        EffectSpec spec = EffectSpec.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"template\":\"example:custom_effect\"}")).result().orElseThrow();
        assertEquals("example:custom_effect", spec.template().toString());
    }
}
