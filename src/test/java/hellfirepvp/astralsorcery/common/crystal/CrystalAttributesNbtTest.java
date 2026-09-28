package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Properties.*;
import static org.junit.jupiter.api.Assertions.*;

class CrystalAttributesNbtTest {

    private static final String MISSING_KNOWLEDGE = "astralsorcery.progress.missing.knowledge";

    // Item tag in the layout 1.16.5 wrote (and 1.20.1 keeps when upgrading a world): the property id is the
    // registry name and the tier is "pLevel". 1.16.5 always stored discovered:1b; this port's generator stored
    // discovered:0b, so both values are covered.
    private static final String LEGACY_ITEM_TAG = "{astralsorcery:{crystalProperties:{attributes:["
            + "{property:\"astralsorcery:shape\",pLevel:3,discovered:0b},"
            + "{property:\"astralsorcery:size\",pLevel:2,discovered:1b},"
            + "{property:\"astralsorcery:tool.durability\",pLevel:1,discovered:0b}"
            + "]}}}";

    @BeforeAll
    static void setup() {
        CrystalTestSupport.init();
    }

    @Test
    void legacyShapeAttributeIsReadBackAsShape() throws Exception {
        CompoundTag itemTag = TagParser.parseTag(LEGACY_ITEM_TAG);

        CrystalAttributes attributes = CrystalAttributes.getCrystalAttributes(NBTHelper.getPersistentData(itemTag));

        assertNotNull(attributes);
        assertEquals(3, attributes.getCrystalAttributes().size());
        CrystalAttributes.Attribute shape = attributes.getAttribute(PROPERTY_SHAPE);
        assertNotNull(shape, "astralsorcery:shape must resolve to the shape property");
        assertSame(PROPERTY_SHAPE, shape.getProperty());
        assertEquals(3, shape.getTier());
        assertEquals(2, attributes.getAttribute(PROPERTY_SIZE).getTier());
        assertEquals(1, attributes.getAttribute(PROPERTY_TOOL_DURABILITY).getTier());
        assertEquals(6, attributes.getTotalTierLevel());
    }

    @Test
    void legacyUndiscoveredAttributesAreShownOnceResearched() throws Exception {
        CrystalAttributes attributes = CrystalAttributes.getCrystalAttributes(
                NBTHelper.getPersistentData(TagParser.parseTag(LEGACY_ITEM_TAG)));
        assertNotNull(attributes);
        assertFalse(attributes.hasUnknownAttributes(), "discovered:0b must not hide an attribute");

        PlayerProgress researched = new PlayerProgress();
        researched.forceGainResearch(ResearchProgression.BASIC_CRAFT);
        List<Component> tooltip = new ArrayList<>();
        CrystalAttributes.TooltipResult result = attributes.addTooltip(tooltip, researched);

        assertEquals(CrystalAttributes.TooltipResult.ADDED_ALL, result);
        List<String> keys = translationKeys(tooltip);
        assertEquals(List.of(
                "crystal.property.astralsorcery.shape.name",
                "crystal.property.astralsorcery.size.name",
                "crystal.property.astralsorcery.tool.durability.name"), keys);

        List<Component> hidden = new ArrayList<>();
        CrystalAttributes.TooltipResult hiddenResult = attributes.addTooltip(hidden, new PlayerProgress());
        assertEquals(CrystalAttributes.TooltipResult.ALL_MISSING, hiddenResult);
        assertEquals(List.of(MISSING_KNOWLEDGE), translationKeys(hidden));
    }

    @Test
    void attributesSurviveTheItemTagSaveAndLoadPath() throws Exception {
        CrystalAttributes original = CrystalAttributes.Builder.newBuilder(false)
                .addProperty(PROPERTY_SIZE, 3)
                .addProperty(PROPERTY_SHAPE, 2)
                .addProperty(PROPERTY_TOOL_EFFICIENCY, 1)
                .addProperty(PROPERTY_CST_AEVITAS, 2)
                .build();
        CompoundTag itemTag = new CompoundTag();
        original.store(NBTHelper.getPersistentData(itemTag));

        // Same compressed NBT path a chunk or player file takes.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NbtIo.writeCompressed(itemTag, out);
        CompoundTag reloaded = NbtIo.readCompressed(new ByteArrayInputStream(out.toByteArray()));

        CrystalAttributes restored = CrystalAttributes.getCrystalAttributes(NBTHelper.getPersistentData(reloaded));
        assertEquals(original, restored);
        assertEquals(8, restored.getTotalTierLevel());

        // The stored layout stays the one older saves use, so they keep loading and this data stays readable.
        ListTag stored = reloaded.getCompound("astralsorcery").getCompound("crystalProperties")
                .getList("attributes", Tag.TAG_COMPOUND);
        assertEquals(4, stored.size());
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < stored.size(); i++) {
            CompoundTag entry = stored.getCompound(i);
            ids.add(entry.getString("property"));
            assertTrue(entry.getInt("pLevel") > 0);
            assertTrue(entry.getBoolean("discovered"));
        }
        assertTrue(ids.contains("astralsorcery:shape"));
        assertTrue(ids.contains("astralsorcery:constellation.aevitas"));
    }

    @Test
    void emptyCrystalPropertiesTagIsTreatedAsNotGenerated() {
        CompoundTag persistent = new CompoundTag();
        assertNull(CrystalAttributes.getCrystalAttributes(persistent));
        persistent.put("crystalProperties", new CompoundTag());
        assertNull(CrystalAttributes.getCrystalAttributes(persistent),
                "an empty tag must still let ItemCrystalBase#inventoryTick generate attributes");
    }

    private static List<String> translationKeys(List<Component> tooltip) {
        List<String> keys = new ArrayList<>();
        for (Component line : tooltip) {
            keys.add(((TranslatableContents) line.getContents()).getKey());
        }
        return keys;
    }
}
