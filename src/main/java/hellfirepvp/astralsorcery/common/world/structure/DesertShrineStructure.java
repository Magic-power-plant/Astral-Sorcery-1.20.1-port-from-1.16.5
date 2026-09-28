package hellfirepvp.astralsorcery.common.world.structure;

import hellfirepvp.astralsorcery.common.lib.WorldGenerationAS;
import hellfirepvp.astralsorcery.common.world.TemplateStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class DesertShrineStructure extends TemplateStructure {

    public DesertShrineStructure(StructurePieceType type, StructureTemplateManager mgr, BlockPos templatePosition) {
        super(type, WorldGenerationAS.Structures.KEY_DESERT_SHRINE, mgr, templatePosition);
        this.setYOffset(-11);
    }

    public DesertShrineStructure(StructurePieceType type, StructureTemplateManager mgr, CompoundTag nbt) {
        super(type, mgr, nbt);
        this.setYOffset(-11);
    }

    @Override
    public ResourceLocation getStructureName() {
        return WorldGenerationAS.Structures.KEY_DESERT_SHRINE;
    }
}
