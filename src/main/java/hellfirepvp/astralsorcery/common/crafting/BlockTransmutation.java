/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.recipe.GatedRecipe;
import hellfirepvp.astralsorcery.common.crafting.serializer.BlockTransmutationSerializer;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.util.block.BlockStateHelper;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import hellfirepvp.astralsorcery.common.util.object.PredicateBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTransmutation
 * Created by HellFirePvP
 * Date: 10.10.2019 / 19:27
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe. Not moved because the package layout is
//a tree-wide decision.
//TODO_PORT[AS-ALT-05]: 1.16.5 extends common/crafting/helper/CustomMatcherRecipe (which supplied getId,
//getType, getSerializer, canFit and the empty crafting result) and returns
//RecipeTypesAS.TYPE_BLOCK_TRANSMUTATION.getType() / RecipeSerializersAS.BLOCK_TRANSMUTATION_SERIALIZER.
//This tree implements vanilla Recipe<Container> directly and resolves both through the DeferredRegister
//holders, because RecipeTypesAS here holds vanilla RecipeTypes rather than 1.16.5's ResolvingRecipeType. The
//members with no 1.16.5 counterpart (matches(Container,Level), assemble, canCraftInDimensions,
//getResultItem, getId and the nested Serializer shell) are the glue that replaces it.
public class BlockTransmutation implements Recipe<Container>, GatedRecipe.Progression {

    private final ResourceLocation id;
    private final BlockState outputState;
    private final double starlight;
    @Nullable
    private final IWeakConstellation constellation;

    private final List<BlockMatchInformation> stateCheck = new ArrayList<>();
    private ItemStack outputDisplay;
    private Predicate<BlockState> matcher = null;

    public BlockTransmutation(ResourceLocation id, BlockState outputState, double starlight) {
        this(id, outputState, starlight, null);
    }

    public BlockTransmutation(ResourceLocation id, BlockState outputState, double starlight, @Nullable IWeakConstellation constellation) {
        this.id = id;
        this.outputState = outputState;
        this.starlight = starlight;
        this.constellation = constellation;
        this.outputDisplay = new ItemStack(outputState.getBlock());
    }

    @Nonnull
    @Override
    public ResearchProgression getRequiredProgression() {
        return ResearchProgression.ATTUNEMENT;
    }

    //1.16.5 signature: matches(IWorld world, BlockPos pos, BlockState state, IWeakConstellation constellation)
    //(1.16.5 common/crafting/recipe/BlockTransmutation.java:68). Neither the world nor the position were read
    //by the 1.16.5 body, and this tree's caller (common/util/block/BlockTransmutationHandler) only has the
    //state and the focused constellation at hand, so the two unused parameters are not carried over.
    public boolean matches(BlockState state, @Nullable IWeakConstellation constellation) {
        if (this.matcher == null) {
            this.matcher = PredicateBuilder.joinOr(this.stateCheck);
        }
        return this.matcher.test(state) && (this.constellation == null || this.constellation.equals(constellation));
    }

    public void addInputOption(BlockMatchInformation test) {
        this.matcher = null;
        this.stateCheck.add(test);
    }

    public List<BlockMatchInformation> getInputOptions() {
        return this.stateCheck;
    }

    public List<ItemStack> getInputDisplay() {
        return this.getInputOptions().stream()
                .map(BlockMatchInformation::getDisplayStack)
                .collect(Collectors.toList());
    }

    @Nonnull
    public BlockState getOutput() {
        return outputState;
    }

    public void setOutputDisplay(@Nonnull ItemStack outputDisplay) {
        this.outputDisplay = outputDisplay;
    }

    @Nonnull
    public ItemStack getOutputDisplay() {
        return outputDisplay.copy();
    }

    public double getStarlightRequired() {
        return starlight;
    }

    @Nullable
    public IWeakConstellation getRequiredConstellation() {
        return constellation;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return this.getOutputDisplay();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.BLOCK_TRANSMUTATION.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeTypesAS.BLOCK_TRANSMUTATION.get();
    }

    /**
     * 1.16.5 registered common/crafting/serializer/BlockTransmutationSerializer directly (through
     * common/lib/RecipeSerializersAS#BLOCK_TRANSMUTATION_SERIALIZER). This tree binds the
     * "astralsorcery:block_transmutation" serializer id through the DeferredRegister in
     * common/lib/RecipeSerializersAS, which needs a nested type to reference, so this shell exists and
     * forwards to the ported serializer. There is exactly one implementation of the format.
     * TODO_PORT[AS-ALT-05]: register BlockTransmutationSerializer itself and drop this shell once
     * common/lib/RecipeSerializersAS exists.
     */
    public static class Serializer implements RecipeSerializer<BlockTransmutation> {

