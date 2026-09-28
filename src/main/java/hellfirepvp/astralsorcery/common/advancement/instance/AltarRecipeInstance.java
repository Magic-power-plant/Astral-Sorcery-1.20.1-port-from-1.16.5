package hellfirepvp.astralsorcery.common.advancement.instance;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.advancement.AltarCraftTrigger;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.tags.TagKey;

import java.util.*;
import java.util.stream.Collectors;

public class AltarRecipeInstance extends AbstractCriterionTriggerInstance {

    private final Set<ResourceLocation> recipeNames = new HashSet<>();
    private final List<Ingredient> recipeOutputs = new ArrayList<>();

    private AltarRecipeInstance(ResourceLocation id) {
        super(id, ContextAwarePredicate.ANY);
    }

    public AltarRecipeInstance(ResourceLocation id, ContextAwarePredicate predicate) {
        super(id, predicate);
    }

    public static AltarRecipeInstance craftRecipe(ResourceLocation... recipeIds) {
        AltarRecipeInstance instance = new AltarRecipeInstance(AltarCraftTrigger.ID);
        instance.recipeNames.addAll(Arrays.asList(recipeIds));
        return instance;
    }

    public static AltarRecipeInstance craftRecipe(AltarRecipe... recipes) {
        AltarRecipeInstance instance = new AltarRecipeInstance(AltarCraftTrigger.ID);
        Arrays.asList(recipes).forEach(recipe -> instance.recipeNames.add(recipe.getId()));
        return instance;
    }

    public static AltarRecipeInstance withOutput(ItemLike... outputs) {
        return withOutput(Ingredient.of(outputs));
    }

    public static AltarRecipeInstance withOutput(ItemStack... outputs) {
        return withOutput(Ingredient.of(outputs));
    }

    public static AltarRecipeInstance withOutput(TagKey<Item>... outputs) {
        return withOutput(Arrays.stream(outputs).map(Ingredient::of).collect(Collectors.toList()));
    }

    public static AltarRecipeInstance withOutput(Ingredient... outputs) {
        return withOutput(Arrays.asList(outputs));
    }

    public static AltarRecipeInstance withOutput(List<Ingredient> outputs) {
        AltarRecipeInstance instance = new AltarRecipeInstance(AltarCraftTrigger.ID);
        instance.recipeOutputs.addAll(outputs);
        return instance;
    }

    @Override
    public JsonObject serializeToJson(SerializationContext conditions) {
        JsonObject out = super.serializeToJson(conditions);
        if (!this.recipeNames.isEmpty()) {
            JsonArray names = new JsonArray();
            for (ResourceLocation name : this.recipeNames) {
                names.add(name.toString());
            }
            out.add("recipeNames", names);
        }
        if (!this.recipeOutputs.isEmpty()) {
            JsonArray outputs = new JsonArray();
            for (Ingredient output : this.recipeOutputs) {
                outputs.add(output.toJson());
            }
            out.add("recipeOutputs", outputs);
        }
        return out;
    }

    public static AltarRecipeInstance deserialize(ResourceLocation id, JsonObject json) {
        AltarRecipeInstance instance = new AltarRecipeInstance(id);
        JsonArray recipeNames = GsonHelper.getAsJsonArray(json, "recipeNames", new JsonArray());
        for (int idx = 0; idx < recipeNames.size(); idx++) {
            JsonElement element = recipeNames.get(idx);
            String key = element.getAsString();
            instance.recipeNames.add(ResourceLocation.parse(key));
        }
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "recipeOutputs", new JsonArray())) {
            instance.recipeOutputs.add(Ingredient.fromJson(element));
        }
        return instance;
    }

    public boolean test(AltarRecipe recipe, ItemStack output) {
        if (this.recipeNames.isEmpty() && this.recipeOutputs.isEmpty()) {
            return true;
        }
        ResourceLocation recipeName = recipe.getId();
        if (this.recipeNames.contains(recipeName)) {
            return true;
        }
        for (Ingredient i : this.recipeOutputs) {
            if (i.test(output)) {
                return true;
            }
        }
        return false;
    }
}
