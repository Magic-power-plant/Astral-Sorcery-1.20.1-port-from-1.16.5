package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.state.BlockState;

public class PatternAltarConstellation extends PatternBlockArray {

    public PatternAltarConstellation() {
        super(StructureTypesAS.PTYPE_ALTAR_CONSTELLATION.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        BlockState raw = BlocksAS.MARBLE_RAW.get().defaultBlockState();
        BlockState chiseled = BlocksAS.MARBLE_CHISELED.get().defaultBlockState();
        BlockState runed = BlocksAS.MARBLE_RUNED.get().defaultBlockState();
        BlockState bricks = BlocksAS.MARBLE_BRICKS.get().defaultBlockState();
        BlockState sootyRaw = BlocksAS.BLACK_MARBLE_RAW.get().defaultBlockState();

        addBlockCube(bricks, -4, -1, -4, 4, -1, 4);
        addBlockCube(sootyRaw, -3, -1, -3, 3, -1, 3);
        addBlock(BlocksAS.ALTAR_CONSTELLATION.get().defaultBlockState(), 0, 0, 0);

        addBlock(raw, -4, -1, -4);
        addBlock(raw, -4, -1, -3);
        addBlock(raw, -3, -1, -4);
        addBlock(raw, 4, -1, -4);
        addBlock(raw, 4, -1, -3);
        addBlock(raw, 3, -1, -4);
        addBlock(raw, -4, -1, 4);
        addBlock(raw, -4, -1, 3);
        addBlock(raw, -3, -1, 4);
        addBlock(raw, 4, -1, 4);
        addBlock(raw, 4, -1, 3);
        addBlock(raw, 3, -1, 4);

        addCornerBricks(-5, -5);
        addCornerBricks(5, -5);
        addCornerBricks(-5, 5);
        addCornerBricks(5, 5);

        addBlock(runed, -4, 0, -4);
        addBlock(runed, -4, 0, 4);
        addBlock(runed, 4, 0, -4);
        addBlock(runed, 4, 0, 4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 4, 1, 4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 4, 1, -4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -4, 1, 4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -4, 1, -4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 4, 2, 4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 4, 2, -4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -4, 2, 4);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), -4, 2, -4);
        addBlock(chiseled, -4, 3, -4);
        addBlock(chiseled, -4, 3, 4);
        addBlock(chiseled, 4, 3, -4);
        addBlock(chiseled, 4, 3, 4);
    }

    private void addCornerBricks(int x, int z) {
        BlockState bricks = BlocksAS.MARBLE_BRICKS.get().defaultBlockState();
        int sx = Integer.signum(x);
        int sz = Integer.signum(z);
        addBlock(bricks, x, -1, z);
        addBlock(bricks, x, -1, z - sz);
        addBlock(bricks, x, -1, z - 2 * sz);
        addBlock(bricks, x - sx, -1, z);
        addBlock(bricks, x - 2 * sx, -1, z);
    }
}
