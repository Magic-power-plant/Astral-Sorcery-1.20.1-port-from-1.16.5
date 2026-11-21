package hellfirepvp.astralsorcery.api.world;

import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import java.util.Collection;

/** Lookup view for registered world recipes. */
public interface IWorldRecipeRegistry<T extends IWorldRecipe> {
    @Nullable T get(ResourceLocation key);
    Collection<T> getAll();
}
