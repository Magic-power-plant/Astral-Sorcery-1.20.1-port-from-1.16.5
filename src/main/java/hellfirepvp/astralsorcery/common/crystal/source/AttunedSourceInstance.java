package hellfirepvp.astralsorcery.common.crystal.source;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertySource;

import javax.annotation.Nullable;

public class AttunedSourceInstance extends PropertySource.SourceInstance {

    private final IWeakConstellation attunedConstellation;

    public AttunedSourceInstance(PropertySource<?, ?> source, @Nullable IWeakConstellation attunedConstellation) {
        super(source);
        this.attunedConstellation = attunedConstellation;
    }

    @Nullable
    public IWeakConstellation getAttunedConstellation() {
        return attunedConstellation;
    }
}
