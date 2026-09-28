package hellfirepvp.astralsorcery.common.crystal.property;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_COLLECTOR_CRYSTAL;

public class PropertyCollectionRate extends CrystalProperty {

    public PropertyCollectionRate() {
        super(AstralSorcery.key("collector.rate"));
        this.setRequiredResearch(ResearchProgression.ATTUNEMENT);

        this.addUsage(ctx -> ctx.uses(USE_COLLECTOR_CRYSTAL));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_COLLECTOR_CRYSTAL, value, () -> value * (1.0 + (0.2 * propertyLevel))));
    }
}
