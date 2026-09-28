package hellfirepvp.astralsorcery.common.structure.types;

import hellfirepvp.astralsorcery.common.structure.match.util.PatternBlockArray;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;

import java.util.function.Supplier;

public class StructureType {

    private final ResourceLocation registryName;
    private final Supplier<PatternBlockArray> structureSupplier;

    public StructureType(ResourceLocation registryName, Supplier<PatternBlockArray> structureSupplier) {
        this.registryName = registryName;
        this.structureSupplier = structureSupplier;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public String getName() {
        return registryName.getPath();
    }

    public Component getDisplayName() {
        return Component.translatable(String.format("structure.%s.%s.name", registryName.getNamespace(), registryName.getPath()));
    }

    public PatternBlockArray getStructure() {
        return structureSupplier.get();
    }

    public boolean matches(BlockGetter reader, BlockPos center) {
        return structureSupplier.get().matches(reader, center);
    }
}
