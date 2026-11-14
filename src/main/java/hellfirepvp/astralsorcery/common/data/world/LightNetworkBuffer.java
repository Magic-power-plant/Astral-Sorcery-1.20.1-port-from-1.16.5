package hellfirepvp.astralsorcery.common.data.world;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.config.entry.LightNetworkConfig;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.IStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.IStarlightTransmission;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightTransmissionHandler;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightUpdateHandler;
import hellfirepvp.astralsorcery.common.starlight.network.TransmissionWorldHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.block.util.BlockStateHelper;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.data.world.cache.WorldCacheDomain;
import hellfirepvp.astralsorcery.common.data.world.cache.base.SectionWorldData;
import hellfirepvp.astralsorcery.common.data.world.cache.base.WorldSection;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class LightNetworkBuffer extends SectionWorldData<LightNetworkBuffer.ChunkNetworkData> {

    private final Map<BlockPos, IIndependentStarlightSource> starlightSources = new HashMap<>();
    private Collection<Tuple<BlockPos, IIndependentStarlightSource>> cachedSourceTuples = null;

    private final Set<BlockPos> queueRemoval = new HashSet<>();

    public LightNetworkBuffer(WorldCacheDomain.SaveKey<?> key) {
        super(key, PRECISION_CHUNK);
    }

    public WorldNetworkHandler getNetworkHandler(Level world) {
        return new WorldNetworkHandler(this, world);
    }

    @Override
    protected ChunkNetworkData createNewSection(int sectionX, int sectionZ) {
        return new ChunkNetworkData(sectionX, sectionZ);
    }

    @Override
    public void updateTick(Level world) {
        cleanupQueuedChunks();

        TransmissionWorldHandler handle = StarlightTransmissionHandler.getInstance().getWorldHandler(world);

        Iterator<Map.Entry<BlockPos, IIndependentStarlightSource>> iterator = starlightSources.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, IIndependentStarlightSource> entry = iterator.next();
            BlockPos pos = entry.getKey();
            IIndependentStarlightSource source = entry.getValue();

            MiscUtils.executeWithChunk(world, pos, () -> {
                IStarlightSource<?> te = MiscUtils.getBlockEntityAt(world, pos, IStarlightSource.class, true);
                if (te != null) {
                    if (te.needsToRefreshNetworkChain()) {
                        if (handle != null) {
                            handle.breakSourceNetwork(source);
                        }
                        te.markChainRebuilt();
                    }
                } else {
                    BlockState actual = world.getBlockState(pos);
                    AstralSorcery.LOG.warn("Cached source at " + pos + " but didn't find the TileEntity!");
                    AstralSorcery.LOG.warn("Purging cache entry and removing erroneous block!");
                    AstralSorcery.LOG.warn("Block that gets purged: " + BlockStateHelper.serialize(actual));
                    iterator.remove();
                    if (world.setBlockAndUpdate(pos, actual.getFluidState().createLegacyBlock())) {
                        ChunkNetworkData data = getSection(pos);
                        if (data != null) {
                            data.removeSourceBlockEntity(pos);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onLoad(Level world) {
        super.onLoad(world);

        if (LightNetworkConfig.CONFIG.performNetworkIntegrityCheck.get()) {
            AstralSorcery.LOG.info("[LightNetworkIntegrityCheck] Performing StarlightNetwork integrity check for world " + world.dimension().location());
            List<IPrismTransmissionNode> invalidRemoval = new LinkedList<>();

            for (ChunkNetworkData data : getSections()) {
                for (ChunkSectionNetworkData secData : data.sections.values()) {
                    for (IPrismTransmissionNode node : secData.getAllTransmissionNodes()) {
                        IStarlightTransmission<?> te = MiscUtils.getBlockEntityAt(world, node.getLocationPos(), IStarlightTransmission.class, true);
                        if (te == null) {
                            invalidRemoval.add(node);
                            continue;
                        }
                        IPrismTransmissionNode newNode = te.provideTransmissionNode(node.getLocationPos());
                        if (!node.getClass().isAssignableFrom(newNode.getClass())) {
                            invalidRemoval.add(node);
                            continue;
                        }

                        if (node.needsUpdate()) {
                            StarlightUpdateHandler.getInstance().addNode(world, node);
                        }
                        node.postLoad(world);
                    }
                }
            }

            AstralSorcery.LOG.info("[LightNetworkIntegrityCheck] Performed StarlightNetwork integrity check. Found " + invalidRemoval.size() + " invalid transmission nodes.");
            for (IPrismTransmissionNode node : invalidRemoval) {
                removeTransmission(node.getLocationPos());
            }
            AstralSorcery.LOG.info("[LightNetworkIntegrityCheck] Removed invalid transmission nodes from the network.");

        } else {
            for (ChunkNetworkData data : getSections()) {
                for (ChunkSectionNetworkData secData : data.sections.values()) {
                    for (IPrismTransmissionNode node : secData.getAllTransmissionNodes()) {
                        if (node.needsUpdate()) {
                            StarlightUpdateHandler.getInstance().addNode(world, node);
                        }
                        node.postLoad(world);
                    }
                }
            }
        }
    }

    private void cleanupQueuedChunks() {
        for (BlockPos pos : queueRemoval) {
            ChunkNetworkData data = getSection(pos);
            if (data != null && data.isEmpty()) {
                this.removeSection(data);
            }
        }
        queueRemoval.clear();
    }

    @Nullable
    public ChunkSectionNetworkData getSectionData(BlockPos pos) {
        ChunkNetworkData data = getSection(pos);
        if (data == null) return null;
        return data.getSection(pos.getY() >> 4);
    }

    @Nullable
    public IIndependentStarlightSource getSource(BlockPos at) {
        return starlightSources.get(at);
    }

    public Collection<Tuple<BlockPos, IIndependentStarlightSource>> getAllSources() {
        if (cachedSourceTuples == null) {
            Collection<Tuple<BlockPos, IIndependentStarlightSource>> cache = new LinkedList<>();
            for (Map.Entry<BlockPos, IIndependentStarlightSource> entry : starlightSources.entrySet()) {
                cache.add(new Tuple<>(entry.getKey(), entry.getValue()));
            }
            this.cachedSourceTuples = Collections.unmodifiableCollection(cache);
        }
        return cachedSourceTuples;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        starlightSources.clear();
        cachedSourceTuples = null;

        if (nbt.contains("sources")) {
            ListTag list = nbt.getList("sources", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag sourcePos = list.getCompound(i);
                BlockPos at = NBTHelper.readBlockPosFromNBT(sourcePos);

                CompoundTag comp = sourcePos.getCompound("source");
                ResourceLocation identifier = ResourceLocation.tryParse(comp.getString("sTypeId"));
                SourceClassRegistry.SourceProvider provider = SourceClassRegistry.getProvider(identifier);
                if (provider == null) {
                    AstralSorcery.LOG.warn("Couldn't load source tile at " + at + " - invalid identifier: " + identifier);
                    continue;
                }
                IIndependentStarlightSource source = provider.provideEmptySource();
                source.readFromNBT(comp);
                this.starlightSources.put(at, source);
            }
        }
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        cleanupQueuedChunks();

        ListTag sourceList = new ListTag();
        for (BlockPos pos : starlightSources.keySet()) {
            CompoundTag sourceTag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(pos, sourceTag);
            CompoundTag source = new CompoundTag();
            IIndependentStarlightSource sourceNode = starlightSources.get(pos);
            try {
                sourceNode.writeToNBT(source);
            } catch (Exception exc) {
                AstralSorcery.LOG.warn("Couldn't write source-node data for network node at " + pos.toString() + "!");
                AstralSorcery.LOG.warn("This is a major problem. To be perfectly save, consider making a backup, then break or mcedit the tileentity out and place a proper/new one...");
                continue;
            }
            source.putString("sTypeId", sourceNode.getProvider().getIdentifier().toString());
            sourceTag.put("source", source);
            sourceList.add(sourceTag);
        }
        nbt.put("sources", sourceList);
    }

    public void addSource(IStarlightSource<?> source, BlockPos pos) {
        ChunkNetworkData data = getOrCreateSection(pos);
        data.addSourceBlockEntity(pos, source);

        IIndependentStarlightSource newSource = addIndependentSource(pos, source);
        if (newSource != null) {
            Map<BlockPos, IIndependentStarlightSource> copyTr = Collections.unmodifiableMap(new HashMap<>(starlightSources));
            Thread tr = new Thread(() -> threadedUpdateSourceProximity(copyTr));
            tr.setName("StarlightNetwork-UpdateThread");
            tr.start();
        }

        markDirty(data);
    }

    private void threadedUpdateSourceProximity(Map<BlockPos, IIndependentStarlightSource> copyTr) {
        try {
            for (Map.Entry<BlockPos, IIndependentStarlightSource> sourceTuple : copyTr.entrySet()) {
                sourceTuple.getValue().threadedUpdateProximity(sourceTuple.getKey(), copyTr);
            }
        } catch (Exception exc) {
            AstralSorcery.LOG.warn("Failed to update proximity status for source nodes.");
            exc.printStackTrace();
        }
    }

    public void addTransmission(IStarlightTransmission<?> transmission, BlockPos pos) {
        ChunkNetworkData data = getOrCreateSection(pos);
        data.addTransmissionBlockEntity(pos, transmission);

        markDirty(data);
    }

    public void removeSource(BlockPos pos) {
        ChunkNetworkData data = getSection(pos);
        if (data == null) return;
        data.removeSourceBlockEntity(pos);

        removeIndependentSource(pos);

        Map<BlockPos, IIndependentStarlightSource> copyTr = Collections.unmodifiableMap(new HashMap<>(starlightSources));
        Thread tr = new Thread(() -> threadedUpdateSourceProximity(copyTr));
        tr.setName("StarlightNetwork-UpdateThread");
        tr.start();

        checkIntegrity(pos);
        markDirty(data);
    }

    public void removeTransmission(BlockPos pos) {
        ChunkNetworkData data = getSection(pos);
        if (data == null) return;
        data.removeTransmissionBlockEntity(pos);

        checkIntegrity(pos);
        markDirty(data);
    }

    private void checkIntegrity(BlockPos actualPos) {
        ChunkNetworkData data = getSection(actualPos);
        if (data == null) return;

        data.checkIntegrity();

        if (data.isEmpty()) {
            queueRemoval.add(actualPos);
        }
    }

    @Nullable
    private IIndependentStarlightSource addIndependentSource(BlockPos pos, IStarlightSource<?> source) {
        this.cachedSourceTuples = null;

        ITransmissionSource node = source.getNode();
        if (node != null) {
            IIndependentStarlightSource sourceNode = node.provideNewIndependentSource(source);
            this.starlightSources.put(pos, sourceNode);
            return sourceNode;
        }
        return null;
    }

    private void removeIndependentSource(BlockPos pos) {
        this.starlightSources.remove(pos);
        this.cachedSourceTuples = null;
    }

    public static class ChunkNetworkData extends WorldSection {

        private final Map<Integer, ChunkSectionNetworkData> sections = new HashMap<>();

        ChunkNetworkData(int sX, int sZ) {
            super(sX, sZ);
        }

        @Override
        public void readFromNBT(CompoundTag tag) {
            for (String key : tag.getAllKeys()) {
                int yLevel;
                try {
                    yLevel = Integer.parseInt(key);
                } catch (NumberFormatException exc) {
                    continue;
                }
                ListTag yData = tag.getList(key, Tag.TAG_COMPOUND);
                ChunkSectionNetworkData sectionNetData = ChunkSectionNetworkData.loadFromNBT(yData);
                this.sections.put(yLevel, sectionNetData);
            }
        }

        @Override
        public void writeToNBT(CompoundTag data) {
            for (Integer yLevel : sections.keySet()) {
                ChunkSectionNetworkData sectionData = sections.get(yLevel);
                ListTag sectionTag = new ListTag();
                sectionData.writeToNBT(sectionTag);
                data.put(String.valueOf(yLevel), sectionTag);
            }
        }

        //Also allows for passing invalid yLevels outside of the world's section range
        @Nullable
        public ChunkSectionNetworkData getSection(int yLevel) {
            return sections.get(yLevel);
        }

        public void checkIntegrity() {
            Iterator<Integer> iterator = sections.keySet().iterator();
            while (iterator.hasNext()) {
                Integer yLevel = iterator.next();
                ChunkSectionNetworkData data = sections.get(yLevel);
                if (data.isEmpty()) iterator.remove();
            }
        }

        public boolean isEmpty() {
            return sections.isEmpty();
        }

        private ChunkSectionNetworkData getOrCreateSection(int yLevel) {
            ChunkSectionNetworkData section = getSection(yLevel);
            if (section == null) {
                section = new ChunkSectionNetworkData();
                sections.put(yLevel, section);
            }
            return section;
        }

        private void removeSourceBlockEntity(BlockPos pos) {
            int yLevel = sectionYLevel(pos);
            ChunkSectionNetworkData section = getSection(yLevel);
            if (section == null) return;
            section.removeSourceBlockEntity(pos);
        }

        private void removeTransmissionBlockEntity(BlockPos pos) {
            int yLevel = sectionYLevel(pos);
            ChunkSectionNetworkData section = getSection(yLevel);
            if (section == null) return;
            section.removeTransmissionBlockEntity(pos);
        }

        private void addSourceBlockEntity(BlockPos pos, IStarlightSource<?> source) {
            int yLevel = sectionYLevel(pos);
            ChunkSectionNetworkData section = getOrCreateSection(yLevel);
            section.addSourceBlockEntity(pos, source);
        }

        private void addTransmissionBlockEntity(BlockPos pos, IStarlightTransmission<?> transmission) {
            int yLevel = sectionYLevel(pos);
            ChunkSectionNetworkData section = getOrCreateSection(yLevel);
            section.addTransmissionBlockEntity(pos, transmission);
        }

        /**
         * The section index of a block position inside this chunk column.
         * <p>
         * 1.16.5 wrote this as {@code (pos.getY() & 255) >> 4} in all four call sites above. With that
         * version's 0..255 world height {@code y & 255 == y}, so the mask was a no-op and the expression
         * was exactly {@code y >> 4} (0..15). The read path never masked at all: 1.16.5's
         * {@link LightNetworkBuffer#getSectionData(BlockPos)} - the single entry point
         * {@code WorldNetworkHandler} queries nodes through - already used a plain {@code pos.getY() >> 4},
         * and still does here.
         * <p>
         * In 1.20.1 worlds span -64..319, where the mask is no longer the identity and would break the
         * agreement between the write and read paths:
         * <ul>
         *   <li>{@code y = -64}: masked {@code (-64 & 255) >> 4 == 12}, but the reader looks in section
         *       {@code -64 >> 4 == -4}. The node is stored in section 12 (the real y 192..207 section),
         *       so it is invisible to every lookup and pollutes an unrelated section.</li>
         *   <li>{@code y = -1}: masked {@code 15}, reader {@code -1}. Same failure, plus y=-16 masks to 15
         *       while y=-17 masks to 14, i.e. the mask does not even keep a section's 16 blocks contiguous
         *       for negative y.</li>
         *   <li>{@code y = 300}: masked {@code 2}, reader {@code 18}.</li>
         * </ul>
         * The plain arithmetic shift is floor division by 16 ({@code -64 >> 4 == -4}, {@code -16 >> 4 == -1},
         * {@code -1 >> 4 == -1}), which is exactly vanilla's {@code SectionPos#blockToSectionCoord}, so the
         * index stays unique and contiguous over the whole -64..319 range (-4..19). Negative indices persist
         * fine: {@link #writeToNBT(CompoundTag)} keys sections by {@code String.valueOf(yLevel)} and
         * {@link #readFromNBT(CompoundTag)} parses them back with {@code Integer.parseInt}.
         * <p>
         * For y in 0..255 this is bit-identical to the 1.16.5 expression, so existing saves are unaffected.
         * Not runtime-tested: nodes below y=0 / above y=255 have not been verified in-game across a
         * save+reload.
         */
        private static int sectionYLevel(BlockPos pos) {
            return pos.getY() >> 4;
        }

    }

    public static class ChunkSectionNetworkData {

        private final Map<BlockPos, IPrismTransmissionNode> nodes = new HashMap<>();

        private static ChunkSectionNetworkData loadFromNBT(ListTag sectionData) {
            ChunkSectionNetworkData netData = new ChunkSectionNetworkData();
            for (int i = 0; i < sectionData.size(); i++) {
                CompoundTag nodeComp = sectionData.getCompound(i);
                BlockPos pos = NBTHelper.readBlockPosFromNBT(nodeComp);

                CompoundTag prismComp = nodeComp.getCompound("nodeTag");
                ResourceLocation nodeIdentifier = ResourceLocation.tryParse(prismComp.getString("trNodeId"));
                TransmissionProvider provider = TransmissionClassRegistry.getProvider(nodeIdentifier);
                if (provider == null) {
                    AstralSorcery.LOG.warn("Couldn't load node tile at " + pos + " - invalid identifier: " + nodeIdentifier);
                    continue;
                }
                IPrismTransmissionNode node = provider.get();
                node.readFromNBT(prismComp);
                netData.nodes.put(pos, node);
            }
            return netData;
        }

        private void writeToNBT(ListTag sectionData) {
            for (Map.Entry<BlockPos, IPrismTransmissionNode> node : nodes.entrySet()) {
                try {
                    CompoundTag nodeComp = new CompoundTag();
                    NBTHelper.writeBlockPosToNBT(node.getKey(), nodeComp);

                    CompoundTag prismComp = new CompoundTag();
                    IPrismTransmissionNode prismNode = node.getValue();
                    prismNode.writeToNBT(prismComp);
                    prismComp.putString("trNodeId", prismNode.getProvider().getIdentifier().toString());

                    nodeComp.put("nodeTag", prismComp);
                    sectionData.add(nodeComp);
                } catch (Exception exc) {
                    try {
                        BlockPos at = node.getKey();
                        AstralSorcery.LOG.warn("Couldn't write node data for network node at " + at.toString() + "!");
                        AstralSorcery.LOG.warn("This is a major problem. To be perfectly save, consider making a backup, then break or mcedit the tileentity out and place a proper/new one...");
                    } catch (Exception exc2) {
                        try {
                            BlockPos at = node.getValue().getLocationPos();
                            AstralSorcery.LOG.warn("Couldn't write node data for network node at " + at.toString() + "!");
                            AstralSorcery.LOG.warn("This is a major problem. To be perfectly save, consider making a backup, then break or mcedit the tileentity out and place a proper/new one...");
                        } catch (Exception exc3) {
                            AstralSorcery.LOG.warn("Couldn't write node data for a network node! Skipping...");
                        }
                    }
                }
            }
        }

        public boolean isEmpty() {
            return nodes.isEmpty();
        }

        public Collection<IPrismTransmissionNode> getAllTransmissionNodes() {
            return Collections.unmodifiableCollection(nodes.values());
        }

        @Nullable
        public IPrismTransmissionNode getTransmissionNode(BlockPos at) {
            return nodes.get(at);
        }

        private void removeSourceBlockEntity(BlockPos pos) {
            removeNode(pos);
        }

        private void removeTransmissionBlockEntity(BlockPos pos) {
            removeNode(pos);
        }

        private void removeNode(BlockPos pos) {
            nodes.remove(pos);
        }

        private void addSourceBlockEntity(BlockPos pos, IStarlightSource<?> source) {
            addNode(pos, source);
        }

        private void addTransmissionBlockEntity(BlockPos pos, IStarlightTransmission<?> transmission) {
            addNode(pos, transmission);
        }

        private void addNode(BlockPos pos, IStarlightTransmission<?> transmission) {
            nodes.put(pos, transmission.provideTransmissionNode(pos));
        }

    }

}
