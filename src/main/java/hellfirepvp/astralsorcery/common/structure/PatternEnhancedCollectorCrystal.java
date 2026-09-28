package hellfirepvp.astralsorcery.common.structure;

import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.match.block.MatchableState;
import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import hellfirepvp.astralsorcery.common.tile.TileCollectorCrystal;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PatternEnhancedCollectorCrystal extends PatternBlockArray {

    public PatternEnhancedCollectorCrystal() {
        super(StructureTypesAS.PTYPE_ENHANCED_COLLECTOR_CRYSTAL.getRegistryName());
        makeStructure();
    }

    private void makeStructure() {
        BlockState chiseled = BlocksAS.MARBLE_CHISELED.get().defaultBlockState();
        BlockState raw = BlocksAS.MARBLE_RAW.get().defaultBlockState();
        BlockState runed = BlocksAS.MARBLE_RUNED.get().defaultBlockState();
        BlockState engraved = BlocksAS.MARBLE_ENGRAVED.get().defaultBlockState();

        addBlockCube(raw, -1, -5, -1, 1, -5, 1);
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                for (int yy = -1; yy <= 1; yy++) {
                    addBlock(MatchableState.REQUIRES_AIR, xx, yy, zz);
                }
            }
        }
        for (BlockPos offset : TileCollectorCrystal.OFFSETS_LIQUID_STARLIGHT) {
            addBlock(FluidsAS.LIQUID_STARLIGHT_BLOCK.get().defaultBlockState(), offset);
        }

        addBlock(BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get().defaultBlockState(), 0, 0, 0);

        addBlock(chiseled, 0, -2, 0);
        addBlock(BlocksAS.MARBLE_PILLAR.get(), 0, -3, 0);
        addBlock(engraved, 0, -4, 0);

        addBlock(chiseled, -2, -4, -2);
        addBlock(chiseled, -2, -4, 2);
        addBlock(chiseled, 2, -4, 2);
        addBlock(chiseled, 2, -4, -2);
        addBlock(engraved, -2, -3, -2);
        addBlock(engraved, -2, -3, 2);
        addBlock(engraved, 2, -3, 2);
        addBlock(engraved, 2, -3, -2);

        addBlock(runed, -2, -4, -1);
        addBlock(runed, -2, -4, 0);
        addBlock(runed, -2, -4, 1);
        addBlock(runed, 2, -4, -1);
        addBlock(runed, 2, -4, 0);
        addBlock(runed, 2, -4, 1);
        addBlock(runed, -1, -4, -2);
        addBlock(runed, 0, -4, -2);
        addBlock(runed, 1, -4, -2);
        addBlock(runed, -1, -4, 2);
        addBlock(runed, 0, -4, 2);
        addBlock(runed, 1, -4, 2);
    }
}
