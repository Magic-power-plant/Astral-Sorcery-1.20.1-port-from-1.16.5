package hellfirepvp.astralsorcery.common.world.structure;

import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.TemplateStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class SmallShrineStructure extends TemplateStructure {

    public SmallShrineStructure(StructurePieceType type, StructureTemplateManager mgr, BlockPos templatePosition) {
        super(type, WorldGenerationAS.Structures.KEY_SMALL_SHRINE, mgr, templatePosition);
    }

    public SmallShrineStructure(StructurePieceType type, StructureTemplateManager mgr, CompoundTag nbt) {
        super(type, mgr, nbt);
    }

    @Override
    public ResourceLocation getStructureName() {
        return WorldGenerationAS.Structures.KEY_SMALL_SHRINE;
    }
}
