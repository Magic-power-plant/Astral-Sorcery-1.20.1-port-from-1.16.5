package hellfirepvp.astralsorcery.common.constellation.effect;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public abstract class ConstellationEffectProvider {

    private final IWeakConstellation cst;
    private final ResourceLocation registryName;

    protected ConstellationEffectProvider(IWeakConstellation cst) {
        this.cst = cst;
        this.registryName = cst.getRegistryName();
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Nonnull
    public IWeakConstellation getConstellation() {
        return cst;
    }

    public abstract ConstellationEffect createEffect(@Nullable ILocatable origin);

    @Nullable
    public static ConstellationEffectProvider getProvider(IWeakConstellation constellation) {
        return RegistriesAS.CONSTELLATION_EFFECTS.getValue(constellation.getRegistryName());
    }

    public static ConstellationEffectProvider register(IWeakConstellation cst, Function<ILocatable, ? extends ConstellationEffect> effectProvider) {
        ConstellationEffectProvider provider = new ConstellationEffectProvider(cst) {
            @Override
            public ConstellationEffect createEffect(@Nullable ILocatable origin) {
                return effectProvider.apply(origin);
            }
        };
        return RegistriesAS.CONSTELLATION_EFFECTS.register(provider);
    }
}
