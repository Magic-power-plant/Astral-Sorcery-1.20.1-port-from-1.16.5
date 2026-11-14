package hellfirepvp.astralsorcery.common.starlight.transmission;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.starlight.IStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.IStarlightTransmission;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightTransmissionHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightUpdateHandler;
import hellfirepvp.astralsorcery.common.starlight.network.TransmissionWorldHandler;
import hellfirepvp.astralsorcery.common.blockentity.base.NetworkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public class TransmissionNetworkHelper {

    private static final double MAX_TRANSMISSION_DIST = 16;

    public static boolean hasTransmissionLink(IStarlightTransmission<?> tr, BlockPos end) {
        IPrismTransmissionNode node = tr.getNode();
        if (node == null) return false;
        WorldNetworkHandler handler = WorldNetworkHandler.getNetworkHandler(tr.getTrWorld());
        List<NodeConnection<IPrismTransmissionNode>> nextNodes = node.queryNext(handler);
        for (NodeConnection<IPrismTransmissionNode> nextNode : nextNodes) {
            if (nextNode.getTo().equals(end)) return true;
        }
        return false;
    }

    public static boolean canCreateTransmissionLink(IStarlightTransmission<?> tr, BlockPos end) {
        IPrismTransmissionNode node = tr.getNode();
        if (node == null) return false;
        WorldNetworkHandler handler = WorldNetworkHandler.getNetworkHandler(tr.getTrWorld());
        List<NodeConnection<IPrismTransmissionNode>> nextNodes = node.queryNext(handler);
        for (NodeConnection<IPrismTransmissionNode> nextNode : nextNodes) {
            if (nextNode.getTo().equals(end)) return false;
        }
        return tr.getTrPos().distSqr(end) <= MAX_TRANSMISSION_DIST * MAX_TRANSMISSION_DIST;
    }

    public static boolean createTransmissionLink(IStarlightTransmission<?> tr, BlockPos next) {
        IPrismTransmissionNode node = tr.getNode();
        if (node == null) {
            AstralSorcery.LOG.info("Trying to create transmission link on non-existing transmission tile! Not creating link!");
            return false;
        }
        createLink(node, tr, next);
        return true;
    }

    public static void removeTransmissionLink(IStarlightTransmission<?> tr, BlockPos next) {
        IPrismTransmissionNode node = tr.getNode();
        if (node == null) {
            return;
        }
        removeLink(node, tr, next);
    }

    private static void removeLink(IPrismTransmissionNode transmissionNode, IStarlightTransmission<?> transmission, BlockPos to) {
        WorldNetworkHandler handler = WorldNetworkHandler.getNetworkHandler(transmission.getTrWorld());
        IPrismTransmissionNode nextNode = handler.getTransmissionNode(to);
        removeLink(transmissionNode, nextNode, transmission.getTrWorld(), transmission.getTrPos(), to);
        handler.markDirty(transmission.getTrPos(), to);
    }

    private static void removeLink(IPrismTransmissionNode thisNode, IPrismTransmissionNode nextNode, Level world, BlockPos from, BlockPos to) {
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(world);
        if (nextNode != null) {
            nextNode.notifySourceUnlink(world, from);
            if (handle != null) {
                handle.notifyTransmissionNodeChange(nextNode);
            }
        }
        thisNode.notifyUnlink(world, to);
        if (handle != null) {
            handle.notifyTransmissionNodeChange(thisNode);
        }
    }

    private static void createLink(IPrismTransmissionNode transmissionNode, IStarlightTransmission<?> transmission, BlockPos to) {
        WorldNetworkHandler handler = WorldNetworkHandler.getNetworkHandler(transmission.getTrWorld());
        IPrismTransmissionNode nextNode = handler.getTransmissionNode(to);
        createLink(transmissionNode, nextNode, transmission.getTrWorld(), transmission.getTrPos(), to);
        handler.markDirty(transmission.getTrPos(), to);
    }

    private static void createLink(IPrismTransmissionNode thisNode, IPrismTransmissionNode nextNode, Level world, BlockPos from, BlockPos to) {
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(world);
        if (nextNode != null) {
            nextNode.notifySourceLink(world, from);
            if (handle != null) {
                handle.notifyTransmissionNodeChange(nextNode);
            }
        }
        thisNode.notifyLink(world, to);
        if (handle != null) {
            handle.notifyTransmissionNodeChange(thisNode);
        }
    }

    public static boolean isBlockEntityInNetwork(NetworkBlockEntity<?> blockEntityNetwork) {
        WorldNetworkHandler handler = WorldNetworkHandler.getNetworkHandler(blockEntityNetwork.getLevel());
        return handler.getTransmissionNode(blockEntityNetwork.getBlockPos()) != null;
    }

    public static void informNetworkBlockEntityPlacement(NetworkBlockEntity<?> blockEntityNetwork) {
        WorldNetworkHandler handler = WorldNetworkHandler.getNetworkHandler(blockEntityNetwork.getLevel());
        if (blockEntityNetwork instanceof IStarlightSource) {
            handler.addNewSourceBlockEntity((IStarlightSource<?>) blockEntityNetwork);
        } else if (blockEntityNetwork instanceof IStarlightTransmission) {
            handler.addTransmissionBlockEntity((IStarlightTransmission<?>) blockEntityNetwork);
        } else {
            AstralSorcery.LOG.warn("Placed a network tile that's not transmission/receiver or source! At: dim=" + blockEntityNetwork.getLevel().dimension().location() + ", pos=" + blockEntityNetwork.getBlockPos());
        }
        IPrismTransmissionNode node = handler.getTransmissionNode(blockEntityNetwork.getBlockPos());
        if (node == null) {
            AstralSorcery.LOG.warn("Placed a network tile that didn't produce a network node! At: dim=" + blockEntityNetwork.getLevel().dimension().location() + ", pos=" + blockEntityNetwork.getBlockPos());
        } else if (node.needsUpdate()) {
            StarlightUpdateHandler.getInstance().addNode(blockEntityNetwork.getLevel(), node);
        }
    }

    public static void informNetworkBlockEntityRemoval(NetworkBlockEntity<?> blockEntityNetwork) {
        WorldNetworkHandler handler = WorldNetworkHandler.getNetworkHandler(blockEntityNetwork.getLevel());
        IPrismTransmissionNode node = handler.getTransmissionNode(blockEntityNetwork.getBlockPos());
        if (node == null) {
            AstralSorcery.LOG.warn("Tried to get a network node at a TileEntity, but didn't find one! At: dim=" + blockEntityNetwork.getLevel().dimension().location() + ", pos=" + blockEntityNetwork.getBlockPos());
        } else {
            StarlightUpdateHandler.getInstance().removeNode(((IStarlightTransmission<?>) blockEntityNetwork).getTrWorld(), node);
        }
        if (blockEntityNetwork instanceof IStarlightSource) {
            handler.removeSource((IStarlightSource<?>) blockEntityNetwork);
        } else if (blockEntityNetwork instanceof IStarlightTransmission) {
            handler.removeTransmission((IStarlightTransmission<?>) blockEntityNetwork);
        } else {
            AstralSorcery.LOG.warn("Removed a network tile that's not transmission/receiver or source! At: dim=" + blockEntityNetwork.getLevel().dimension().location() + ", pos=" + blockEntityNetwork.getBlockPos());
        }
    }

}
