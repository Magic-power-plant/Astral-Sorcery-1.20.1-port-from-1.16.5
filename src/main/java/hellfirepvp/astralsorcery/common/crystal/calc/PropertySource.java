package hellfirepvp.astralsorcery.common.crystal.calc;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public abstract class PropertySource<T, I extends PropertySource.SourceInstance> {

    private final ResourceLocation registryName;

    public PropertySource(ResourceLocation registryName) {
        this.registryName = registryName;
    }

    public final ResourceLocation getRegistryName() {
        return registryName;
    }

    public abstract I createInstance(T obj);

    public Component getName() {
        return Component.translatable(String.format("crystal.source.%s.%s.name",
                getRegistryName().getNamespace(), getRegistryName().getPath()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertySource that = (PropertySource) o;
        return Objects.equals(registryName, that.registryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registryName);
    }

    public abstract static class SourceInstance {

        private final PropertySource<?, ?> source;

        protected SourceInstance(PropertySource<?, ?> source) {
            this.source = source;
        }

        public PropertySource<?, ?> getSource() {
            return source;
        }
    }
}
