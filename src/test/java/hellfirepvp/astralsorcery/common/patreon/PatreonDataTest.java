package hellfirepvp.astralsorcery.common.patreon;

import hellfirepvp.astralsorcery.common.patreon.data.PatreonDataManager;
import hellfirepvp.astralsorcery.common.patreon.effect.*;
import net.minecraftforge.fml.LogicalSide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.StringReader;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PatreonDataTest {
    private static final UUID OWNER = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID EFFECT = UUID.fromString("11111111-2222-3333-4444-555555555555");
    @AfterEach void reset() { PatreonEffectHelper.replaceEffects(List.of()); }

    @Test void everyStableTypeBuildsSharedDataWithoutRenderingResources() throws Exception {
        for (PatreonEffectType type : PatreonEffectType.values()) {
            List<String> parameters = switch (type) {
                case FLARE_DYNAMIC_COLOR -> List.of(EFFECT.toString());
                case TREE_BEACON_COLOR -> List.of(EFFECT.toString(), "BLUE", "16711680");
                case FLARE_CRYSTAL -> List.of(EFFECT.toString(), "BLUE", "16777215", "crystal_blue");
                case CRYSTAL_FOOTPRINTS -> List.of(EFFECT.toString(), "BLUE", "16777215");
                case HELMET -> List.of(EFFECT.toString(), "minecraft:stone", "BLUE");
                case BLOCK_RING -> List.of(EFFECT.toString(), "BLUE", "1", "10", "4", "20", "[]");
                default -> List.of(EFFECT.toString(), "BLUE");
            };
            PatreonEffectDefinition definition = type.getProvider().buildEffect(OWNER, parameters);
            assertEquals(type, definition.getType());
            assertEquals(EFFECT, definition.getEffectUUID());
            assertEquals(OWNER, definition.getOwnerUUID());
            assertTrue(definition.hasPartialEntity(), type.toString());
            assertEquals(OWNER, definition.createEntity(OWNER).getOwnerUUID());
        }
    }

    @Test void definitionCopiesParametersAndCatalogPublishesBothIndexesTogether() {
        ArrayList<String> parameters = new ArrayList<>(List.of(EFFECT.toString(), "RED"));
        var definition = new PatreonEffectDefinition(PatreonEffectType.FLARE, OWNER, parameters);
        parameters.set(1, "BLUE");
        PatreonEffectHelper.replaceEffects(List.of(definition));
        assertEquals("RED", definition.getParameters().get(1));
        assertThrows(UnsupportedOperationException.class, () -> definition.getParameters().clear());
        assertSame(definition, PatreonEffectHelper.getEffect(EFFECT));
        assertEquals(List.of(definition), PatreonEffectHelper.getPatreonEffects(LogicalSide.SERVER, OWNER));
        assertEquals(List.of(definition), PatreonEffectHelper.getPatreonEffects(LogicalSide.CLIENT, OWNER));
        assertThrows(UnsupportedOperationException.class,
                () -> PatreonEffectHelper.getPatreonEffects(LogicalSide.CLIENT, OWNER).clear());
        assertThrows(IllegalArgumentException.class,
                () -> PatreonEffectHelper.replaceEffects(List.of(definition, definition)));
        assertSame(definition, PatreonEffectHelper.getEffect(EFFECT), "Rejected update must not replace the catalog");
        PatreonEffectHelper.replaceEffects(List.of());
        assertNull(PatreonEffectHelper.getEffect(EFFECT));
        assertTrue(PatreonEffectHelper.getPatreonEffects(LogicalSide.SERVER, OWNER).isEmpty());
    }

    @Test void legacyJsonAndTreeBeaconColorDecodeWithoutCreatingClientEffects() {
        String json = """
                {"effectList":[{"uuid":"%s","effectClass":"TREE_BEACON_COLOR",
                "parameters":["%s","null","16711680"]}]}
                """.formatted(OWNER, EFFECT);
        var effect = PatreonDataManager.decode(new StringReader(json)).get(0);
        assertEquals(java.awt.Color.RED, effect.getTreeBeaconColor());
        assertFalse(effect.hasPartialEntity());
        assertNull(effect.createEntity(OWNER));
        assertEquals(PatreonEffectType.TREE_BEACON_COLOR, effect.getType());
    }

    @Test void helmetWithoutColorAndDynamicFlareKeepTheirCompanionSemantics() {
        var helmet = new PatreonEffectDefinition(PatreonEffectType.HELMET, OWNER,
                List.of(EFFECT.toString(), "minecraft:stone"));
        var dynamic = new PatreonEffectDefinition(PatreonEffectType.FLARE_DYNAMIC_COLOR, OWNER,
                List.of(EFFECT.toString()));
        assertFalse(helmet.hasPartialEntity());
        assertTrue(dynamic.hasPartialEntity());
        assertNull(dynamic.getFlareColor());
    }
}
