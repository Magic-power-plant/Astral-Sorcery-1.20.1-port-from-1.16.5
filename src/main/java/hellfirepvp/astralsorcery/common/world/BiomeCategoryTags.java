package hellfirepvp.astralsorcery.common.world;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The 1.16.5 worldgen config selected biomes by {@code Biome.Category}, an enum deleted in 1.19.3. The closest
 * 1.20.1 equivalent of "what kind of biome is this" is a biome tag, so every category name an existing
 * astralsorcery-common.toml can contain is mapped here onto the tag(s) covering the same biomes.
 * <p>
 * The table is kept as tag <em>ids</em> and resolved to {@link TagKey}s on first use: touching
 * {@code net.minecraft.core.registries.Registries} initialises {@code BuiltInRegistries}, which cannot load
 * outside a bootstrapped game, and the category names have to stay readable from plain unit tests.
 */
public final class BiomeCategoryTags {

    private static final Map<String, List<String>> CATEGORY_TAG_IDS = ImmutableMap.<String, List<String>>builder()
            //1.16.5 Biome.Category#getString values.
            .put("taiga", Collections.singletonList("minecraft:is_taiga"))
            //EXTREME_HILLS covered both the mountain ranges and their hill variants, which 1.18 split in two.
            .put("extreme_hills", Arrays.asList("minecraft:is_mountain", "minecraft:is_hill"))
            .put("jungle", Collections.singletonList("minecraft:is_jungle"))
            .put("mesa", Collections.singletonList("minecraft:is_badlands"))
            .put("plains", Collections.singletonList("forge:is_plains"))
            .put("savanna", Collections.singletonList("minecraft:is_savanna"))
            .put("icy", Collections.singletonList("forge:is_snowy"))
            .put("the_end", Collections.singletonList("minecraft:is_end"))
            .put("beach", Collections.singletonList("minecraft:is_beach"))
            .put("forest", Collections.singletonList("minecraft:is_forest"))
            .put("ocean", Collections.singletonList("minecraft:is_ocean"))
            .put("desert", Collections.singletonList("forge:is_desert"))
            .put("river", Collections.singletonList("minecraft:is_river"))
            .put("swamp", Collections.singletonList("forge:is_swamp"))
            .put("mushroom", Collections.singletonList("forge:is_mushroom"))
            .put("nether", Collections.singletonList("minecraft:is_nether"))
            .put("underground", Collections.singletonList("forge:is_underground"))
            //1.20.1 spellings for the same things, so a hand-edited config can use the tag names it sees in game.
            .put("mountain", Collections.singletonList("minecraft:is_mountain"))
            .put("badlands", Collections.singletonList("minecraft:is_badlands"))
            .put("snowy", Collections.singletonList("forge:is_snowy"))
            .put("sandy", Collections.singletonList("forge:is_sandy"))
            .put("overworld", Collections.singletonList("minecraft:is_overworld"))
            .build();

    private static volatile Map<String, List<TagKey<Biome>>> resolved;

    private BiomeCategoryTags() {}

    /**
     * The category names {@link #tagsFor(String)} understands. A name outside this set matches no biome at all,
     * so the shipped defaults are asserted against it by WorldGenDataIntegrityTest.
     */
    public static Set<String> knownCategories() {
        return CATEGORY_TAG_IDS.keySet();
    }

    public static Map<String, List<String>> categoryTagIds() {
        return CATEGORY_TAG_IDS;
    }

    /**
     * @return the biome tags a 1.16.5 category name stands for, empty when the name is unknown.
     */
    public static List<TagKey<Biome>> tagsFor(String categoryName) {
        Map<String, List<TagKey<Biome>>> tags = resolved;
        if (tags == null) {
            synchronized (BiomeCategoryTags.class) {
                tags = resolved;
                if (tags == null) {
                    tags = resolve();
                    resolved = tags;
                }
            }
        }
        return tags.getOrDefault(categoryName.toLowerCase(Locale.ROOT), Collections.emptyList());
    }

    private static Map<String, List<TagKey<Biome>>> resolve() {
        Map<String, List<TagKey<Biome>>> tags = new HashMap<>();
        CATEGORY_TAG_IDS.forEach((category, ids) -> {
            List<TagKey<Biome>> keys = new ArrayList<>(ids.size());
            for (String id : ids) {
                keys.add(TagKey.create(Registries.BIOME, ResourceLocation.parse(id)));
            }
            tags.put(category, Collections.unmodifiableList(keys));
        });
        return Collections.unmodifiableMap(tags);
    }
}
