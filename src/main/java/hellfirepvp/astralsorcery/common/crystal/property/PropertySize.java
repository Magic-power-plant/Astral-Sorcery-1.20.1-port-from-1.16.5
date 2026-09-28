package hellfirepvp.astralsorcery.common.crystal.property;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_COLLECTOR_CRYSTAL;
import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_TOOL_DURABILITY;

public class PropertySize extends CrystalProperty {

    public PropertySize() {
        super(AstralSorcery.key("size"));
        this.setRequiredResearch(ResearchProgression.BASIC_CRAFT);

        this.addUsage(ctx -> ctx.uses(USE_COLLECTOR_CRYSTAL));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_COLLECTOR_CRYSTAL, value, () -> value * (1.0 + (0.2 * propertyLevel))));
        this.addUsage(ctx -> ctx.uses(USE_TOOL_DURABILITY));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_TOOL_DURABILITY, value, () -> value * (1.0 + (0.15 * propertyLevel))));
    }
}
