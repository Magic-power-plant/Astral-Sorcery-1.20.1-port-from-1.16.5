package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.block.MatchableState;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.state.BlockState;

public class PatternRitualPedestal extends PatternBlockArray {

    public PatternRitualPedestal() {
        super(StructureTypesAS.PTYPE_RITUAL_PEDESTAL.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        addBlockCube(MatchableState.REQUIRES_AIR, -2, 0, -2, 2, 2, 2);
        addBlockCube(MatchableState.REQUIRES_AIR, -3, 0, -1, 3, 2, 1);
        addBlockCube(MatchableState.REQUIRES_AIR, -1, 0, -3, 1, 2, 3);

        addBlock(BlocksAS.RITUAL_PEDESTAL.get().defaultBlockState(), 0, 0, 0);

        BlockState chiseled = BlocksAS.MARBLE_CHISELED.get().defaultBlockState();
        BlockState bricks = BlocksAS.MARBLE_BRICKS.get().defaultBlockState();
        BlockState raw = BlocksAS.MARBLE_RAW.get().defaultBlockState();
        BlockState arch = BlocksAS.MARBLE_ARCH.get().defaultBlockState();

        addBlock(chiseled, 0, -1, 0);

        addBlock(bricks, 0, -1, 1);
        addBlock(bricks, 0, -1, 2);
        addBlock(bricks, 0, -1, 3);
        addBlock(bricks, 1, -1, 3);
        addBlock(bricks, -1, -1, 3);
        addBlock(bricks, 0, -1, -1);
        addBlock(bricks, 0, -1, -2);
        addBlock(bricks, 0, -1, -3);
        addBlock(bricks, 1, -1, -3);
        addBlock(bricks, -1, -1, -3);
        addBlock(bricks, 1, -1, 0);
        addBlock(bricks, 2, -1, 0);
        addBlock(bricks, 3, -1, 0);
        addBlock(bricks, 3, -1, 1);
        addBlock(bricks, 3, -1, -1);
        addBlock(bricks, -1, -1, 0);
        addBlock(bricks, -2, -1, 0);
        addBlock(bricks, -3, -1, 0);
        addBlock(bricks, -3, -1, 1);
        addBlock(bricks, -3, -1, -1);
        addBlock(bricks, 2, -1, 2);
        addBlock(bricks, -2, -1, 2);
        addBlock(bricks, 2, -1, -2);
        addBlock(bricks, -2, -1, -2);

        addBlock(raw, 1, -1, 1);
        addBlock(raw, 1, -1, 2);
        addBlock(raw, 2, -1, 1);
        addBlock(raw, -1, -1, 1);
        addBlock(raw, -1, -1, 2);
        addBlock(raw, -2, -1, 1);
        addBlock(raw, 1, -1, -1);
        addBlock(raw, 1, -1, -2);
        addBlock(raw, 2, -1, -1);
        addBlock(raw, -1, -1, -1);
        addBlock(raw, -1, -1, -2);
        addBlock(raw, -2, -1, -1);

        addBlock(arch, 0, -1, 4);
        addBlock(arch, 1, -1, 4);
        addBlock(arch, -1, -1, 4);
        addBlock(arch, 0, -1, -4);
        addBlock(arch, 1, -1, -4);
        addBlock(arch, -1, -1, -4);
        addBlock(arch, 4, -1, 0);
        addBlock(arch, 4, -1, 1);
        addBlock(arch, 4, -1, -1);
        addBlock(arch, -4, -1, 0);
        addBlock(arch, -4, -1, 1);
        addBlock(arch, -4, -1, -1);
        addBlock(arch, 3, -1, 2);
        addBlock(arch, 3, -1, -2);
        addBlock(arch, -3, -1, 2);
        addBlock(arch, -3, -1, -2);
        addBlock(arch, 2, -1, 3);
        addBlock(arch, -2, -1, 3);
        addBlock(arch, 2, -1, -3);
        addBlock(arch, -2, -1, -3);
    }
}
