package hellfirepvp.astralsorcery.common.world.structure.feature;

import com.mojang.serialization.Codec;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.TemplateStructureFeature;
import hellfirepvp.astralsorcery.common.world.structure.AncientShrineStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class FeatureAncientShrineStructure extends TemplateStructureFeature {

    //1.16.5 had no structure json at all, so a unit codec looked harmless. In 1.20.1 the biome list, the
    //decoration step and the terrain adaptation all come from data/astralsorcery/worldgen/structure/*.json via
    //Structure#settingsCodec; a unit codec reads none of it and pinned every shrine to minecraft:plains.
    public static final Codec<FeatureAncientShrineStructure> CODEC = simpleCodec(FeatureAncientShrineStructure::new);

    public FeatureAncientShrineStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected String getConfigSection() {
        return WorldGenerationAS.Structures.KEY_ANCIENT_SHRINE.getPath();
    }

    @Override
    protected void addPieces(GenerationContext context, BlockPos pos, StructurePiecesBuilder builder) {
        builder.addPiece(new AncientShrineStructure(
                WorldGenerationAS.Structures.ANCIENT_SHRINE_PIECE,
                context.structureTemplateManager(),
                pos));
    }

    @Override
    public StructureType<?> type() {
        return WorldGenerationAS.Structures.ANCIENT_SHRINE_TYPE;
    }
}
