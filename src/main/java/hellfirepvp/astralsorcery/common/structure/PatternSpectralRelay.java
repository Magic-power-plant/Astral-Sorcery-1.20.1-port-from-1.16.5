package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.world.level.block.state.BlockState;

public class PatternSpectralRelay extends PatternBlockArray {

    public PatternSpectralRelay() {
        super(StructureTypesAS.PTYPE_SPECTRAL_RELAY.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        addBlock(BlocksAS.SPECTRAL_RELAY.get().defaultBlockState(), 0, 0, 0);

        BlockState chiseled = BlocksAS.MARBLE_CHISELED.get().defaultBlockState();
        BlockState arch = BlocksAS.MARBLE_ARCH.get().defaultBlockState();
        BlockState sooty = BlocksAS.BLACK_MARBLE_RAW.get().defaultBlockState();

        addBlock(sooty, 0, -1, 0);

        addBlock(chiseled, -1, -1, -1);
        addBlock(chiseled, 1, -1, -1);
        addBlock(chiseled, -1, -1, 1);
        addBlock(chiseled, 1, -1, 1);

        addBlock(arch, -1, -1, 0);
        addBlock(arch, 1, -1, 0);
        addBlock(arch, 0, -1, -1);
        addBlock(arch, 0, -1, 1);
    }
}
