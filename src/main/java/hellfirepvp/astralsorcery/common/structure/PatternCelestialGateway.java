package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.state.BlockState;

public class PatternCelestialGateway extends PatternBlockArray {

    public PatternCelestialGateway() {
        super(StructureTypesAS.PTYPE_CELESTIAL_GATEWAY.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        BlockState arch = BlocksAS.MARBLE_ARCH.get().defaultBlockState();
        BlockState runed = BlocksAS.MARBLE_RUNED.get().defaultBlockState();
        BlockState engraved = BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState();
        BlockState sooty = BlocksAS.BLACK_MARBLE_RAW.get().defaultBlockState();

        addBlock(BlocksAS.CELESTIAL_GATEWAY.get().defaultBlockState(), 0, 0, 0);

        addBlockCube(arch, -3, -1, -3, 3, -1, 3);
        addBlockCube(sooty, -2, -1, -2, 2, -1, 2);

        addBlock(runed, -3, -1, -3);
        addBlock(runed, 3, -1, -3);
        addBlock(runed, 3, -1, 3);
        addBlock(runed, -3, -1, 3);

        addBlock(engraved, -3, 0, -3);
        addBlock(engraved, 3, 0, -3);
        addBlock(engraved, 3, 0, 3);
        addBlock(engraved, -3, 0, 3);
    }
}
