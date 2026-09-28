package hellfirepvp.astralsorcery.common.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.LogicalSide;

import java.util.Optional;

public class SidedReference<T> {

    private T clientData = null;
    private T serverData = null;

    public Optional<T> getData(Dist side) {
        if (side.isClient()) {
            return Optional.ofNullable(this.clientData);
        }
        return Optional.ofNullable(this.serverData);
    }

    public Optional<T> getData(LogicalSide side) {
        return this.getData(side.isClient() ? Dist.CLIENT : Dist.DEDICATED_SERVER);
    }

    public void setData(Dist side, T data) {
        if (side.isClient()) {
            this.clientData = data;
        } else {
            this.serverData = data;
        }
    }

    public void setData(LogicalSide side, T data) {
        this.setData(side.isClient() ? Dist.CLIENT : Dist.DEDICATED_SERVER, data);
    }

}
