package hellfirepvp.astralsorcery.common.starlight.network;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.block.base.BlockStarlightRecipient;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.data.sync.server.DataLightBlockEndpoints;
import hellfirepvp.astralsorcery.common.data.sync.server.LightConnectionsData;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.NodeConnection;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

public class TransmissionChain {

    private final Set<ChunkPos> involvedChunks = new HashSet<>();
    private final List<LightConnection> foundConnections = new LinkedList<>();
    private final Map<BlockPos, Float> remainMultiplierMap = new HashMap<>();

    private final Set<BlockPos> uncheckedEndpointsBlock = new HashSet<>();
    private final Set<BlockPos> resolvedNormalBlockPositions = new HashSet<>();
    private final Set<ITransmissionReceiver> endpointsNodes = new HashSet<>();
    private final Map<IPrismTransmissionNode, Float> transmissionUpdateMap = new HashMap<>();

    private final WorldNetworkHandler handler;
    private final IPrismTransmissionNode sourceNode;

    private TransmissionChain(WorldNetworkHandler netHandler, IPrismTransmissionNode sourceNode) {
        this.handler = netHandler;
        this.sourceNode = sourceNode;
    }

    public static void buildNetworkChain(Level world, TransmissionWorldHandler handle, IIndependentStarlightSource source, WorldNetworkHandler netHandler, BlockPos sourcePos) {
        TransmissionChain chain = buildFromSource(netHandler, sourcePos);
        handle.updateNetworkChainData(world, chain, source, netHandler, sourcePos);
        SyncDataHolder.executeServer(SyncDataHolder.DATA_LIGHT_CONNECTIONS, LightConnectionsData.class, data -> {
            data.updateNewConnectionsThreaded(netHandler.getWorld().dimension(), chain.getFoundConnections());
        });
        SyncDataHolder.executeServer(SyncDataHolder.DATA_LIGHT_BLOCK_ENDPOINTS, DataLightBlockEndpoints.class, data -> {
            data.updateNewEndpoints(netHandler.getWorld().dimension(), chain.getResolvedNormalBlockPositions());
        });
    }

    private static TransmissionChain buildFromSource(WorldNetworkHandler netHandler, BlockPos at) {
        TransmissionChain chain = new TransmissionChain(netHandler, null);

        IPrismTransmissionNode node = netHandler.getTransmissionNode(at);
        if (node != null) {
            chain = new TransmissionChain(netHandler, node);
            chain.recBuildChain(node, 1F, new LinkedList<>());
        }

        chain.calculateInvolvedChunks();
        chain.resolveLoadedEndpoints(netHandler.getWorld());
        return chain;
    }

    private void resolveLoadedEndpoints(Level world) {
        for (BlockPos pos : uncheckedEndpointsBlock) {
            MiscUtils.executeWithChunk(world, pos, () -> {
                BlockState state = world.getBlockState(pos);
                Block b = state.getBlock();
                if (b instanceof BlockStarlightRecipient) {
                    return;
                }
                resolvedNormalBlockPositions.add(pos);
            });
        }
    }

    protected void updatePosAsResolved(Level world, BlockPos pos) {
        if (uncheckedEndpointsBlock.contains(pos) && !resolvedNormalBlockPositions.contains(pos)) {
            resolvedNormalBlockPositions.add(pos);
            SyncDataHolder.executeServer(SyncDataHolder.DATA_LIGHT_BLOCK_ENDPOINTS, DataLightBlockEndpoints.class, data -> {
                data.updateNewEndpoint(world.dimension(), pos);
            });
        }
    }

    private void recBuildChain(IPrismTransmissionNode node, float lossMultiplier, LinkedList<BlockPos> prevPath) {
        if (lossMultiplier <= 0.001F) return;

        CrystalAttributes lensProperties = node.getTransmissionProperties();
        float lossPerc = lossMultiplier * CrystalCalculations.getThroughputMultiplier(lensProperties);
        float nextHopLossPerc = lossPerc * node.getTransmissionThroughputMultiplier();
        float transmissionPerc = lossPerc * node.getTransmissionConsumptionMultiplier() * CrystalCalculations.getThroughputEffectMultiplier(lensProperties);

        List<NodeConnection<IPrismTransmissionNode>> next = node.queryNext(handler);
        float nextLoss = nextHopLossPerc / ((float) next.size());
        prevPath.push(node.getLocationPos());

        if (node.needsTransmissionUpdate()) {
            transmissionUpdateMap.put(node, transmissionPerc);
        }

        for (NodeConnection<IPrismTransmissionNode> nextNode : next) {
            IPrismTransmissionNode trNode = nextNode.getNode();
            if (nextNode.canConnect()) {
                BlockPos nextPos = nextNode.getTo();
                addIfNonExistentConnection(node.getLocationPos(), nextPos);
                if (!prevPath.contains(nextPos)) {
                    remainMultiplierMap.merge(nextPos, nextLoss, Float::sum);

                    if (trNode != null) {
                        if (trNode instanceof ITransmissionReceiver) {
                            if (!this.endpointsNodes.contains(trNode)) {
                                this.endpointsNodes.add((ITransmissionReceiver) trNode);
                            }
                        } else {
                            recBuildChain(trNode, nextLoss, prevPath);
                        }
                    } else {
                        this.uncheckedEndpointsBlock.add(nextPos);
                    }
                }
            }
        }

        prevPath.pop();
    }

    private void calculateInvolvedChunks() {
        for (BlockPos nodePos : remainMultiplierMap.keySet()) {
            involvedChunks.add(new ChunkPos(nodePos));
        }
    }

    public Set<BlockPos> getResolvedNormalBlockPositions() {
        return resolvedNormalBlockPositions;
    }

    public IPrismTransmissionNode getSourceNode() {
        return sourceNode;
    }

    private void addIfNonExistentConnection(BlockPos start, BlockPos end) {
        LightConnection newCon = new LightConnection(start, end);
        if (!foundConnections.contains(newCon)) foundConnections.add(newCon);
    }

    public Map<IPrismTransmissionNode, Float> getTransmissionUpdates() {
        return this.transmissionUpdateMap;
    }

    public Collection<ChunkPos> getInvolvedChunks() {
        return involvedChunks;
    }

    public Map<BlockPos, Float> getLossMultipliers() {
        return remainMultiplierMap;
    }

    public List<LightConnection> getFoundConnections() {
        return foundConnections;
    }

    public Set<ITransmissionReceiver> getEndpointsNodes() {
        return endpointsNodes;
    }

    public Set<BlockPos> getUncheckedEndpointsBlock() {
        return uncheckedEndpointsBlock;
    }

}
