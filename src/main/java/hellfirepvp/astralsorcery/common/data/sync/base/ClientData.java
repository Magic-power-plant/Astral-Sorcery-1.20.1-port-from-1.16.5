package hellfirepvp.astralsorcery.common.data.sync.base;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class ClientData<C extends ClientData<C>> {

    public abstract void clear(ResourceKey<Level> dim);

    public abstract void clearClient();

}
