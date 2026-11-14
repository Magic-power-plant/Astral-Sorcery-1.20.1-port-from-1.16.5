package hellfirepvp.astralsorcery.common.data.config.base;

import hellfirepvp.astralsorcery.common.block.util.BlockPredicate;
import hellfirepvp.astralsorcery.common.block.util.BlockStateList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.function.Predicate;

public class ConfiguredBlockStateList implements BlockPredicate, Predicate<BlockState> {

    private final ForgeConfigSpec.ConfigValue<List<String>> configList;
    private BlockStateList resolvedConfiguration = null;

    public ConfiguredBlockStateList(ForgeConfigSpec.ConfigValue<List<String>> configList) {
        this.configList = configList;
    }

    @Override
    public boolean test(BlockState state) {
        if (resolvedConfiguration == null) {
            resolvedConfiguration = BlockStateList.fromConfig(configList.get());
        }
        return this.resolvedConfiguration.test(state);
    }

    @Override
    public boolean test(Level world, BlockPos pos, BlockState state) {
        if (resolvedConfiguration == null) {
            resolvedConfiguration = BlockStateList.fromConfig(configList.get());
        }
        return this.resolvedConfiguration.test(state);
    }
}
