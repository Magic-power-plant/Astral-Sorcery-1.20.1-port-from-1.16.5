package hellfirepvp.astralsorcery.common.data.journal;

import hellfirepvp.astralsorcery.client.screen.journal.page.RenderPageStructure;
import hellfirepvp.astralsorcery.client.screen.journal.page.RenderablePage;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.structure.match.util.BlockArray;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JournalPageStructure implements JournalPage {

    private final BlockArray structure;
    private final Component name;
    private final Vector3 shift;

    public JournalPageStructure(BlockArray struct) {
        this(struct, null);
    }

    public JournalPageStructure(BlockArray struct, @Nullable Component name) {
        this(struct, name, new Vector3());
    }

    public JournalPageStructure(BlockArray struct, @Nullable Component name, @Nonnull Vector3 shift) {
        this.structure = struct;
        this.name = name;
        this.shift = shift;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderablePage buildRenderPage(ResearchNode node, int nodePage) {
        return new RenderPageStructure(node, nodePage, structure, name, shift.clone());
    }
}
