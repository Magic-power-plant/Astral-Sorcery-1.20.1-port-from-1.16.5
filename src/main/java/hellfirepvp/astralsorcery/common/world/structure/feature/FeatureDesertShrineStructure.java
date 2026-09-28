package hellfirepvp.astralsorcery.common.world.structure.feature;

import com.mojang.serialization.Codec;
import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.TemplateStructureFeature;
import hellfirepvp.astralsorcery.common.world.structure.DesertShrineStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class FeatureDesertShrineStructure extends TemplateStructureFeature {

    //See FeatureAncientShrineStructure.CODEC: the settings have to come from the structure json.
    public static final Codec<FeatureDesertShrineStructure> CODEC = simpleCodec(FeatureDesertShrineStructure::new);

    public FeatureDesertShrineStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected String getConfigSection() {
        return WorldGenerationAS.Structures.KEY_DESERT_SHRINE.getPath();
    }

    @Override
    protected void addPieces(GenerationContext context, BlockPos pos, StructurePiecesBuilder builder) {
        builder.addPiece(new DesertShrineStructure(
                WorldGenerationAS.Structures.DESERT_SHRINE_PIECE,
                context.structureTemplateManager(),
                pos));
    }

    @Override
    public StructureType<?> type() {
        return WorldGenerationAS.Structures.DESERT_SHRINE_TYPE;
    }
}
