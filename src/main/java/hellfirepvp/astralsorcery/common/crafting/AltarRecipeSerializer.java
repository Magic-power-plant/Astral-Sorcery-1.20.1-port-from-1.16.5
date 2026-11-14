/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleAltarRecipeSerializer
 * Created by HellFirePvP
 * Date: 12.08.2019 / 19:45
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 calls this common/crafting/serializer/SimpleAltarRecipeSerializer. Not renamed
//or moved because the package layout is a tree-wide decision and common/crafting/RecipeSerializersAS binds
//the "astralsorcery:altar" id to this class by name.
//TODO_PORT[AS-ALT-03]: this is the serializer of the divergent live common/crafting/AltarRecipe; the
//reference-faithful serializer of common/crafting/recipe/SimpleAltarRecipe is fully ported next to it as
//common/crafting/serializer/SimpleAltarRecipeSerializer (unregistered - see the marker there). 1.16.5
//extends common/crafting/helper/CustomRecipeSerializer, which supplied the registry name and the
//write(JsonObject, R) half of the contract; this tree implements vanilla RecipeSerializer directly because
//it is what the DeferredRegister needs, so there is no JSON-write half here (1.20.1's RecipeSerializer has
//no toJson). Retiring AltarRecipe in favour of SimpleAltarRecipe retires this class with it.
public class AltarRecipeSerializer implements RecipeSerializer<AltarRecipe> {

    @Override
    public AltarRecipe fromJson(ResourceLocation id, JsonObject json) {
        AltarType altarType = readAltarType(json);
        //1.16.5 read "duration" and "starlight" as mandatory ints
        //(1.16.5 common/crafting/serializer/SimpleAltarRecipeSerializer.java:49-50). The alternative keys and
        //the defaults below have no 1.16.5 counterpart; they are kept because every shipped altar recipe json
        //carries the reference keys anyway, so they only ever apply to third-party datapacks.
        int starlight = GsonHelper.getAsInt(json, "starlight", GsonHelper.getAsInt(json, "starlightRequired", 0));
        int craftingTime = GsonHelper.getAsInt(json, "duration", GsonHelper.getAsInt(json, "craftingTime", altarType.getDefaultAltarCraftingDuration()));
        //1.16.5 called grid.validate(type) right after deserialize(..); this tree's AltarRecipeGrid#deserialize
        //ends with that very call (common/crafting/AltarRecipeGrid.java:301), so it is not repeated here.
        AltarRecipeGrid grid = AltarRecipeGrid.deserialize(altarType, json);
        List<ItemStack> outputs = readOutputs(json);
        AltarRecipe recipe = new AltarRecipe(id, altarType, grid, outputs, starlight, craftingTime);
        if (json.has("recipe_class")) {
            ResourceLocation customType = ResourceLocation.tryParse(GsonHelper.getAsString(json, "recipe_class"));
            if (customType != null) {
                recipe = AltarRecipeTypeHandler.convert(recipe, customType);
                recipe.setCustomRecipeType(customType);
            }
        }
        if (json.has("focus_constellation")) {
            ResourceLocation focusId = ResourceLocation.tryParse(GsonHelper.getAsString(json, "focus_constellation"));
            IConstellation focus = ConstellationRegistry.getConstellation(focusId);
            if (focus == null) {
                throw new JsonSyntaxException("Unknown constellation " + focusId + " for altar recipe " + id);
            }
            recipe.setFocusConstellation(focus);
        }
        if (json.has("relay_inputs")) {
            JsonArray relayInputs = GsonHelper.getAsJsonArray(json, "relay_inputs");
            for (int i = 0; i < relayInputs.size(); i++) {
                JsonElement element = relayInputs.get(i);
                //1.16.5 used a plain Ingredient.deserialize here; this tree wraps the element through
                //WrappedIngredient#fromJson so that astralsorcery fluid ingredients keep working. The
                //empty-ingredient guard below is 1.16.5's
                //(1.16.5 common/crafting/serializer/SimpleAltarRecipeSerializer.java:90-97) and was missing:
                //an unresolvable relay tag was silently added as a relay input that can never be satisfied.
                //Ingredient#hasNoMatchingItems is Ingredient#isEmpty in 1.20.1 and FluidIngredient overrides
                //it to report on its fluid list, so fluid relay inputs are unaffected.
                WrappedIngredient ingredient = WrappedIngredient.fromJson(element);
                if (!ingredient.getIngredient().isEmpty()) {
                    recipe.addRelayInput(ingredient);
                } else {
                    AstralSorcery.LOG.warn("Skipping relay_inputs[" + i + "] for recipe " + id + " as the ingredient has no matching items!");
                    //1.16.5: JSONUtils.toString(JsonElement) - renamed to GsonHelper.getType(JsonElement)
                    AstralSorcery.LOG.warn("Ingredient skipped: " + GsonHelper.getType(element));
                }
            }
        }
        if (json.has("options")) {
            recipe.deserializeAdditionalJson(GsonHelper.getAsJsonObject(json, "options"));
        } else {
            recipe.deserializeAdditionalJson(new JsonObject());
        }
        //The "effects" block was not read at all, so every shipped altar recipe json silently lost its
        //crafting effects - data/astralsorcery/recipes/altar/*.json declares them on almost every recipe and
        //client/render/tile/RenderAltar reads them back off the live recipe
        //(1.16.5 common/crafting/serializer/SimpleAltarRecipeSerializer.java:100-111).
        if (json.has("effects")) {
            JsonArray effectNames = GsonHelper.getAsJsonArray(json, "effects");
            for (int i = 0; i < effectNames.size(); i++) {
                JsonElement element = effectNames.get(i);
                ResourceLocation effectKey = ResourceLocation.parse(GsonHelper.convertToString(element, "effects[" + i + "]"));
                AltarRecipeEffect effect = RegistriesAS.REGISTRY_ALTAR_EFFECTS.getValue(effectKey);
                if (effect == null) {
                    throw new JsonSyntaxException("No altar effect for name " + effectKey + "! (Found at: effects[" + i + "])");
                }
                recipe.addAltarEffect(effect);
            }
        }
        return recipe;
    }

