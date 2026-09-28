package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.crystal.calc.PropertySource;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertyUsage;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CalculationContext {

    private final Collection<PropertyUsage> usages = new HashSet<>();
    private PropertySource.SourceInstance source = null;

    private CalculationContext() {}

    public boolean uses(PropertyUsage usage) {
        return usages.contains(usage);
    }

    public double withUse(PropertyUsage usage, double defaultValue, Supplier<Double> valueSupplier) {
        if (this.uses(usage)) {
            return valueSupplier.get();
        }
        return defaultValue;
    }

    public boolean hasSource() {
        return source != null;
    }

    public boolean isSource(PropertySource<?, ?> source) {
        return this.isSource(source::equals);
    }

    public boolean isSource(Predicate<PropertySource<?, ?>> sourceTest) {
        return hasSource() && sourceTest.test(this.source.getSource());
    }

    @SuppressWarnings("unchecked")
    public <T extends PropertySource.SourceInstance> T getSource() {
        if (hasSource()) {
            return (T) this.source;
        }
        return null;
    }

    public boolean isEmpty() {
        return this.usages.isEmpty() && this.source == null;
    }

    public static class Builder {

        private CalculationContext ctx = new CalculationContext();

        public static Builder newBuilder() {
            return new Builder();
        }

        public static Builder withUsage(PropertyUsage usage) {
            return newBuilder().addUsage(usage);
        }

        public static Builder withSource(PropertySource.SourceInstance source) {
            return newBuilder().fromSource(source);
        }

        public Builder addUsage(PropertyUsage usage) {
            ctx.usages.add(usage);
            return this;
        }

        public Builder fromSource(PropertySource.SourceInstance source) {
            ctx.source = source;
            return this;
        }

        public CalculationContext build() {
            return this.ctx;
        }
    }
}
