package hellfirepvp.astralsorcery.common.crystal.property;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_TOOL_EFFECTIVENESS;

public class PropertyToolEfficiency extends CrystalProperty {

    public PropertyToolEfficiency() {
        super(AstralSorcery.key("tool.efficiency"));
        this.setRequiredResearch(ResearchProgression.BASIC_CRAFT);

        this.addUsage(ctx -> ctx.uses(USE_TOOL_EFFECTIVENESS));
        this.addModifier((value, originalValue, propertyLevel, context) ->
                context.withUse(USE_TOOL_EFFECTIVENESS, value, () -> value * (1.0 + (0.15 * Math.min(propertyLevel, 4)))));
    }
}
