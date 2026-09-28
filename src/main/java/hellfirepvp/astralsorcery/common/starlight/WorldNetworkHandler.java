/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.world.LightNetworkBuffer;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightTransmissionHandler;
import hellfirepvp.astralsorcery.common.starlight.network.TransmissionWorldHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.NodeConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldNetworkHandler
 * Created by HellFirePvP
 * Date: 02.08.2016 / 23:09
 */
public class WorldNetworkHandler {

    private final LightNetworkBuffer buffer;
    private final Level world;

    public WorldNetworkHandler(LightNetworkBuffer lightNetworkBuffer, Level world) {
        this.buffer = lightNetworkBuffer;
        this.world = world;
    }

    public Level getWorld() {
        return world;
    }

    /**
     * Reference: 1.16.5 WorldNetworkHandler.java:53-55. Persistence goes through
     * WorldCacheDomain#getData on DataAS.KEY_STARLIGHT_NETWORK, which lazily loads/instantiates the
     * faithful {@link LightNetworkBuffer}; the buffer then hands back a handler, which is a cheap,
     * stateless view over it and is created fresh per call in the reference too.
     */
    public static WorldNetworkHandler getNetworkHandler(Level world) {
        return DataAS.DOMAIN_AS.getData(world, DataAS.KEY_STARLIGHT_NETWORK).getNetworkHandler(world);
    }

    public void informBlockChange(BlockPos at) {
        List<LightNetworkBuffer.ChunkSectionNetworkData> relatedData = getAffectedChunkSections(at);
        if (relatedData.isEmpty()) return; //lucky. nothing to do.
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(getWorld());

        for (LightNetworkBuffer.ChunkSectionNetworkData data : relatedData) {
            if (data == null) continue;
            Collection<IPrismTransmissionNode> transmissionNodes = data.getAllTransmissionNodes();
            for (IPrismTransmissionNode node : transmissionNodes) {
                if (node.notifyBlockChange(getWorld(), at)) {
                    if (handle != null) {
                        handle.notifyTransmissionNodeChange(node);
                    }
                }
            }
        }
    }

    public void attemptAutoLinkTo(BlockPos at) {
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(world);
        for (Tuple<BlockPos, IIndependentStarlightSource> source : getAllSources()) {
            if (!source.getB().providesAutoLink()) continue;

            //1.16.5: source.getA().distanceSq(Vector3d.copy(at), false) - useCenter=false makes that
            //the plain corner-to-corner squared distance, i.e. 1.20.1 Vec3i#distSqr(Vec3i).
            if (source.getA().distSqr(at) <= 256) {
                IPrismTransmissionNode node = getTransmissionNode(source.getA());
                if (node == null) {
                    AstralSorcery.LOG.warn("Didn't find a TransmissionNode at a position that's supposed to be a source!");
                    AstralSorcery.LOG.warn("Details: Dim=" + getWorld().dimension().location() + " at " + source.getA());
                    continue;
                }
                if (!(node instanceof ITransmissionSource)) {
                    AstralSorcery.LOG.warn("Found TransmissionNode that isn't a source at a source position!");
                    AstralSorcery.LOG.warn("Details: Dim=" + getWorld().dimension().location() + " at " + source.getA());
                    continue;
                }
                ITransmissionSource sourceNode = (ITransmissionSource) node;
                if (sourceNode.getLocationPos().getY() <= at.getY()) continue;
                sourceNode.notifyLink(getWorld(), at);

                markDirty(at, source.getA());

                if (handle != null) {
                    handle.notifyTransmissionNodeChange(sourceNode);
                }
            }
        }
    }

