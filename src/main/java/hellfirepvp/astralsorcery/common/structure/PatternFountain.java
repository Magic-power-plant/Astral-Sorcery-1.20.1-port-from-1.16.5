package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.block.MatchableState;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.state.BlockState;

public class PatternFountain extends PatternBlockArray {

    public PatternFountain() {
        super(StructureTypesAS.PTYPE_FOUNTAIN.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        BlockState runed = BlocksAS.MARBLE_RUNED.get().defaultBlockState();
        BlockState sooty = BlocksAS.BLACK_MARBLE_RAW.get().defaultBlockState();

        for (int xx = -3; xx <= 3; xx++) {
            for (int zz = -3; zz <= 3; zz++) {
                for (int yy = -6; yy <= 3; yy++) {
                    if (Math.abs(xx) == 3 && Math.abs(zz) == 3) {
                        continue;
                    }
                    if (xx == 0 && zz == 0 && Math.abs(yy) == 1) {
                        continue;
                    }
                    addBlock(MatchableState.REQUIRES_AIR, xx, yy, zz);
                }
            }
        }

        addBlock(BlocksAS.FOUNTAIN.get(), 0, 0, 0);

        addBlock(sooty, 4, 0, 0);
        addBlock(sooty, -4, 0, 0);
        addBlock(sooty, 0, 0, 4);
        addBlock(sooty, 0, 0, -4);

        addPillarColumn(4, 0);
        addPillarColumn(-4, 0);
        addPillarColumn(0, 4);
        addPillarColumn(0, -4);

        addBlock(runed, 4, 0, 1);
        addBlock(runed, 4, 0, 2);
        addBlock(runed, 4, 0, -1);
        addBlock(runed, 4, 0, -2);

        addBlock(runed, -4, 0, 1);
        addBlock(runed, -4, 0, 2);
        addBlock(runed, -4, 0, -1);
        addBlock(runed, -4, 0, -2);

        addBlock(runed, 1, 0, 4);
        addBlock(runed, 2, 0, 4);
        addBlock(runed, -1, 0, 4);
        addBlock(runed, -2, 0, 4);

        addBlock(runed, 1, 0, -4);
        addBlock(runed, 2, 0, -4);
        addBlock(runed, -1, 0, -4);
        addBlock(runed, -2, 0, -4);

        addBlock(runed, 3, 0, 3);
        addBlock(runed, 3, 0, -3);
        addBlock(runed, -3, 0, -3);
        addBlock(runed, -3, 0, 3);
    }

    private void addPillarColumn(int x, int z) {
        addBlock(BlocksAS.MARBLE_PILLAR.get(), x, 1, z);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), x, 2, z);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), x, -1, z);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), x, -2, z);
    }
}
