package hellfirepvp.astralsorcery.api.progression;

import hellfirepvp.astralsorcery.api.perk.IPlayerPerkData;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import net.minecraft.resources.ResourceLocation;
import java.util.Collection;
import javax.annotation.Nullable;

/** Read-only research and constellation progression for a player. */
public interface IPlayerProgression {
    ProgressionTier getTierReached();
    @Nullable IMajorConstellation getAttunedConstellation();
    boolean isAttuned();
    boolean wasOnceAttuned();
    boolean didReceiveTome();
    boolean doPerkAbilities();
    Collection<ResourceLocation> getKnownConstellations();
    Collection<ResourceLocation> getSeenConstellations();
    Collection<ResourceLocation> getStoredConstellationPapers();
    Collection<ResearchProgression> getResearchProgression();
    boolean hasResearch(ResearchProgression progression);
    boolean hasSeenConstellation(IConstellation constellation);
    boolean hasSeenConstellation(ResourceLocation constellation);
    boolean hasConstellationDiscovered(IConstellation constellation);
    boolean hasConstellationDiscovered(ResourceLocation constellation);
    IPlayerPerkData getPerkData();
}
