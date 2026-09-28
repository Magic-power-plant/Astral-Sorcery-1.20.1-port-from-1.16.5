package hellfirepvp.astralsorcery.common.world;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.world.placement.config.WorldFilterConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class FeatureGenerationConfig extends ConfigEntry {

    //1.16.5 handed the live config object straight to the placement/decorator it configured. The 1.20.1
    //placements and biome modifiers are deserialised from json instead and can only carry the section name, so
    //they look the instance up here. Filled by the constructor, i.e. by WorldGenerationAS.Config's static init.
    private static final Map<String, FeatureGenerationConfig> BY_NAME = new HashMap<>();

    private List<String> categories = new ArrayList<>();
    private List<ResourceKey<Level>> worlds = new ArrayList<>();
    private boolean defaultEveryBiome = false, defaultEveryWorld = false;

    private ForgeConfigSpec.BooleanValue enabled;
    private ForgeConfigSpec.BooleanValue everyBiome;
    private ForgeConfigSpec.BooleanValue everyWorld;
    private ForgeConfigSpec.ConfigValue<List<String>> biomeCategoryNames;
    private ForgeConfigSpec.ConfigValue<List<String>> worldNames;

    public FeatureGenerationConfig(ResourceLocation featureName) {
        this(featureName.getPath());
    }

    public FeatureGenerationConfig(String featureName) {
        super(featureName);
        BY_NAME.put(this.getPath(), this);
    }

    @Nullable
    public static FeatureGenerationConfig findByName(String name) {
        return BY_NAME.get(name.toLowerCase(Locale.ROOT));
    }

    public <T extends FeatureGenerationConfig> T generatesInBiomes(List<String> biomeCategories) {
        this.categories = biomeCategories;
        return (T) this;
    }

    public <T extends FeatureGenerationConfig> T generatesInWorlds(List<ResourceKey<Level>> worlds) {
        this.worlds = worlds;
        return (T) this;
    }

    public <T extends FeatureGenerationConfig> T setGenerateEveryBiome() {
        this.defaultEveryBiome = true;
        return (T) this;
    }

    public <T extends FeatureGenerationConfig> T setGenerateEveryWorld() {
        this.defaultEveryWorld = true;
        return (T) this;
    }

    @Override
    public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
        this.enabled = cfgBuilder
                .comment("Set this to false to disable this worldgen feature.")
                .translation(translationKey("enabled"))
                .define("enabled", true);
        this.everyBiome = cfgBuilder
                .comment("Set this to true to let this feature generate in any biome.")
                .translation(translationKey("everyBiome"))
                .define("everyBiome", this.defaultEveryBiome);
        this.everyWorld = cfgBuilder
                .comment("Set this to true to let this feature generate in any world. (Does NOT work for structures!)")
                .translation(translationKey("everyWorld"))
                .define("everyWorld", this.defaultEveryWorld);

        String allCategories = String.join(",", BiomeCategoryTags.knownCategories());
        List<String> defaultCategories = categories;
        this.biomeCategoryNames = cfgBuilder
                .comment("Sets the categories to generate this feature in. Available categories: " + allCategories)
                .translation(translationKey("biomeCategoryNames"))
                .define("biomeCategoryNames", defaultCategories);

        List<String> defaultWorlds = worlds.stream()
                .map(ResourceKey::location)
                .map(ResourceLocation::getPath)
                .collect(Collectors.toList());
        this.worldNames = cfgBuilder
                .comment("Sets the worlds to generate this feature in. (Does NOT work for structures!)")
                .translation(translationKey("worldNames"))
                .define("worldNames", defaultWorlds);
    }

    //The ForgeConfigSpec values only exist once createEntries has run, i.e. once something has fed this entry to a
    //BaseConfiguration. Anything reached from datapack loading (biome modifiers, placements) has to be able to ask.
    public boolean isConfigured() {
        return this.enabled != null;
    }

    public boolean isEnabled() {
        return this.enabled.get();
    }

    public boolean canGenerateIn(String categoryName) {
        if (this.everyBiome.get()) {
            return true;
        }
        return this.biomeCategoryNames.get().contains(categoryName);
    }

    //1.16.5: cfg.canGenerateIn(event.getCategory()) in RegistryWorldGeneration#loadBiomeFeatures. There is no
    //category to ask a biome for any more, so the configured category names are turned into biome tags and the
    //biome is tested against those instead.
    public boolean matchesBiome(Holder<Biome> biome) {
        if (this.everyBiome.get()) {
            return true;
        }
        for (String categoryName : this.biomeCategoryNames.get()) {
            for (TagKey<Biome> tag : BiomeCategoryTags.tagsFor(categoryName)) {
                if (biome.is(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    public WorldFilterConfig worldFilterConfig() {
        return new WorldFilterConfig(this.everyWorld::get, () -> {
            return this.worldNames.get().stream()
                    .map(s -> ResourceLocation.parse(s))
                    .map(key -> ResourceKey.create(Registries.DIMENSION, key))
                    .collect(Collectors.toList());
        });
    }
}
