package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

public interface IHandlerRecipe<I extends IItemHandler> extends Recipe<Container> {

    boolean matches(I handler, Level world);

    @Override
    default boolean matches(Container inv, Level worldIn) {
        return false;
    }
}
