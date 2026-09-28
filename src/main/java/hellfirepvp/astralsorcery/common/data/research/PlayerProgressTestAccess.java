package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlayerProgressTestAccess extends PlayerProgress {

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public Collection<ResearchProgression> getResearchProgression() {
        return Collections.emptyList();
    }

    @Override
    public List<ResourceLocation> getSeenConstellations() {
        return Collections.emptyList();
    }

    @Override
    public List<ResourceLocation> getKnownConstellations() {
        return Collections.emptyList();
    }

    @Override
    public List<ResourceLocation> getStoredConstellationPapers() {
        return Collections.emptyList();
    }

    @Override
    public IMajorConstellation getAttunedConstellation() {
        return null;
    }

    @Override
    public ProgressionTier getTierReached() {
        return ProgressionTier.DISCOVERY;
    }

    @Override
    public boolean hasSeenConstellation(ResourceLocation constellation) {
        return false;
    }

    @Override
    public boolean hasConstellationDiscovered(ResourceLocation constellation) {
        return false;
    }

    @Override
    public boolean wasOnceAttuned() {
        return false;
    }

    @Override
    public boolean didReceiveTome() {
        return true;
    }

    @Override
    public boolean doPerkAbilities() {
        return false;
    }

}
