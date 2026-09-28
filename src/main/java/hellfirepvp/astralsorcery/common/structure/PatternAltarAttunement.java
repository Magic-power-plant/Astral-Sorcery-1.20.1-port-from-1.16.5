package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.state.BlockState;

public class PatternAltarAttunement extends PatternBlockArray {

    public PatternAltarAttunement() {
        super(StructureTypesAS.PTYPE_ALTAR_ATTUNEMENT.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        BlockState chiseled = BlocksAS.MARBLE_CHISELED.get().defaultBlockState();
        BlockState bricks = BlocksAS.MARBLE_BRICKS.get().defaultBlockState();
        BlockState arch = BlocksAS.MARBLE_ARCH.get().defaultBlockState();
        BlockState sootyRaw = BlocksAS.BLACK_MARBLE_RAW.get().defaultBlockState();

        addBlockCube(sootyRaw, -3, -1, -3, 3, -1, 3);
        addBlock(BlocksAS.ALTAR_ATTUNEMENT.get().defaultBlockState(), 0, 0, 0);

        for (int i = -3; i <= 3; i++) {
            addBlock(arch, 4, -1, i);
            addBlock(arch, -4, -1, i);
            addBlock(arch, i, -1, 4);
            addBlock(arch, i, -1, -4);
            addBlock(bricks, 3, -1, i);
            addBlock(bricks, -3, -1, i);
            addBlock(bricks, i, -1, 3);
            addBlock(bricks, i, -1, -3);
        }
        addBlock(chiseled, 3, -1, 3);
        addBlock(chiseled, 3, -1, -3);
        addBlock(chiseled, -3, -1, 3);
        addBlock(chiseled, -3, -1, -3);

        addBlock(bricks, 2, -1, 0);
        addBlock(bricks, -2, -1, 0);
        addBlock(bricks, 0, -1, 2);
        addBlock(bricks, 0, -1, -2);

        addBlock(BlocksAS.MARBLE_PILLAR.get(), 3, 0, 3);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 3, 0, -3);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -3, 0, 3);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -3, 0, -3);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 3, 1, 3);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 3, 1, -3);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -3, 1, 3);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -3, 1, -3);

        addBlock(chiseled, 3, 2, 3);
        addBlock(chiseled, 3, 2, -3);
        addBlock(chiseled, -3, 2, 3);
        addBlock(chiseled, -3, 2, -3);
    }
}
