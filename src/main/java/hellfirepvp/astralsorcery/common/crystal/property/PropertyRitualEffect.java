package hellfirepvp.astralsorcery.common.crystal.property;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Usages.USE_RITUAL_EFFECT;

public class PropertyRitualEffect extends CrystalProperty {

    public PropertyRitualEffect() {
        super(AstralSorcery.key("ritual.effect"));
        this.setRequiredResearch(ResearchProgression.ATTUNEMENT);

        this.addUsage(ctx -> ctx.uses(USE_RITUAL_EFFECT));
        this.addModifier((value, originalValue, propertyLevel, context) -> {
            if (context.uses(USE_RITUAL_EFFECT)) {
                return value * (1.0 + (0.3 * propertyLevel));
            }
            return value;
        });
    }
}
