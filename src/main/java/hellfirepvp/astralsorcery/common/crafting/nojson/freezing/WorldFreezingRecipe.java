/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.freezing;

import hellfirepvp.astralsorcery.common.crafting.nojson.CustomRecipe;
import hellfirepvp.astralsorcery.api.world.IWorldRecipe;
import hellfirepvp.astralsorcery.common.block.util.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldFreezingRecipe
 * Created by HellFirePvP
 * Date: 30.11.2019 / 19:10
 */
public abstract class WorldFreezingRecipe extends CustomRecipe implements IWorldRecipe {

    private final BlockPredicate matcher;

    public WorldFreezingRecipe(ResourceLocation key, BlockPredicate matcher) {
        super(key);
        this.matcher = matcher;
    }

    public boolean canFreeze(Level world, BlockPos pos) {
        return this.matcher.test(world, pos, world.getBlockState(pos));
    }

    @Override
    public boolean matches(Level level, BlockPos pos) {
        return canFreeze(level, pos);
    }

    @Override
    public void process(Level level, BlockPos pos, Consumer<ItemStack> itemOutput) {
        doOutput(level, pos, level.getBlockState(pos), itemOutput);
    }

    public abstract void doOutput(Level world, BlockPos pos, BlockState state, Consumer<ItemStack> itemOutput);
}
