/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.builder;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
//1.16.5 imported hellfirepvp.astralsorcery.common.block.util.BlockMatchInformation here. This tree has to use
//the nested BlockTransmutation.BlockMatchInformation, a thin subclass of that very class, because
//BlockTransmutation#addInputOption takes the nested type - see the TODO_PORT[AS-ALT-06] note on the nested
//type for why it still exists. Every use below is otherwise unchanged.
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation.BlockMatchInformation;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeBuilder;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.serializer.BlockTransmutationSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTransmutationBuilder
 * Created by HellFirePvP
 * Date: 07.03.2020 / 17:02
 */
public class BlockTransmutationBuilder extends CustomRecipeBuilder<BlockTransmutation> {

    private final ResourceLocation id;

    //1.16.5: Blocks.AIR.getDefaultState() - renamed to Block#defaultBlockState() in 1.20.1
    private BlockState outputState = Blocks.AIR.defaultBlockState();
    private double starlight = 200.0D;
    private IWeakConstellation constellation = null;
    private ItemStack outputDisplay = ItemStack.EMPTY;
    private final List<BlockMatchInformation> stateCheck = new ArrayList<>();

    private BlockTransmutationBuilder(ResourceLocation id) {
        this.id = id;
    }

    //1.16.5 signature: builder(ForgeRegistryEntry<?> nameProvider)
    //(1.16.5 common/crafting/builder/BlockTransmutationBuilder.java:52-54). IForgeRegistryEntry and
    //ForgeRegistryEntry were removed in 1.20.1 along with getRegistryName(), so the registry name has to be
    //looked up through the registry. Every 1.16.5 call site passed a Block or an Item, both of which are
    //ItemLike, so the path is resolved through ForgeRegistries.ITEMS on the item form.
    //TODO_PORT[AS-ALT-08]: no 1.20.1 supertype covers "anything with a registry name" any more; if a call
    // site ever needs a non-ItemLike name provider it needs its own overload.
    public static BlockTransmutationBuilder builder(ItemLike nameProvider) {
        return new BlockTransmutationBuilder(AstralSorcery.key(ForgeRegistries.ITEMS.getKey(nameProvider.asItem()).getPath()));
    }

    public static BlockTransmutationBuilder builder(ResourceLocation id) {
        return new BlockTransmutationBuilder(id);
    }

    public BlockTransmutationBuilder multiplyStarlightCost(float multiply) {
        this.starlight *= multiply;
        return this;
    }

    public BlockTransmutationBuilder setStarlightCost(double starlight) {
        this.starlight = starlight;
        return this;
    }

    public BlockTransmutationBuilder setConstellation(IWeakConstellation constellation) {
        this.constellation = constellation;
        return this;
    }

    public BlockTransmutationBuilder addInputCheck(Block matchBlock) {
        return this.addInputCheck(matchBlock.defaultBlockState());
    }

    public BlockTransmutationBuilder addInputCheck(BlockState matchState) {
        return this.addInputCheck(matchState, false);
    }

    //1.16.5: ITag<Block> - the tag wrapper types were replaced by TagKey<Block> in 1.20.1.
    public BlockTransmutationBuilder addInputCheck(TagKey<Block> matchTag, ItemStack display) {
        this.stateCheck.add(new BlockMatchInformation(matchTag, display));
        return this;
    }

    public BlockTransmutationBuilder addInputCheck(BlockState matchState, boolean matchExact) {
        return this.addInputCheck(matchState, new ItemStack(matchState.getBlock()), matchExact);
    }

    //1.16.5: IItemProvider -> ItemLike in 1.20.1.
    public BlockTransmutationBuilder addInputCheck(BlockState matchState, ItemLike display, boolean matchExact) {
        return this.addInputCheck(matchState, new ItemStack(display), matchExact);
    }

    public BlockTransmutationBuilder addInputCheck(BlockState matchState, ItemStack display, boolean matchExact) {
        this.stateCheck.add(new BlockMatchInformation(matchState, display, matchExact));
        return this;
    }

    public BlockTransmutationBuilder setOutput(Block output) {
        return this.setOutput(output.defaultBlockState());
    }

    public BlockTransmutationBuilder setOutput(BlockState outputState) {
        this.outputState = outputState;
        this.outputDisplay = new ItemStack(outputState.getBlock());
        return this;
    }

    public BlockTransmutationBuilder setOutputDisplay(ItemLike item) {
        return this.setOutputDisplay(new ItemStack(item));
    }

    public BlockTransmutationBuilder setOutputDisplay(ItemStack stack) {
        this.outputDisplay = stack.copy();
        return this;
    }

    @Nonnull
    @Override
    protected BlockTransmutation validateAndGet() {
        if (this.stateCheck.isEmpty()) {
            throw new IllegalArgumentException("No block input checks!");
        }
        for (BlockMatchInformation inputMatch : this.stateCheck) {
            if (!inputMatch.isValid()) {
                throw new IllegalArgumentException("A block transmutation must not convert 'air' into something!");
            }
        }

        BlockTransmutation tr = new BlockTransmutation(this.id, this.outputState, this.starlight, this.constellation);
        this.stateCheck.forEach(tr::addInputOption);
        tr.setOutputDisplay(this.outputDisplay);
        return tr;
    }

    @Override
    protected CustomRecipeSerializer<BlockTransmutation> getSerializer() {
        //1.16.5: RecipeSerializersAS.BLOCK_TRANSMUTATION_SERIALIZER (common/lib). common/lib is not owned by
        //this slice, so the single instance is held on the serializer class itself.
        return BlockTransmutationSerializer.INSTANCE;
    }
}
