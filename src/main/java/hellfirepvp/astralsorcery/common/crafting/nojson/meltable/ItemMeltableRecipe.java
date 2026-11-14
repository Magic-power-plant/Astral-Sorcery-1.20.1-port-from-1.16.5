/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.meltable;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.util.BlockPredicate;
import hellfirepvp.astralsorcery.common.block.util.BlockPredicates;
import hellfirepvp.astralsorcery.common.block.util.WorldBlockPos;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
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
 * Class: ItemMeltableRecipe
 * Created by HellFirePvP
 * Date: 29.11.2019 / 23:01
 */
public class ItemMeltableRecipe extends WorldMeltableRecipe {

    private final BiFunction<WorldBlockPos, BlockState, ItemStack> outputGenerator;

    public ItemMeltableRecipe(ResourceLocation key, BlockPredicate matcher, ItemStack output) {
        this(key, matcher, (worldPos, state) -> ItemUtils.copyStackWithSize(output, output.getCount()));
    }

    public ItemMeltableRecipe(ResourceLocation key, BlockPredicate matcher, BiFunction<WorldBlockPos, BlockState, ItemStack> outputGenerator) {
        super(key, matcher);
        this.outputGenerator = outputGenerator;
    }

    public static ItemMeltableRecipe of(BlockState stateIn, ItemStack itemOut) {
        // 1.16.5 Block#getRegistryName() is gone in 1.20.1; ForgeRegistries.BLOCKS.getKey(Block) is the replacement
        return new ItemMeltableRecipe(AstralSorcery.key(ForgeRegistries.BLOCKS.getKey(stateIn.getBlock()).getPath()),
                BlockPredicates.isState(stateIn), itemOut);
    }

    public static ItemMeltableRecipe of(TagKey<Block> blockTagIn, ItemStack itemOut) {
        // 1.16.5 ITag.INamedTag<Block> is TagKey<Block>; INamedTag#getName() is TagKey#location()
        return new ItemMeltableRecipe(AstralSorcery.key(String.format("tag_%s", blockTagIn.location().getPath())),
                BlockPredicates.isInTag(blockTagIn), itemOut);
    }

    @Override
    public void doOutput(Level world, BlockPos pos, BlockState state, Consumer<ItemStack> itemOutput) {
        if (world.removeBlock(pos, false)) {
            ItemStack generated = this.outputGenerator.apply(WorldBlockPos.wrapServer(world, pos), state);
            if (!generated.isEmpty()) {
                itemOutput.accept(generated);
            }
        }
    }
}
