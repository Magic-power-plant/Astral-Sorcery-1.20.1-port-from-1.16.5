package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class CrystalProperty implements Comparable<CrystalProperty> {

    private static int counter = 0;
    private final int sortingId;
    private final ResourceLocation registryName;

    private ResearchProgression requiredResearch = null;
    private List<CrystalPropertyModifierFunction> modifiers = new ArrayList<>();
    private Predicate<CalculationContext> usageTests = (ctx) -> false;

    public CrystalProperty(ResourceLocation registryName) {
        this.sortingId = counter++;
        this.registryName = registryName;
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public CrystalProperty setRequiredResearch(ResearchProgression requiredResearch) {
        this.requiredResearch = requiredResearch;
        return this;
    }

    public CrystalProperty addModifier(CrystalPropertyModifierFunction modifierFunction) {
        this.modifiers.add(modifierFunction);
        return this;
    }

    public CrystalProperty addUsage(Predicate<CalculationContext> usage) {
        this.usageTests = this.usageTests.or(usage);
        return this;
    }

    public int getMaxTier() {
        return 3;
    }

    public boolean canSee(PlayerProgress progress) {
        return this.requiredResearch == null || progress.hasResearch(this.requiredResearch);
    }

    public boolean hasUsageFor(CalculationContext ctx) {
        return this.usageTests.test(ctx);
    }

    public double modify(double value, int tier, CalculationContext context) {
        double originalValue = value;
        for (CrystalPropertyModifierFunction fn : modifiers) {
            value = fn.modify(value, originalValue, tier, context);
        }
        return value;
    }

    public MutableComponent getName(int currentTier) {
        return Component.translatable(String.format("crystal.property.%s.%s.name", getRegistryName().getNamespace(), getRegistryName().getPath()));
    }

    @Override
    public int compareTo(CrystalProperty other) {
        return this.sortingId - other.sortingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrystalProperty that = (CrystalProperty) o;
        return Objects.equals(this.getRegistryName(), that.getRegistryName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getRegistryName());
    }
}
