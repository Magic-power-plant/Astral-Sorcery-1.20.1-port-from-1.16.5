package hellfirepvp.astralsorcery.common.world.feature;

import hellfirepvp.astralsorcery.common.data.world.RockCrystalBuffer;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

public class RockCrystalFeature extends ReplaceBlockFeature {

    @Override
    protected boolean setBlockState(WorldGenLevel world, BlockPos pos, BlockState state) {
        ((RockCrystalBuffer) DataAS.DOMAIN_AS.getData(world.getLevel(), DataAS.KEY_ROCK_CRYSTAL_BUFFER)).addOre(pos);
        return super.setBlockState(world, pos, state);
    }
}
