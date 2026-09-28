package hellfirepvp.astralsorcery.common.crystal.source;

import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.calc.PropertySource;

import javax.annotation.Nullable;

public class Ritual extends TraitedSourceInstance {

    public Ritual(PropertySource<?, ?> source,
                  @Nullable IWeakConstellation attunedConstellation,
                  @Nullable IMinorConstellation traitConstellation) {
        super(source, attunedConstellation, traitConstellation);
    }
}
