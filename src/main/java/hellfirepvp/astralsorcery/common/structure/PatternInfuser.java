package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PatternInfuser extends PatternBlockArray {

    public PatternInfuser() {
        super(StructureTypesAS.PTYPE_INFUSER.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        addBlock(BlocksAS.INFUSER.get().defaultBlockState(), 0, 0, 0);

        BlockState chiseled = BlocksAS.MARBLE_CHISELED.get().defaultBlockState();
        BlockState runed = BlocksAS.MARBLE_RUNED.get().defaultBlockState();
        BlockState raw = BlocksAS.MARBLE_RAW.get().defaultBlockState();

        addBlock(Blocks.LAPIS_BLOCK.defaultBlockState(), 0, -1, 0);

        for (int i = -2; i <= 2; i++) {
            addBlock(raw, i, -2, -2);
            addBlock(raw, i, -2, 2);
            addBlock(raw, -2, -2, i);
            addBlock(raw, 2, -2, i);
        }

        for (int i = -1; i <= 1; i++) {
            addBlock(runed, i, -1, -1);
            addBlock(runed, i, -1, 1);
            addBlock(runed, -1, -1, i);
            addBlock(runed, 1, -1, i);

            addBlock(runed, i, -1, -3);
            addBlock(runed, i, -1, 3);
            addBlock(runed, 3, -1, i);
            addBlock(runed, -3, -1, i);
        }

        addBlock(chiseled, -2, -1, -2);
        addBlock(chiseled, 2, -1, -2);
        addBlock(chiseled, -2, -1, 2);
        addBlock(chiseled, 2, -1, 2);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -2, 0, -2);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 2, 0, -2);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -2, 0, 2);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 2, 0, 2);
        addBlock(chiseled, -2, 1, -2);
        addBlock(chiseled, 2, 1, -2);
        addBlock(chiseled, -2, 1, 2);
        addBlock(chiseled, 2, 1, 2);
    }
}
