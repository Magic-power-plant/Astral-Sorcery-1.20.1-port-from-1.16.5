package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.state.BlockState;

public class PatternAltarTrait extends PatternBlockArray {

    public PatternAltarTrait() {
        super(StructureTypesAS.PTYPE_ALTAR_TRAIT.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        BlockState bricks = BlocksAS.MARBLE_BRICKS.get().defaultBlockState();

        StructureTypesAS.PTYPE_ALTAR_CONSTELLATION.getStructure().getContents().forEach((pos, state) -> addBlock(state, pos));
        addBlock(BlocksAS.ALTAR_RADIANCE.get().defaultBlockState(), 0, 0, 0);

        addBlock(bricks, 4, 3, 3);
        addBlock(bricks, 4, 3, -3);
        addBlock(bricks, -4, 3, 3);
        addBlock(bricks, -4, 3, -3);
        addBlock(bricks, 3, 3, 4);
        addBlock(bricks, -3, 3, 4);
        addBlock(bricks, 3, 3, -4);
        addBlock(bricks, -3, 3, -4);
        addBlock(bricks, 3, 4, 3);
        addBlock(bricks, 3, 4, 2);
        addBlock(bricks, 3, 4, 1);
        addBlock(bricks, 3, 4, -1);
        addBlock(bricks, 3, 4, -2);
        addBlock(bricks, 3, 4, -3);
        addBlock(bricks, 2, 4, -3);
        addBlock(bricks, 1, 4, -3);
        addBlock(bricks, -1, 4, -3);
        addBlock(bricks, -2, 4, -3);
        addBlock(bricks, -3, 4, -3);
        addBlock(bricks, -3, 4, -2);
        addBlock(bricks, -3, 4, -1);
        addBlock(bricks, -3, 4, 1);
        addBlock(bricks, -3, 4, 2);
        addBlock(bricks, -3, 4, 3);
        addBlock(bricks, -2, 4, 3);
        addBlock(bricks, -1, 4, 3);
        addBlock(bricks, 1, 4, 3);
        addBlock(bricks, 2, 4, 3);
    }
}
