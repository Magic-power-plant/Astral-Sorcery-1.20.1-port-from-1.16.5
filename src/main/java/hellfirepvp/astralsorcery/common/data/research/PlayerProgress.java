package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.api.progression.IPlayerProgression;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.network.packet.PktSyncKnowledge;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayerProgress implements IPlayerProgression {
    private final List<ResourceLocation> knownConstellations = new ArrayList<>();
    private final List<ResourceLocation> seenConstellations = new ArrayList<>();
    private final List<ResourceLocation> storedConstellationPapers = new ArrayList<>();
    private final Set<ResearchProgression> researchProgression = new HashSet<>();
    private ProgressionTier tierReached = ProgressionTier.DISCOVERY;
    @Nullable
    private IMajorConstellation attunedConstellation;
    private boolean wasOnceAttuned;
    private boolean tomeReceived;
    private boolean usePerkAbilities = true;
    private final PlayerPerkData perkData = new PlayerPerkData();

    public void load(CompoundTag compound) {
        this.knownConstellations.clear();
        this.seenConstellations.clear();
        this.storedConstellationPapers.clear();
        this.researchProgression.clear();
        this.attunedConstellation = null;
        this.tierReached = ProgressionTier.DISCOVERY;
        this.wasOnceAttuned = false;
        this.tomeReceived = false;
        this.usePerkAbilities = true;
        readResourceList(compound, "seenConstellations", this.seenConstellations);
        readResourceList(compound, "constellations", this.knownConstellations);
        for (ResourceLocation known : this.knownConstellations) {
            addUnique(this.seenConstellations, known);
        }
        readResourceList(compound, "storedConstellationPapers", this.storedConstellationPapers);
        for (ResourceLocation stored : this.storedConstellationPapers) {
            addUnique(this.seenConstellations, stored);
        }

        if (compound.contains("attuned", Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(compound.getString("attuned"));
            IConstellation constellation = ConstellationRegistry.getConstellation(id);
            if (constellation instanceof IMajorConstellation major) {
                this.attunedConstellation = major;
            } else if (id != null) {
                AstralSorcery.LOG.warn("Failed to load attuned Constellation: {} - constellation doesn't exist or isn't major.", id);
            }
        }

        // Perk loading depends on the attuned constellation when an old tree version
        // is detected and the current root perk must be restored.
        this.perkData.load(this, compound);

        if (compound.contains("tierReached", Tag.TAG_INT)) {
            this.tierReached = byOrdinal(compound.getInt("tierReached"));
        }
        if (compound.contains("research", Tag.TAG_INT_ARRAY)) {
            for (int researchOrdinal : compound.getIntArray("research")) {
                ResearchProgression progression = researchByOrdinal(researchOrdinal);
                if (progression != null) {
                    this.researchProgression.add(progression);
                }
            }
        }
        this.wasOnceAttuned = compound.getBoolean("wasAttuned");
        this.tomeReceived = !compound.contains("bookReceived", Tag.TAG_BYTE) || compound.getBoolean("bookReceived");
        if (compound.contains("usePerkAbilities", Tag.TAG_BYTE)) {
            this.usePerkAbilities = compound.getBoolean("usePerkAbilities");
        }
    }

    public void store(CompoundTag compound) {
        writeResourceList(compound, "constellations", this.knownConstellations);
        writeResourceList(compound, "seenConstellations", this.seenConstellations);
        writeResourceList(compound, "storedConstellationPapers", this.storedConstellationPapers);
        compound.putInt("tierReached", this.tierReached.ordinal());
        compound.putIntArray("research", this.researchProgression.stream().mapToInt(ResearchProgression::ordinal).toArray());
        compound.putBoolean("wasAttuned", this.wasOnceAttuned);
        if (this.attunedConstellation != null) {
            compound.putString("attuned", this.attunedConstellation.getRegistryName().toString());
        }
        compound.putBoolean("bookReceived", this.tomeReceived);
        compound.putBoolean("usePerkAbilities", this.usePerkAbilities);
        this.perkData.save(compound);
    }

    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        store(compound);
        return compound;
    }

    public void deserializeNBT(CompoundTag compound) {
        load(compound);
    }

    public void storeKnowledge(CompoundTag compound) {
        writeResourceList(compound, "constellations", this.knownConstellations);
        writeResourceList(compound, "seenConstellations", this.seenConstellations);
        compound.putInt("tierReached", this.tierReached.ordinal());
        compound.putIntArray("research", this.researchProgression.stream().mapToInt(ResearchProgression::ordinal).toArray());
        compound.putBoolean("wasAttuned", this.wasOnceAttuned);
    }

    public void loadKnowledge(CompoundTag compound) {
        List<ResourceLocation> seen = new ArrayList<>();
        readResourceList(compound, "seenConstellations", seen);
        for (ResourceLocation id : seen) {
            memorizeConstellation(id);
        }

        List<ResourceLocation> known = new ArrayList<>();
        readResourceList(compound, "constellations", known);
        for (ResourceLocation id : known) {
            discoverConstellation(id);
        }

        if (compound.contains("tierReached", Tag.TAG_INT)) {
            ProgressionTier otherTier = byOrdinal(compound.getInt("tierReached"));
            if (otherTier.isThisLater(this.tierReached)) {
                this.tierReached = otherTier;
            }
        }
        if (compound.contains("research", Tag.TAG_INT_ARRAY)) {
            for (int researchOrdinal : compound.getIntArray("research")) {
                ResearchProgression progression = researchByOrdinal(researchOrdinal);
                if (progression != null) {
                    this.researchProgression.add(progression);
                }
            }
        }
        if (compound.getBoolean("wasAttuned")) {
            this.wasOnceAttuned = true;
        }
    }

    public ProgressionTier getTierReached() {
        return tierReached;
    }

    public void setTierReached(ProgressionTier tierReached) {
        this.tierReached = tierReached;
    }

    @Nullable
    public IMajorConstellation getAttunedConstellation() {
        return attunedConstellation;
    }

    public void setAttunedConstellation(@Nullable IMajorConstellation constellation) {
        this.attunedConstellation = constellation;
        this.wasOnceAttuned = constellation != null || this.wasOnceAttuned;
    }

    public boolean isAttuned() {
        return this.attunedConstellation != null;
    }

    public boolean wasOnceAttuned() {
        return wasOnceAttuned;
    }

    public void setAttunedBefore(boolean attuned) {
        this.wasOnceAttuned = attuned;
    }

    public boolean didReceiveTome() {
        return tomeReceived;
    }

    public void setTomeReceived() {
        this.tomeReceived = true;
    }

    public boolean doPerkAbilities() {
        return usePerkAbilities;
    }

    public void setUsePerkAbilities(boolean usePerkAbilities) {
        this.usePerkAbilities = usePerkAbilities;
    }

    public List<ResourceLocation> getKnownConstellations() {
        return Collections.unmodifiableList(knownConstellations);
    }

    public List<ResourceLocation> getSeenConstellations() {
        return Collections.unmodifiableList(seenConstellations);
    }

    public List<ResourceLocation> getStoredConstellationPapers() {
        return Collections.unmodifiableList(storedConstellationPapers);
    }

    public Collection<ResearchProgression> getResearchProgression() {
        return Collections.unmodifiableSet(researchProgression);
    }

    public boolean hasResearch(ResearchProgression progression) {
        return this.researchProgression.contains(progression);
    }

    public boolean forceGainResearch(ResearchProgression progression) {
        return this.researchProgression.add(progression);
    }

    public boolean hasSeenConstellation(IConstellation constellation) {
        return hasSeenConstellation(constellation.getRegistryName());
    }

    public boolean hasSeenConstellation(ResourceLocation constellation) {
        return seenConstellations.contains(constellation);
    }

    public boolean hasConstellationDiscovered(IConstellation constellation) {
        return hasConstellationDiscovered(constellation.getRegistryName());
    }

    public boolean hasConstellationDiscovered(ResourceLocation constellation) {
        return knownConstellations.contains(constellation);
    }

    public void discoverConstellation(ResourceLocation name) {
        memorizeConstellation(name);
        addUnique(knownConstellations, name);
    }

    public void memorizeConstellation(ResourceLocation name) {
        addUnique(seenConstellations, name);
    }

    public void setStoredConstellationPapers(List<ResourceLocation> names) {
        this.storedConstellationPapers.clear();
        for (ResourceLocation name : names) {
            addUnique(this.storedConstellationPapers, name);
            memorizeConstellation(name);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void receive(PktSyncKnowledge message) {
        //1.16.5 swaps the collection references outright; the target's collections are final fields, so the
        //contents are replaced in place instead.
        this.knownConstellations.clear();
        this.knownConstellations.addAll(message.knownConstellations);
        this.seenConstellations.clear();
        this.seenConstellations.addAll(message.seenConstellations);
        this.storedConstellationPapers.clear();
        this.storedConstellationPapers.addAll(message.storedConstellationPapers);
        this.researchProgression.clear();
        this.researchProgression.addAll(new HashSet<>(message.researchProgression));
        this.tierReached = MiscUtils.getEnumEntry(ProgressionTier.class, message.progressTier);
        this.attunedConstellation = message.attunedConstellation;
        this.wasOnceAttuned = message.wasOnceAttuned;
        this.perkData.receive(message);
        this.usePerkAbilities = message.doPerkAbilities;
    }

    public PlayerProgress copy() {
        PlayerProgress copy = new PlayerProgress();
        copy.load(this.serializeNBT());
        return copy;
    }

    public void acceptMergeFrom(PlayerProgress toMergeFrom) {
        for (ResourceLocation seen : toMergeFrom.seenConstellations) {
            memorizeConstellation(seen);
        }
        for (ResourceLocation known : toMergeFrom.knownConstellations) {
            discoverConstellation(known);
        }
        if (toMergeFrom.tierReached.isThisLater(this.tierReached)) {
            this.tierReached = toMergeFrom.tierReached;
        }
        if (toMergeFrom.wasOnceAttuned) {
            this.wasOnceAttuned = true;
        }
        for (ResearchProgression progression : toMergeFrom.researchProgression) {
            this.researchProgression.add(progression);
        }
    }

    public PlayerPerkData getPerkData() {
        return this.perkData;
    }

    public boolean isValid() {
        return true;
    }

    private static void readResourceList(CompoundTag compound, String key, List<ResourceLocation> out) {
        if (!compound.contains(key, Tag.TAG_LIST)) {
            return;
        }
        ListTag list = compound.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                addUnique(out, id);
            }
        }
    }

    private static void writeResourceList(CompoundTag compound, String key, Iterable<ResourceLocation> values) {
        ListTag list = new ListTag();
        for (ResourceLocation id : values) {
            list.add(StringTag.valueOf(id.toString()));
        }
        compound.put(key, list);
    }

    private static void addUnique(List<ResourceLocation> list, ResourceLocation id) {
        if (!list.contains(id)) {
            list.add(id);
        }
    }

    private static ProgressionTier byOrdinal(int ordinal) {
        ProgressionTier[] values = ProgressionTier.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return ProgressionTier.DISCOVERY;
        }
        return values[ordinal];
    }

    @Nullable
    private static ResearchProgression researchByOrdinal(int ordinal) {
        ResearchProgression[] values = ResearchProgression.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return null;
        }
        return values[ordinal];
    }
}
