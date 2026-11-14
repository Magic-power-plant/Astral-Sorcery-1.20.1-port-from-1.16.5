/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.freezing;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.util.BlockPredicate;
import hellfirepvp.astralsorcery.common.block.util.BlockPredicates;
import hellfirepvp.astralsorcery.common.block.util.WorldBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockFreezingRecipe
 * Created by HellFirePvP
 * Date: 30.11.2019 / 19:18
 */
public class BlockFreezingRecipe extends WorldFreezingRecipe {

    private final BiFunction<WorldBlockPos, BlockState, BlockState> outputGenerator;

    public BlockFreezingRecipe(ResourceLocation key, BlockPredicate matcher, BlockState output) {
        this(key, matcher, (worldPos, state) -> output);
    }

    public BlockFreezingRecipe(ResourceLocation key, BlockPredicate matcher, BiFunction<WorldBlockPos, BlockState, BlockState> outputGenerator) {
        super(key, matcher);
        this.outputGenerator = outputGenerator;
    }

    public static BlockFreezingRecipe of(BlockState stateIn, BlockState stateOut) {
        // 1.16.5 Block#getRegistryName() is gone in 1.20.1; ForgeRegistries.BLOCKS.getKey(Block) is the replacement
        return new BlockFreezingRecipe(AstralSorcery.key(ForgeRegistries.BLOCKS.getKey(stateIn.getBlock()).getPath()),
                BlockPredicates.isState(stateIn), stateOut);
    }

    public static BlockFreezingRecipe of(Block blockIn, BlockState stateOut) {
        return new BlockFreezingRecipe(AstralSorcery.key(ForgeRegistries.BLOCKS.getKey(blockIn).getPath()),
                BlockPredicates.isBlock(blockIn), stateOut);
    }

    public static BlockFreezingRecipe of(TagKey<Block> blockTagIn, BlockState stateOut) {
        // 1.16.5 ITag.INamedTag<Block> is TagKey<Block>; INamedTag#getName() is TagKey#location()
        return new BlockFreezingRecipe(AstralSorcery.key(String.format("tag_%s", blockTagIn.location().getPath())),
                BlockPredicates.isInTag(blockTagIn), stateOut);
    }

    @Override
    public void doOutput(Level world, BlockPos pos, BlockState state, Consumer<ItemStack> itemOutput) {
        BlockState generated = this.outputGenerator.apply(WorldBlockPos.wrapServer(world, pos), state);
        if (generated != state) {
            // 1.16.5 Constants.BlockFlags.DEFAULT_AND_RERENDER (11) is Block.UPDATE_ALL_IMMEDIATE in 1.20.1
            world.setBlock(pos, generated, Block.UPDATE_ALL_IMMEDIATE);
        }
    }

}
