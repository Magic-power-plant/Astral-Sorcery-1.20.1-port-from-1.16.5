package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PatternSingularity extends PatternBlockArray {

    public PatternSingularity() {
        super(StructureTypesAS.PTYPE_SINGULARITY.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        BlockState runed = BlocksAS.MARBLE_RUNED.get().defaultBlockState();
        BlockState engraved = BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState();
        BlockState chiseled = BlocksAS.MARBLE_CHISELED.get().defaultBlockState();
        BlockState sooty = BlocksAS.BLACK_MARBLE_RAW.get().defaultBlockState();

        addBlockCube(runed, -4, -4, -4, 4, -4, 4);
        addBlockCube(sooty, -3, -4, -3, 3, -4, 3);

        addBlockCube(runed, -4, -4, -4, -2, -4, -2);
        addBlockCube(runed, -4, -4, 4, -2, -4, 2);
        addBlockCube(runed, 4, -4, -4, 2, -4, -2);
        addBlockCube(runed, 4, -4, 4, 2, -4, 2);

        addBlock(sooty, -3, -5, -3);
        addBlock(sooty, -3, -5, 3);
        addBlock(sooty, 3, -5, -3);
        addBlock(sooty, 3, -5, 3);
        addBlock(FluidsAS.LIQUID_STARLIGHT_BLOCK.get().defaultBlockState(), -3, -4, -3);
        addBlock(FluidsAS.LIQUID_STARLIGHT_BLOCK.get().defaultBlockState(), -3, -4, 3);
        addBlock(FluidsAS.LIQUID_STARLIGHT_BLOCK.get().defaultBlockState(), 3, -4, -3);
        addBlock(FluidsAS.LIQUID_STARLIGHT_BLOCK.get().defaultBlockState(), 3, -4, 3);

        addBlock(engraved, -4, -4, -4);
        addBlock(engraved, -4, -4, 4);
        addBlock(engraved, 4, -4, -4);
        addBlock(engraved, 4, -4, 4);
        addBlock(engraved, -4, -4, -2);
        addBlock(engraved, -4, -4, 2);
        addBlock(engraved, 4, -4, -2);
        addBlock(engraved, 4, -4, 2);
        addBlock(engraved, -2, -4, -4);
        addBlock(engraved, -2, -4, 4);
        addBlock(engraved, 2, -4, -4);
        addBlock(engraved, 2, -4, 4);
        addBlock(engraved, -2, -4, -2);
        addBlock(engraved, -2, -4, 2);
        addBlock(engraved, 2, -4, -2);
        addBlock(engraved, 2, -4, 2);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -4, -3, -4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -4, -3, 4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 4, -3, -4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 4, -3, 4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -4, -2, -4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -4, -2, 4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 4, -2, -4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 4, -2, 4);
        addBlock(chiseled, -4, -1, -4);
        addBlock(chiseled, -4, -1, 4);
        addBlock(chiseled, 4, -1, -4);
        addBlock(chiseled, 4, -1, 4);

        addBlock(Blocks.GOLD_BLOCK, 0, -4, 0);
        addBlock(chiseled, 1, -4, 0);
        addBlock(chiseled, 0, -4, 1);
        addBlock(chiseled, -1, -4, 0);
        addBlock(chiseled, 0, -4, -1);

        addBlock(chiseled, -2, -3, -2);
        addBlock(chiseled, -2, -3, 2);
        addBlock(chiseled, 2, -3, -2);
        addBlock(chiseled, 2, -3, 2);
    }
}
