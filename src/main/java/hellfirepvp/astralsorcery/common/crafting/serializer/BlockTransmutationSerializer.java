/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
//1.16.5 imported hellfirepvp.astralsorcery.common.util.block.BlockMatchInformation here. This tree has to
//use the nested BlockTransmutation.BlockMatchInformation, a thin subclass of that very class - see the
//TODO_PORT[AS-ALT-06] note on the nested type for why it still exists. Every use below is unchanged.
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation.BlockMatchInformation;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.util.block.BlockStateHelper;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTransmutationSerializer
 * Created by HellFirePvP
 * Date: 10.10.2019 / 19:27
 */
public class BlockTransmutationSerializer extends CustomRecipeSerializer<BlockTransmutation> {

    //1.16.5 held the single instance in common/lib/RecipeSerializersAS as BLOCK_TRANSMUTATION_SERIALIZER.
    //common/lib is not owned by this slice, and this tree's common/lib/RecipeSerializersAS binds the
    //"astralsorcery:block_transmutation" id to the nested BlockTransmutation.Serializer shell, which forwards
    //to this instance - so there is still exactly one implementation of the format.
    //TODO_PORT[AS-ALT-05]: move to common/lib/RecipeSerializersAS#BLOCK_TRANSMUTATION_SERIALIZER and register
    // this instance directly instead of through the nested shell.
    public static final BlockTransmutationSerializer INSTANCE = new BlockTransmutationSerializer();

    public BlockTransmutationSerializer() {
        super(RecipeSerializersAS.BLOCK_TRANSMUTATION.getId());
    }

    @Override
    public BlockTransmutation fromJson(ResourceLocation recipeId, JsonObject json) {
        List<BlockMatchInformation> matchInformation = new ArrayList<>();
        JsonHelper.parseMultipleJsonObjects(json, "input", object -> matchInformation.add(BlockMatchInformation.read(object)));
        if (matchInformation.isEmpty()) {
            throw new IllegalArgumentException("A block transmutation has to have at least 1 input!");
        }
        for (BlockMatchInformation info : matchInformation) {
            if (!info.isValid()) {
                throw new JsonSyntaxException("Block transmutation must not convert an air-block into something!");
            }
        }

        //1.16.5: JSONUtils.getJsonObject -> GsonHelper.getAsJsonObject in 1.20.1
        BlockState output = BlockStateHelper.deserializeObject(GsonHelper.getAsJsonObject(json, "output"));
        ItemStack outputDisplay = new ItemStack(output.getBlock());
        //1.16.5: JSONUtils.hasField -> GsonHelper.isValidNode in 1.20.1
        if (GsonHelper.isValidNode(json, "display")) {
            outputDisplay = JsonHelper.getItemStack(json, "display");
        }
        //1.16.5: JSONUtils.getFloat -> GsonHelper.getAsFloat in 1.20.1
        float starlight = GsonHelper.getAsFloat(json, "starlight");

        IWeakConstellation matchConstellation = null;
        if (json.has("constellation")) {
            //1.16.5: new ResourceLocation(String) - deprecated in this Forge version, ResourceLocation.parse
            //is the replacement and throws the very same ResourceLocationException on a malformed id.
            ResourceLocation cstKey = ResourceLocation.parse(GsonHelper.getAsString(json, "constellation"));
            IConstellation cst = RegistriesAS.CONSTELLATIONS.getValue(cstKey);
            if (cst == null) {
                throw new JsonSyntaxException(String.format("Unknown constellation %s!", cstKey.toString()));
            }
            if (!(cst instanceof IWeakConstellation)) {
                throw new JsonSyntaxException(String.format("Constellation %s has to be either a major or dim constellation!", cstKey.toString()));
            }
            matchConstellation = (IWeakConstellation) cst;
        }
        BlockTransmutation tr = new BlockTransmutation(recipeId, output, starlight, matchConstellation);
        matchInformation.forEach(tr::addInputOption);
        tr.setOutputDisplay(outputDisplay);
        return tr;
    }

    @Nullable
    @Override
    public BlockTransmutation fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        List<BlockMatchInformation> matchInformation = ByteBufUtils.readList(buffer, BlockMatchInformation::read);
        BlockState output = ByteBufUtils.readBlockState(buffer);
        ItemStack display = ByteBufUtils.readItemStack(buffer);
        double starlight = buffer.readDouble();
        //1.16.5 used ByteBufUtils.readRegistryEntry, which resolved the value through
        //GameRegistry.findRegistry(IForgeRegistryEntry#getRegistryType). IForgeRegistryEntry is gone in
        //1.20.1, so the registry name is written/read explicitly and resolved against the same registry the
        //value came from - identical wire content (a ResourceLocation) and identical result.
        IWeakConstellation cst = ByteBufUtils.readOptional(buffer,
                buf -> (IWeakConstellation) RegistriesAS.CONSTELLATIONS.getValue(ByteBufUtils.readResourceLocation(buf)));
        BlockTransmutation tr = new BlockTransmutation(recipeId, output, starlight, cst);
        matchInformation.forEach(tr::addInputOption);
        tr.setOutputDisplay(display);
        return tr;
    }

    @Override
    public void write(JsonObject object, BlockTransmutation recipe) {
        JsonArray inputs = new JsonArray();
        for (BlockMatchInformation info : recipe.getInputOptions()) {
            inputs.add(info.serializeJson());
        }
        object.add("input", inputs);

        object.add("output", BlockStateHelper.serializeObject(recipe.getOutput(), true));
        object.add("display", JsonHelper.serializeItemStack(recipe.getOutputDisplay()));
        object.addProperty("starlight", recipe.getStarlightRequired());

        if (recipe.getRequiredConstellation() != null) {
            object.addProperty("constellation", recipe.getRequiredConstellation().getRegistryName().toString());
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, BlockTransmutation recipe) {
        ByteBufUtils.writeCollection(buffer, recipe.getInputOptions(), (buf, match) -> match.serialize(buf));
        ByteBufUtils.writeBlockState(buffer, recipe.getOutput());
        ByteBufUtils.writeItemStack(buffer, recipe.getOutputDisplay());
        buffer.writeDouble(recipe.getStarlightRequired());
        //See fromNetwork(): the 1.16.5 ByteBufUtils.writeRegistryEntry is replaced by an explicit registry name.
        ByteBufUtils.writeOptional(buffer, recipe.getRequiredConstellation(),
                (buf, cst) -> ByteBufUtils.writeResourceLocation(buf, cst.getRegistryName()));
    }
}