    public void removeAutoLinkTo(BlockPos at) {
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(world);
        for (Tuple<BlockPos, IIndependentStarlightSource> source : getAllSources()) {
            if (!source.getB().providesAutoLink()) continue;

            //1.16.5: source.getA().distanceSq(Vector3d.copy(at), false) - see attemptAutoLinkTo.
            if (source.getA().distSqr(at) <= 256) {
                IPrismTransmissionNode node = getTransmissionNode(source.getA());
                if (node == null) {
                    AstralSorcery.LOG.warn("Didn't find a TransmissionNode at a position that's supposed to be a source!");
                    AstralSorcery.LOG.warn("Details: Dim=" + getWorld().dimension().location() + " at " + source.getA());
                    continue;
                }
                if (!(node instanceof ITransmissionSource)) {
                    AstralSorcery.LOG.warn("Found TransmissionNode that isn't a source at a source position!");
                    AstralSorcery.LOG.warn("Details: Dim=" + getWorld().dimension().location() + " at " + source.getA());
                    continue;
                }
                ITransmissionSource sourceNode = (ITransmissionSource) node;
                if (sourceNode.notifyUnlink(getWorld(), at)) {
                    markDirty(at, source.getA());

                    if (handle != null) {
                        handle.notifyTransmissionNodeChange(sourceNode);
                    }
                }
            }
        }
    }

    @Nullable
    public IPrismTransmissionNode getTransmissionNode(@Nullable BlockPos pos) {
        if (pos == null) return null;
        LightNetworkBuffer.ChunkSectionNetworkData section = getNetworkData(pos);
        if (section != null) {
            return section.getTransmissionNode(pos);
        }
        return null;
    }

    //1.16.5 took a Vector3i vararg; 1.20.1's Vec3i is the same type (BlockPos still extends it).
    public void markDirty(Vec3i... positions) {
        for (Vec3i pos : positions) {
            buffer.markDirty(pos);
        }
    }

    @Nullable
    public IIndependentStarlightSource getSourceAt(BlockPos pos) {
        return buffer.getSource(pos);
    }

    public Collection<Tuple<BlockPos, IIndependentStarlightSource>> getAllSources() {
        return buffer.getAllSources();
    }

    public void removeSource(IStarlightSource<?> source) {
        removeThisSourceFromNext(source);
        removeThisNextFromSources(source);

        buffer.removeSource(source.getTrPos());
    }

    public void removeTransmission(IStarlightTransmission<?> transmission) {
        removeThisSourceFromNext(transmission);
        removeThisNextFromSources(transmission);

        buffer.removeTransmission(transmission.getTrPos());
    }

    public void addNewSourceTile(IStarlightSource<?> source) {
        buffer.addSource(source, source.getTrPos());

        linkNextToThisSources(source);
    }

    public void addTransmissionTile(IStarlightTransmission<?> transmission) {
        buffer.addTransmission(transmission, transmission.getTrPos());

        linkNextToThisSources(transmission);
    }

    //For all sources of this "tr" inform the transmission system that the connection might've changed.
    private void removeThisNextFromSources(IStarlightTransmission<?> tr) {
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(getWorld());
        if (handle == null) return;

        IPrismTransmissionNode node = tr.getNode();
        if (node == null) {
            new Throwable().printStackTrace();
            AstralSorcery.LOG.warn("Could not find transmission node for Transmission tile '" + tr.getClass().getSimpleName() + "'");
            AstralSorcery.LOG.warn("This is an implementation error. Report it along with the steps to create this, if you come across this.");
            return;
        }

        for (BlockPos pos : node.getSources()) {
            IPrismTransmissionNode sourceNode = getTransmissionNode(pos);
            if (sourceNode != null) {
                handle.notifyTransmissionNodeChange(sourceNode);
            }
        }
    }

    //What it does: For all "next"'s of this "tr" inform them that "tr" has been removed and
    //thus remove "tr" from their sources.
    private void removeThisSourceFromNext(IStarlightTransmission<?> tr) {
        IPrismTransmissionNode node = tr.getNode();
        if (node == null) {
            AstralSorcery.LOG.warn("Could not find transmission node for Transmission tile '" + tr.getClass().getSimpleName() + "'");
            AstralSorcery.LOG.warn("This is an implementation error. Report it along with the steps to create this, if you come across this.");
            return;
        }
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(getWorld());
        if (handle != null) {
            handle.notifyTransmissionNodeChange(node);
        }

        BlockPos thisPos = tr.getTrPos();
        List<NodeConnection<IPrismTransmissionNode>> nodeConnections = node.queryNext(this);
        for (NodeConnection<IPrismTransmissionNode> connection : nodeConnections) {
            if (connection.getNode() != null) {
                connection.getNode().notifySourceUnlink(getWorld(), thisPos);
                if (handle != null) {
                    handle.notifyTransmissionNodeChange(connection.getNode());
                }
            }
        }
    }

