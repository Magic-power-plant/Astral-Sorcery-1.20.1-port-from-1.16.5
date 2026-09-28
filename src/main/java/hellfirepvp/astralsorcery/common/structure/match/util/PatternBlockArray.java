package hellfirepvp.astralsorcery.common.structure.match.util;

import hellfirepvp.astralsorcery.common.structure.match.structure.MatchableStructure;
import net.minecraft.resources.ResourceLocation;

public class PatternBlockArray extends BlockArray implements MatchableStructure {

    private final ResourceLocation registryName;

    public PatternBlockArray(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }
}
