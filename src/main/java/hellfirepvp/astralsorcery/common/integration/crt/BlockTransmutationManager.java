/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.crt;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.block.util.BlockMatchInformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTransmutationManager
 * Created by Jaredlll08
 * Date: 03.17.2021 / 15:36
 */
//BLOCKED_PORT[AS-INT-03]: 1.16.5 declared
//   @ZenRegister
//   @ZenCodeType.Name("mods.astralsorcery.BlockTransmutationManager")
//   public class BlockTransmutationManager implements IRecipeManager
// (1.16.5 BlockTransmutationManager.java:41-43). No CraftTweaker artifact is on the compile
// classpath, so com.blamejared.crafttweaker.* (CraftTweakerAPI, IRecipeManager, IItemStack,
// MCTag, ActionAddRecipe, ActionRemoveRecipe, ExpandBlockState) and org.openzen.zencode.java.ZenCodeType
// cannot be imported. CraftTweaker-typed parameters follow the same degradation rule as
// AltarManager: a wrapper the reference only unwraps (MCTag<Block> -> TagKey<Block>) becomes the
// vanilla type it wraps, a wrapper whose CraftTweaker-only methods are used (IItemStack#matches)
// becomes Object. @ZenCodeType.Optional("null") becomes a plain @Nullable parameter until the
// annotation is available again.
public class BlockTransmutationManager {

    /**
     * 1.16.5 signature:
     * void addRecipe(String name, BlockState outState, MCTag<Block> input, double starlight,
     *                @ZenCodeType.Optional("null") ResourceLocation constellationKey)
     */
    public void addRecipe(String name, BlockState outState, TagKey<Block> input, double starlight, @Nullable ResourceLocation constellationKey) {
        addTransmutation(name, outState, starlight, constellationKey, transmutation -> {
            //TODO_PORT[AS-INT-06]: 1.16.5 body (1.16.5 BlockTransmutationManager.java:48):
            //   transmutation.addInputOption(new BlockMatchInformation((ITag<Block>) input.getInternal()));
            // 1.20.1: ITag<Block> became TagKey<Block> (which is why the MCTag<Block> parameter is
            // degraded to TagKey<Block> here), and common/util/block/BlockMatchInformation already has
            // a BlockMatchInformation(TagKey<Block>) constructor for exactly that.
            // The blocker is on the Astral Sorcery side: BlockTransmutation#addInputOption takes the
            // *nested* BlockTransmutation.BlockMatchInformation in this tree, whose only constructor is
            // private and whose only public factories are read(JsonObject)/read(FriendlyByteBuf) - a tag
            // match cannot be expressed through either, so the call cannot be written from here at all.
        });
    }

    /**
     * 1.16.5 signature:
     * void addRecipe(String name, BlockState outState, BlockState input, boolean exact, double starlight,
     *                @ZenCodeType.Optional("null") ResourceLocation constellationKey)
     */
    public void addRecipe(String name, BlockState outState, BlockState input, boolean exact, double starlight, @Nullable ResourceLocation constellationKey) {
        addTransmutation(name, outState, starlight, constellationKey, transmutation -> {
            //TODO_PORT[AS-INT-06]: 1.16.5 body (1.16.5 BlockTransmutationManager.java:55):
            //   transmutation.addInputOption(new BlockMatchInformation(input, exact));
            // common/util/block/BlockMatchInformation(BlockState, boolean) exists, but
            // BlockTransmutation#addInputOption expects the nested BlockTransmutation.BlockMatchInformation
            // (private constructor; only read(JsonObject)/read(FriendlyByteBuf) are public, and neither
            // carries the "exact" flag) in this tree - see externalChangesNeeded.
        });
    }

    private void addTransmutation(String name, BlockState outState, double starlight,
                                  @Nullable ResourceLocation constellationKey, Consumer<BlockTransmutation> addInputRequirements) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5: name = fixRecipeName(name);
        IWeakConstellation weakConstellation = null;
        if (constellationKey != null) {
            if (!RegistriesAS.CONSTELLATIONS.containsKey(constellationKey)) {
                throw new IllegalArgumentException("Invalid constellation key: \"" + constellationKey + "\"");
            }
            IConstellation constellation = RegistriesAS.CONSTELLATIONS.getValue(constellationKey);
            if (constellation instanceof IWeakConstellation) {
                weakConstellation = (IWeakConstellation) constellation;
            } else {
                throw new IllegalArgumentException("Constellation: \"" + constellationKey + "\" is not a weak constellation!");
            }
        }
        BlockTransmutation transmutation = new BlockTransmutation(new ResourceLocation(name), outState, starlight, weakConstellation);
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 (1.16.5 BlockTransmutationManager.java:75):
        //   CraftTweakerAPI.apply(new ActionAddRecipe(this, transmutation));
        // Note: 1.16.5 never invoked addInputRequirements in this method either - the consumer handed
        // in by both addRecipe overloads is dead there as well. That is reproduced rather than
        // "fixed", so the recipe is registered without input options exactly like upstream.
    }

    // There is currently an issue with optional booleans, so this is the work around
    /**
     * 1.16.5 @ZenCodeType.Method
     */
    public void removeRecipe(BlockState outputState) {
        this.removeRecipe(outputState, false);
    }

    /**
     * 1.16.5 @ZenCodeType.Method
     */
    public void removeRecipe(BlockState outputState, boolean exact) {
        BlockMatchInformation matcher = new BlockMatchInformation(outputState, exact);
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 body (1.16.5 BlockTransmutationManager.java:87-93):
        //   CraftTweakerAPI.apply(new ActionRemoveRecipe(this, iRecipe -> {
        //       if(iRecipe instanceof BlockTransmutation) {
        //           BlockTransmutation recipe = (BlockTransmutation) iRecipe;
        //           return matcher.test(recipe.getOutput());
        //       }
        //       return false;
        //   }, action -> "Removing Block Transmutation recipes that output " + ExpandBlockState.getCommandString(outputState)));
        // The predicate itself is portable as written - common/util/block/BlockMatchInformation
        // implements Predicate<BlockState> and BlockTransmutation#getOutput returns the BlockState -
        // only ActionRemoveRecipe/ExpandBlockState are missing.
    }

    /**
     * 1.16.5 @Override of IRecipeManager#removeRecipe(IItemStack)
     */
    public void removeRecipe(Object output) {
        throw new UnsupportedOperationException("Cannot remove Astral Sorcery Block Transmutation recipes by IItemStacks, use the BlockState method instead!");
    }

    /**
     * 1.16.5 @Override of IRecipeManager#getRecipeType, which returned IRecipeType<BlockTransmutation>
     * via RecipeTypesAS.TYPE_BLOCK_TRANSMUTATION.getType().
     */
    public RecipeType<BlockTransmutation> getRecipeType() {
        return RecipeTypesAS.BLOCK_TRANSMUTATION.get();
    }
}
