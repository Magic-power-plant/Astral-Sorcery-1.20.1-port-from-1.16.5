package hellfirepvp.astralsorcery.common.crafting.helper;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public abstract class CustomRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {

    private ResourceLocation registryName;

    public CustomRecipeSerializer(ResourceLocation name) {
        this.registryName = name;
    }

    public void setRegistryName(ResourceLocation name) {
        this.registryName = name;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public abstract void write(JsonObject object, T recipe);
}
