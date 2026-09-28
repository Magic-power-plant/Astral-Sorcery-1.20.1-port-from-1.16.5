package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

public class DefaultWrapperContext extends RecipeCraftingContext {

    private final IItemHandler handler;
    private final Level world;

    public DefaultWrapperContext(IItemHandler handler, Level world) {
        this.handler = handler;
        this.world = world;
    }

    public IItemHandler getHandler() {
        return handler;
    }

    public Level getWorld() {
        return world;
    }
}
