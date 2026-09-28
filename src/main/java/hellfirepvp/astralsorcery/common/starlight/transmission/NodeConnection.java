package hellfirepvp.astralsorcery.common.starlight.transmission;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public class NodeConnection<T extends IPrismTransmissionNode> {

    private final T node;
    private final BlockPos to;
    private final boolean canConnect;

    public NodeConnection(@Nullable T node, BlockPos to, boolean canConnect) {
        this.node = node;
        this.to = to;
        this.canConnect = canConnect;
    }

    public BlockPos getTo() {
        return to;
    }

    @Nullable
    public T getNode() {
        return node;
    }

    public boolean canConnect() {
        return canConnect;
    }
}
