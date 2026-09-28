package hellfirepvp.astralsorcery.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Optional;

public abstract class TemplateStructureFeature extends Structure {

    public TemplateStructureFeature(StructureSettings settings) {
        super(settings);
    }

    //1.16.5 did not put the structure into DimensionStructuresSettings at all when its config section was
    //disabled (RegistryWorldGeneration#registerStructureGeneration) and skipped it again per biome in
    //#loadBiomeFeatures. Neither hook exists in 1.20.1: structure placement comes from a structure_set datapack
    //entry, and Forge's StructureModifier cannot help because Structure#biomes() still reads the unmodified
    //settings in 47.4.20. Refusing to pick a generation point is the one gate that is actually consulted, so the
    //"enabled" option is honoured here instead.
    protected abstract String getConfigSection();

    protected abstract void addPieces(GenerationContext context, BlockPos pos,
                                      net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder builder);

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        FeatureGenerationConfig config = FeatureGenerationConfig.findByName(this.getConfigSection());
        if (config != null && config.isConfigured() && !config.isEnabled()) {
            return Optional.empty();
        }
        int x = context.chunkPos().getMinBlockX() + context.random().nextInt(16);
        int z = context.chunkPos().getMinBlockZ() + context.random().nextInt(16);
        int y = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.MOTION_BLOCKING,
                context.heightAccessor(), context.randomState());
        BlockPos pos = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(pos, builder -> this.addPieces(context, pos, builder)));
    }

    //1.16.5 hard-coded SURFACE_STRUCTURES in TemplateStructureFeature#getDecorationStage. The 1.20.1 step comes
    //out of data/astralsorcery/worldgen/structure/*.json (which carries that same value), so Structure#step is
    //deliberately not overridden here - overriding it would make the json field a lie.
}