        @Override
        public BlockTransmutation fromJson(ResourceLocation id, JsonObject json) {
            return BlockTransmutationSerializer.INSTANCE.fromJson(id, json);
        }

        @Override
        public BlockTransmutation fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return BlockTransmutationSerializer.INSTANCE.fromNetwork(id, buffer);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BlockTransmutation recipe) {
            BlockTransmutationSerializer.INSTANCE.toNetwork(buffer, recipe);
        }
    }

    /**
     * 1.16.5 kept this at common/util/block/BlockMatchInformation and this tree already carries a complete
     * port of it there. It cannot simply be swapped in because
     * client/screen/journal/page/RenderPageBlockTransmutation (outside this slice) method-references
     * {@code BlockTransmutation.BlockMatchInformation::getDisplayStack} against the list returned by
     * {@link #getInputOptions()}, so the nested type has to keep existing under this name. It is therefore a
     * thin subclass of the real thing: every matching/serialising behaviour is inherited unchanged
     * ({@code isValid()}, {@code test(BlockState)}, {@code getDisplayStack()}, {@code serializeJson()},
     * {@code serialize(FriendlyByteBuf)}), only the two {@code read} factories are restated so that they
     * hand back this subtype.
     * TODO_PORT[AS-ALT-06]: delete this class and use common/util/block/BlockMatchInformation directly once
     * client/screen/journal/page/RenderPageBlockTransmutation can be re-pointed at it (see
     * externalChangesNeeded).
     * AS-ALT-07 (resolved at runtime): recipe loading happens BEFORE block tags are bound on 1.20.1 - the
     * dedicated server logged "Parsing error loading recipe astralsorcery:block_transmutation/iron_starmetal:
     * unable to create valid itemstack from block tag forge:ores/iron!" and dropped the recipe. The base
     * BlockMatchInformation now resolves the display stack lazily instead of at construction time, matching how
     * vanilla's Ingredient.TagValue defers its own tag lookup.
     */
    public static class BlockMatchInformation extends hellfirepvp.astralsorcery.common.util.block.BlockMatchInformation {

        public BlockMatchInformation(TagKey<Block> matchTag) {
            super(matchTag);
        }

        public BlockMatchInformation(TagKey<Block> matchTag, ItemStack display) {
            super(matchTag, display);
        }

        public BlockMatchInformation(BlockState matchState, boolean matchExact) {
            super(matchState, matchExact);
        }

        public BlockMatchInformation(BlockState matchState, ItemStack display, boolean matchExact) {
            super(matchState, display, matchExact);
        }

        public static BlockMatchInformation read(JsonObject object) {
            if (object.has("block")) {
                BlockState state = BlockStateHelper.deserializeObject(object);
                boolean fullyDefined = !BlockStateHelper.isMissingStateInformation(object);
                ItemStack display = new ItemStack(state.getBlock());
                if (object.has("display")) {
                    display = JsonHelper.getItemStack(object, "display");
                }
                return new BlockMatchInformation(state, display, fullyDefined);
            } else if (object.has("tag")) {
                TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(object.get("tag").getAsString()));
                if (object.has("display")) {
                    ItemStack display = JsonHelper.getItemStack(object, "display");
                    return new BlockMatchInformation(blockTag, display);
                }
                return new BlockMatchInformation(blockTag);
            }
            throw new JsonSyntaxException("Neither block nor tag defined for block transmutation match information!");
        }

        public static BlockMatchInformation read(FriendlyByteBuf buf) {
            int type = buf.readInt();
            ItemStack display = ByteBufUtils.readItemStack(buf);
            switch (type) {
                case 0: {
                    BlockState state = ByteBufUtils.readBlockState(buf);
                    boolean exactMatch = buf.readBoolean();
                    return new BlockMatchInformation(state, display, exactMatch);
                }
                case 1: {
                    String tagName = ByteBufUtils.readString(buf);
                    TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(tagName));
                    return new BlockMatchInformation(blockTag, display);
                }
            }
            throw new IllegalArgumentException("Unknown block transmutation match type: " + type);
        }
    }
}
