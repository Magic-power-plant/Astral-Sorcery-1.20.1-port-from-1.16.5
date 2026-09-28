package hellfirepvp.astralsorcery.common.starlight.transmission;

import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;

import java.util.LinkedList;
import java.util.List;

public interface ITransmissionNode extends IPrismTransmissionNode {

    NodeConnection<IPrismTransmissionNode> queryNextNode(WorldNetworkHandler handler);

    @Override
    default List<NodeConnection<IPrismTransmissionNode>> queryNext(WorldNetworkHandler handler) {
        List<NodeConnection<IPrismTransmissionNode>> nodes = new LinkedList<>();
        NodeConnection<IPrismTransmissionNode> next = queryNextNode(handler);
        if (next != null) {
            nodes.add(next);
        }
        return nodes;
    }
}
