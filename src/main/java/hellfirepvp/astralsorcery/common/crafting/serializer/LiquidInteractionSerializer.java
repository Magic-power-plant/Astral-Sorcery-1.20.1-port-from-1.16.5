/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.serializer;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LiquidInteractionSerializer
 * Created by HellFirePvP
 * Date: 29.10.2020 / 20:45
 */
public class LiquidInteractionSerializer extends CustomRecipeSerializer<LiquidInteraction> {

    //1.16.5 held the single instance in common/lib/RecipeSerializersAS as LIQUID_INTERACTION_SERIALIZER.
    //common/lib is not owned by this slice, and this tree's common/lib/RecipeSerializersAS binds the
    //"astralsorcery:liquid_interaction" id to the nested LiquidInteraction.Serializer shell, which forwards
    //to this instance - so there is still exactly one implementation of the format.
    //TODO_PORT[AS-ALT-05]: move to common/lib/RecipeSerializersAS#LIQUID_INTERACTION_SERIALIZER and register
    // this instance directly instead of through the nested shell.
    public static final LiquidInteractionSerializer INSTANCE = new LiquidInteractionSerializer();

    public LiquidInteractionSerializer() {
        super(RecipeSerializersAS.LIQUID_INTERACTION.getId());
    }

    //1.16.5: CustomRecipeSerializer#read(ResourceLocation, JsonObject) - vanilla renamed the recipe
    //serializer entry points to fromJson/fromNetwork/toNetwork in 1.20.1.
    @Override
    public LiquidInteraction fromJson(ResourceLocation recipeId, JsonObject json) {
        return LiquidInteraction.read(recipeId, json);
    }

    @Override
    public void write(JsonObject object, LiquidInteraction recipe) {
        recipe.write(object);
    }

    @Nullable
    @Override
    public LiquidInteraction fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return LiquidInteraction.read(recipeId, buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, LiquidInteraction recipe) {
        recipe.write(buffer);
    }
}