    //That's what i call resource intensive.
    //What it does: Checks if there is a Node that has a "next" at this "tr"'s position.
    //If yes, it'll add that node as source for this "tr" to make backwards find possible.
    private void linkNextToThisSources(IStarlightTransmission<?> tr) {
        IPrismTransmissionNode node = tr.getNode();
        if (node == null) {
            AstralSorcery.LOG.warn("Previously added Transmission tile '" + tr.getClass().getSimpleName() + "' didn't create a Transmission node!");
            AstralSorcery.LOG.warn("This is an implementation error. Report it along with the steps to create this, if you come across this.");
            return;
        }
        BlockPos thisPos = tr.getTrPos();
        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(getWorld());
        List<LightNetworkBuffer.ChunkSectionNetworkData> dataList = getAffectedChunkSections(tr.getTrPos());
        for (LightNetworkBuffer.ChunkSectionNetworkData data : dataList) {
            if (data == null) continue;
            for (IPrismTransmissionNode otherNode : data.getAllTransmissionNodes()) {
                List<NodeConnection<IPrismTransmissionNode>> nodeConnections = otherNode.queryNext(this);
                for (NodeConnection<IPrismTransmissionNode> connection : nodeConnections) {
                    if (connection.getTo().equals(thisPos)) {
                        node.notifySourceLink(getWorld(), otherNode.getLocationPos());
                        if (handle != null) {
                            handle.notifyTransmissionNodeChange(otherNode);
                        }
                    }
                }
            }
        }
    }

    //Collects a 3x3 field of chunkData, centered around the given pos.
    //Might be empty, if there's no network nearby.
    private List<LightNetworkBuffer.ChunkSectionNetworkData> getAffectedChunkSections(BlockPos centralPos) {
        List<LightNetworkBuffer.ChunkSectionNetworkData> dataList = new LinkedList<>();
        ChunkPos central = new ChunkPos(centralPos);
        //1.16.5 clamped to 0..255 (MathHelper.clamp(centralPos.getY(), 0, 255)) because that was the
        //whole world height. 1.20.1 worlds span getMinBuildHeight()..getMaxBuildHeight()-1 (-64..319 by
        //default), so clamping to 0..255 here would silently drop every node above y=255 or below y=0
        //from the 3x3x3 query - LightNetworkBuffer keys sections by the unmasked pos.getY() >> 4, which
        //is negative below y=0.
        int minY = getWorld().getMinBuildHeight();
        int maxY = getWorld().getMaxBuildHeight() - 1;
        int posYLevel = Mth.clamp(centralPos.getY(), minY, maxY);
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                for (int yy = -1; yy <= 1; yy++) {
                    //1.16.5 ChunkPos#asBlockPos is 1.20.1 ChunkPos#getWorldPosition - the chunk origin at y=0,
                    //so offsetting by the clamped y level below reproduces 1.16.5's absolute y exactly.
                    BlockPos pos = central.getWorldPosition();
                    pos = pos.offset(xx * 16, Mth.clamp(posYLevel + yy * 16, minY, maxY), zz * 16);
                    queryData(pos, dataList);
                }
            }
        }
        return dataList;
    }

    private void queryData(BlockPos pos, List<LightNetworkBuffer.ChunkSectionNetworkData> out) {
        LightNetworkBuffer.ChunkSectionNetworkData data = buffer.getSectionData(pos);
        if (data != null && !data.isEmpty()) out.add(data);
    }

    @Nullable
    private LightNetworkBuffer.ChunkSectionNetworkData getNetworkData(BlockPos at) {
        LightNetworkBuffer.ChunkSectionNetworkData data = buffer.getSectionData(at);
        if (data != null && !data.isEmpty()) return data;
        return null;
    }
}
