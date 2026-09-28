package hellfirepvp.astralsorcery.common.world;

import hellfirepvp.astralsorcery.common.world.marker.MarkerManagerAS;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class TemplateStructure extends TemplateStructurePiece {

    private int yOffset = 0;

    public TemplateStructure(StructurePieceType type, ResourceLocation location, StructureTemplateManager mgr, BlockPos templatePosition) {
        super(type, 0, mgr, location, location.toString(), new StructurePlaceSettings()
                .setIgnoreEntities(true)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK), templatePosition);
    }

    public TemplateStructure(StructurePieceType type, StructureTemplateManager mgr, CompoundTag nbt) {
        super(type, nbt, mgr, (loc) -> new StructurePlaceSettings()
                .setIgnoreEntities(true)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK));
    }

    public <T extends TemplateStructure> T setYOffset(int yOffset) {
        this.yOffset = yOffset;
        return (T) this;
    }

    public abstract ResourceLocation getStructureName();

    @Override
    public void postProcess(WorldGenLevel world, StructureManager mgr, ChunkGenerator gen, RandomSource rand, BoundingBox box, ChunkPos chunkPos, BlockPos structCenter) {
        BoundingBox genBox = new BoundingBox(box.minX(), box.minY() + this.yOffset, box.minZ(), box.maxX(), box.maxY() + this.yOffset, box.maxZ());

        BlockPos original = this.templatePosition();
        this.templatePosition = original.above(this.yOffset);
        try {
            super.postProcess(world, mgr, gen, rand, genBox, chunkPos, structCenter.above(yOffset));
        } finally {
            this.templatePosition = original;
            this.placeSettings().setBoundingBox(box);
            this.boundingBox = this.template().getBoundingBox(this.placeSettings(), this.templatePosition());
        }
    }

    @Override
    protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor worldIn, RandomSource rand, BoundingBox sbb) {
        if (sbb.isInside(pos)) {
            MarkerManagerAS.handleMarker(function, pos, worldIn, rand, boundingBox);
        }
    }
}
