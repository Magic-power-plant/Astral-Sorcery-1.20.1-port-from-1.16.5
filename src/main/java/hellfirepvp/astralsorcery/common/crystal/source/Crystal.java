package hellfirepvp.astralsorcery.common.crystal.source;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertySource;

import javax.annotation.Nullable;

public class Crystal extends AttunedSourceInstance {

    public Crystal(PropertySource<?, ?> source,
                   @Nullable IWeakConstellation attunedConstellation) {
        super(source, attunedConstellation);
    }
}
