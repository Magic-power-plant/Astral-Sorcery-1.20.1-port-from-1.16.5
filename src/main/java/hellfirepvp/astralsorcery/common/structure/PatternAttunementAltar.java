package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.state.BlockState;

public class PatternAttunementAltar extends PatternBlockArray {

    public PatternAttunementAltar() {
        super(StructureTypesAS.PTYPE_ATTUNEMENT_ALTAR.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        BlockState arch = BlocksAS.MARBLE_ARCH.get().defaultBlockState();
        BlockState sooty = BlocksAS.BLACK_MARBLE_RAW.get().defaultBlockState();

        addBlock(BlocksAS.ATTUNEMENT_ALTAR.get().defaultBlockState(), 0, 0, 0);

        addBlockCube(arch, -7, -1, -8, 7, -1, -8);
        addBlockCube(arch, -7, -1, 8, 7, -1, 8);
        addBlockCube(arch, -8, -1, -7, -8, -1, 7);
        addBlockCube(arch, 8, -1, -7, 8, -1, 7);
        addBlockCube(sooty, -7, -1, -7, 7, -1, 7);

        pillar(-8, 0, -8);
        pillar(-8, 0, 8);
        pillar(8, 0, -8);
        pillar(8, 0, 8);

        addCorner(arch, -9, -9);
        addCorner(arch, -9, 9);
        addCorner(arch, 9, -9);
        addCorner(arch, 9, 9);
    }

    private void addCorner(BlockState arch, int x, int z) {
        int sx = Integer.signum(x);
        int sz = Integer.signum(z);
        addBlock(arch, x, -1, z);
        addBlock(arch, x, -1, z - sz);
        addBlock(arch, x, -1, z - 2 * sz);
        addBlock(arch, x - sx, -1, z);
        addBlock(arch, x - 2 * sx, -1, z);
    }

    private void pillar(int x, int y, int z) {
        addBlock(BlocksAS.MARBLE_RUNED.get().defaultBlockState(), x, y, z);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), x, y + 1, z);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), x, y + 2, z);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), x, y + 3, z);
        addBlock(BlocksAS.MARBLE_CHISELED.get().defaultBlockState(), x, y + 4, z);
    }
}
