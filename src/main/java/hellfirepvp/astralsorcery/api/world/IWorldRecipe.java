package hellfirepvp.astralsorcery.api.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.function.Consumer;

/** A keyed recipe that matches and processes a position in a world. */
public interface IWorldRecipe {
    ResourceLocation getKey();
    boolean matches(Level level, BlockPos pos);
    void process(Level level, BlockPos pos, Consumer<ItemStack> itemOutput);
}