    @Override
    public AltarRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        AltarType altarType = buffer.readEnum(AltarType.class);
        int starlight = buffer.readVarInt();
        int craftingTime = buffer.readVarInt();
        AltarRecipeGrid grid = AltarRecipeGrid.read(buffer);
        int outputCount = buffer.readVarInt();
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < outputCount; i++) {
            outputs.add(buffer.readItem());
        }
        AltarRecipe recipe = new AltarRecipe(id, altarType, grid, outputs, starlight, craftingTime);
        if (buffer.readBoolean()) {
            ResourceLocation customType = buffer.readResourceLocation();
            recipe = AltarRecipeTypeHandler.convert(recipe, customType);
            recipe.setCustomRecipeType(customType);
        }
        if (buffer.readBoolean()) {
            recipe.setFocusConstellation(ConstellationRegistry.getConstellation(buffer.readResourceLocation()));
        }
        int relayInputCount = buffer.readVarInt();
        for (int i = 0; i < relayInputCount; i++) {
            recipe.addRelayInput(WrappedIngredient.read(buffer));
        }
        //1.16.5 syncs the crafting effects with the recipe
        //(1.16.5 common/crafting/recipe/SimpleAltarRecipe.java:243-246); they were missing from this stream,
        //so the client - which only ever sees recipes through this path - rendered no crafting effects at
        //all. 1.16.5 wrote them through ByteBufUtils.write/readRegistryEntry, which is gone with
        //IForgeRegistryEntry in 1.20.1, so the registry name goes over the wire explicitly and is resolved
        //against the registry the value came from: identical wire content, identical result.
        int effectCount = buffer.readVarInt();
        for (int i = 0; i < effectCount; i++) {
            AltarRecipeEffect effect = RegistriesAS.REGISTRY_ALTAR_EFFECTS.getValue(buffer.readResourceLocation());
            if (effect != null) {
                recipe.addAltarEffect(effect);
            }
        }
        recipe.readRecipeSync(buffer);
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AltarRecipe recipe) {
        buffer.writeEnum(recipe.getAltarType());
        buffer.writeVarInt(recipe.getStarlightRequired());
        buffer.writeVarInt(recipe.getCraftingTime());
        recipe.getGrid().write(buffer);
        buffer.writeVarInt(recipe.getOutputs().size());
        for (ItemStack output : recipe.getOutputs()) {
            buffer.writeItem(output);
        }
        ResourceLocation customType = recipe.getCustomRecipeType();
        buffer.writeBoolean(customType != null);
        if (customType != null) {
            buffer.writeResourceLocation(customType);
        }
        IConstellation focus = recipe.getFocusConstellation();
        buffer.writeBoolean(focus != null);
        if (focus != null) {
            buffer.writeResourceLocation(focus.getRegistryName());
        }
        buffer.writeVarInt(recipe.getRelayInputs().size());
        for (WrappedIngredient input : recipe.getRelayInputs()) {
            input.write(buffer);
        }
        //See fromNetwork(): the 1.16.5 ByteBufUtils.writeRegistryEntry is replaced by an explicit registry name.
        buffer.writeVarInt(recipe.getCraftingEffects().size());
        for (AltarRecipeEffect effect : recipe.getCraftingEffects()) {
            buffer.writeResourceLocation(effect.getRegistryName());
        }
        recipe.writeRecipeSync(buffer);
    }

    //1.16.5: MiscUtils.getEnumEntry(AltarType.class, JSONUtils.getInt(json, "altar_type")) - "altar_type" was
    //mandatory there. The "altarType" name fallback and the DISCOVERY default have no 1.16.5 counterpart;
    //every shipped altar recipe json declares "altar_type", so they only ever apply to third-party datapacks.
    private static AltarType readAltarType(JsonObject json) {
        if (json.has("altar_type")) {
            int ordinal = GsonHelper.getAsInt(json, "altar_type");
            AltarType[] values = AltarType.values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
        }
        if (json.has("altarType")) {
            return AltarType.valueOf(GsonHelper.getAsString(json, "altarType"));
        }
        return AltarType.DISCOVERY;
    }

    private static List<ItemStack> readOutputs(JsonObject json) {
        List<ItemStack> outputs = new ArrayList<>();
        if (GsonHelper.isArrayNode(json, "output")) {
            JsonArray outputArray = GsonHelper.getAsJsonArray(json, "output");
            for (int i = 0; i < outputArray.size(); i++) {
                outputs.add(JsonHelper.getItemStack(outputArray.get(i), String.format("output[%s]", i)));
            }
        } else {
            outputs.add(JsonHelper.getItemStack(json, "output"));
        }
        return outputs;
    }
}
