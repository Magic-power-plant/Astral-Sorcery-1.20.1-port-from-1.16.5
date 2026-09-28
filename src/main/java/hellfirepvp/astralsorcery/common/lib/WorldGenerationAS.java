package hellfirepvp.astralsorcery.common.lib;

import com.mojang.serialization.Codec;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.world.FeatureGenerationConfig;
import hellfirepvp.astralsorcery.common.world.StructureGenerationConfig;
import hellfirepvp.astralsorcery.common.world.biome.ConfigGatedAddFeaturesBiomeModifier;
import hellfirepvp.astralsorcery.common.world.feature.ReplaceBlockFeature;
import hellfirepvp.astralsorcery.common.world.feature.RockCrystalFeature;
import hellfirepvp.astralsorcery.common.world.structure.AncientShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.DesertShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.SmallShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureAncientShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureDesertShrineStructure;
import hellfirepvp.astralsorcery.common.world.structure.feature.FeatureSmallShrineStructure;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.Collections;

public class WorldGenerationAS {

    private WorldGenerationAS() {}

    public static final DeferredRegister<net.minecraft.world.level.levelgen.feature.Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, AstralSorcery.MODID);

    //1.16.5 injected its features into biomes from RegistryWorldGeneration#loadBiomeFeatures (BiomeLoadingEvent);
    //1.20.1 needs a registered BiomeModifier codec plus data/astralsorcery/forge/biome_modifier/*.json.
    public static class BiomeModifiers {

        public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
                DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, AstralSorcery.MODID);

        public static final RegistryObject<Codec<? extends BiomeModifier>> CONFIG_GATED_ADD_FEATURES =
                BIOME_MODIFIER_SERIALIZERS.register("config_gated_add_features",
                        () -> ConfigGatedAddFeaturesBiomeModifier.CODEC);

    }

    public static class Structures {

        public static final ResourceLocation KEY_ANCIENT_SHRINE = AstralSorcery.key("ancient_shrine");
        public static final ResourceLocation KEY_DESERT_SHRINE = AstralSorcery.key("desert_shrine");
        public static final ResourceLocation KEY_SMALL_SHRINE = AstralSorcery.key("small_shrine");

        public static StructurePieceType.StructureTemplateType ANCIENT_SHRINE_PIECE;
        public static StructurePieceType.StructureTemplateType DESERT_SHRINE_PIECE;
        public static StructurePieceType.StructureTemplateType SMALL_SHRINE_PIECE;

        public static StructureType<?> ANCIENT_SHRINE_TYPE;
        public static StructureType<?> DESERT_SHRINE_TYPE;
        public static StructureType<?> SMALL_SHRINE_TYPE;

    }

    public static class Features {

        public static final ResourceLocation KEY_FEATURE_REPLACE_BLOCK = AstralSorcery.key("replace_block");
        public static final ResourceLocation KEY_FEATURE_ROCK_CRYSTAL = AstralSorcery.key("rock_crystal");

        public static RegistryObject<ReplaceBlockFeature> REPLACE_BLOCK;
        public static RegistryObject<RockCrystalFeature> ROCK_CRYSTAL;

    }

    public static class Config {

        public static StructureGenerationConfig CFG_ANCIENT_SHRINE =
                new StructureGenerationConfig(Structures.KEY_ANCIENT_SHRINE, 18, 4)
                        .generatesInBiomes(Arrays.asList("icy", "extreme_hills"))
                        .generatesInWorlds(Collections.singletonList(Level.OVERWORLD));
        public static StructureGenerationConfig CFG_DESERT_SHRINE =
                new StructureGenerationConfig(Structures.KEY_DESERT_SHRINE, 18, 4)
                        .generatesInBiomes(Arrays.asList("mesa", "desert", "savanna"))
                        .generatesInWorlds(Collections.singletonList(Level.OVERWORLD));
        public static StructureGenerationConfig CFG_SMALL_SHRINE =
                new StructureGenerationConfig(Structures.KEY_SMALL_SHRINE, 18, 4)
                        .generatesInBiomes(Arrays.asList("forest", "plains"))
                        .generatesInWorlds(Collections.singletonList(Level.OVERWORLD));

        //1.16.5 WorldGenerationAS.Config:76-91 - the three feature sections besides rock crystal were missing from
        //this tree, which left RegistryWorldGeneration#addConfigEntries unable to register them. Defaults copied
        //verbatim, including marble's "every world" (it has no world list at all in 1.16.5).
        public static FeatureGenerationConfig CFG_GLOW_FLOWER =
                new FeatureGenerationConfig("glow_flower")
                        .generatesInBiomes(Arrays.asList("icy", "extreme_hills"))
                        .generatesInWorlds(Collections.singletonList(Level.OVERWORLD));

        public static FeatureGenerationConfig CFG_ROCK_CRYSTAL =
                new FeatureGenerationConfig(Features.KEY_FEATURE_ROCK_CRYSTAL)
                        .setGenerateEveryBiome()
                        .generatesInWorlds(Collections.singletonList(Level.OVERWORLD));

        public static FeatureGenerationConfig CFG_AQUAMARINE =
                new FeatureGenerationConfig("aquamarine")
                        .setGenerateEveryBiome()
                        .generatesInWorlds(Collections.singletonList(Level.OVERWORLD));

        public static FeatureGenerationConfig CFG_MARBLE =
                new FeatureGenerationConfig("marble")
                        .setGenerateEveryBiome()
                        .setGenerateEveryWorld();

    }

    @SuppressWarnings("unchecked")
    public static void init() {
        //Registered from RegistryWorldGeneration#registerWorldGenTypes: STRUCTURE_PIECE is frozen during mod construction.
        Structures.ANCIENT_SHRINE_PIECE = (mgr, pos) -> new AncientShrineStructure(Structures.ANCIENT_SHRINE_PIECE, mgr, pos);
        Structures.DESERT_SHRINE_PIECE = (mgr, pos) -> new DesertShrineStructure(Structures.DESERT_SHRINE_PIECE, mgr, pos);
        Structures.SMALL_SHRINE_PIECE = (mgr, pos) -> new SmallShrineStructure(Structures.SMALL_SHRINE_PIECE, mgr, pos);

        Structures.ANCIENT_SHRINE_TYPE = () -> (com.mojang.serialization.Codec) FeatureAncientShrineStructure.CODEC;
        Structures.DESERT_SHRINE_TYPE = () -> (com.mojang.serialization.Codec) FeatureDesertShrineStructure.CODEC;
        Structures.SMALL_SHRINE_TYPE = () -> (com.mojang.serialization.Codec) FeatureSmallShrineStructure.CODEC;

        Features.REPLACE_BLOCK = FEATURES.register("replace_block", ReplaceBlockFeature::new);
        Features.ROCK_CRYSTAL = FEATURES.register("rock_crystal", RockCrystalFeature::new);
    }
}
