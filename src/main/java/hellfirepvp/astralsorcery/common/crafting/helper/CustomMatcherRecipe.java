package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

public abstract class CustomMatcherRecipe extends BaseHandlerRecipe<IItemHandler> {

    protected CustomMatcherRecipe(ResourceLocation recipeId) {
        super(recipeId);
    }

    @Override
    public final boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public final boolean matches(IItemHandler handler, Level world) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public abstract CustomRecipeSerializer<?> getSerializer();
}
