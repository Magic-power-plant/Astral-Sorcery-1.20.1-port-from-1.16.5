package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderablePage;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface JournalPage {

    int DEFAULT_WIDTH = 175;
    int DEFAULT_HEIGHT = 220;

    @OnlyIn(Dist.CLIENT)
    RenderablePage buildRenderPage(ResearchNode node, int page);

}
