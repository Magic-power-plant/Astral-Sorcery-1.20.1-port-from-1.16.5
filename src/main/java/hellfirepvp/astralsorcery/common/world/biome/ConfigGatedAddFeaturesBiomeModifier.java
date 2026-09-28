package hellfirepvp.astralsorcery.common.world.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.FeatureGenerationConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

/**
 * Port of 1.16.5 {@code RegistryWorldGeneration#loadBiomeFeatures} (common/registry/RegistryWorldGeneration.java:135-154).
 * <p>
 * That method ran on Forge's {@code BiomeLoadingEvent} and, for every configured feature, added it to the biome
 * only when {@code cfg.isEnabled() && cfg.canGenerateIn(event.getCategory())}. Both the event and
 * {@code Biome.Category} are gone in 1.20.1; features are added by data-driven biome modifiers instead.
 * <p>
 * {@code forge:add_features} alone would lose the config gating, so this modifier keeps it: {@code config_entry}
 * names the {@link FeatureGenerationConfig} section under {@code worldgen} in astralsorcery-common.toml, and the
 * feature is only added when that section is enabled and its biome categories match the biome being built. The
 * {@code biomes} field stays as a coarse, datapack-overridable pre-filter (the dimension the feature belongs to),
 * exactly the role the hard-coded overworld check played in 1.16.5.
 */
public record ConfigGatedAddFeaturesBiomeModifier(String configEntry,
                                                  HolderSet<Biome> biomes,
                                                  HolderSet<PlacedFeature> features,
                                                  GenerationStep.Decoration step) implements BiomeModifier {

    public static final Codec<ConfigGatedAddFeaturesBiomeModifier> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.STRING.fieldOf("config_entry")
                            .forGetter(ConfigGatedAddFeaturesBiomeModifier::configEntry),
                    Biome.LIST_CODEC.fieldOf("biomes")
                            .forGetter(ConfigGatedAddFeaturesBiomeModifier::biomes),
                    PlacedFeature.LIST_CODEC.fieldOf("features")
                            .forGetter(ConfigGatedAddFeaturesBiomeModifier::features),
                    GenerationStep.Decoration.CODEC.fieldOf("step")
                            .forGetter(ConfigGatedAddFeaturesBiomeModifier::step)
            ).apply(builder, ConfigGatedAddFeaturesBiomeModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !this.biomes.contains(biome)) {
            return;
        }
        FeatureGenerationConfig config = FeatureGenerationConfig.findByName(this.configEntry);
        //An unknown or not-yet-built section means the config cannot answer; adding the feature keeps the world
        //generating (the placement logs the unknown name) instead of silently dropping all AS worldgen.
        if (config != null && config.isConfigured() && (!config.isEnabled() || !config.matchesBiome(biome))) {
            return;
        }
        BiomeGenerationSettingsBuilder generation = builder.getGenerationSettings();
        this.features.forEach(feature -> generation.addFeature(this.step, feature));
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return WorldGenerationAS.BiomeModifiers.CONFIG_GATED_ADD_FEATURES.get();
    }
}
