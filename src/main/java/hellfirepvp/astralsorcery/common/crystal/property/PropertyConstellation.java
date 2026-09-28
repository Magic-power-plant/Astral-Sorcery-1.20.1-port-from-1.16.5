package hellfirepvp.astralsorcery.common.crystal.property;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.crystal.source.AttunedSourceInstance;
import hellfirepvp.astralsorcery.common.crystal.source.Crystal;
import hellfirepvp.astralsorcery.common.crystal.source.Ritual;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;

import static hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS.Sources.*;

public class PropertyConstellation extends CrystalProperty {

    private final IWeakConstellation cst;

    public PropertyConstellation(IWeakConstellation cst) {
        super(AstralSorcery.key("constellation." + cst.getSimpleName()));
        this.cst = cst;
        this.setRequiredResearch(ResearchProgression.ATTUNEMENT);

        this.addUsage(ctx -> ctx.isSource(SOURCE_COLLECTOR_CRYSTAL));
        this.addModifier((value, originalValue, propertyLevel, context) -> {
            if (context.isSource(SOURCE_COLLECTOR_CRYSTAL) || context.isSource(SOURCE_TILE_COLLECTOR_CRYSTAL)) {
                Crystal crystal = context.getSource();
                if (crystal != null && cst.equals(crystal.getAttunedConstellation())) {
                    return value * (1.0 + (0.3 * propertyLevel));
                }
            }
            return value;
        });
        this.addUsage(ctx -> ctx.isSource(SOURCE_RITUAL_PEDESTAL) || ctx.isSource(SOURCE_TILE_RITUAL_PEDESTAL));
        this.addModifier((value, originalValue, propertyLevel, context) -> {
            if (context.isSource(SOURCE_RITUAL_PEDESTAL) || context.isSource(SOURCE_TILE_RITUAL_PEDESTAL)) {
                Ritual ritual = context.getSource();
                if (ritual != null && cst.equals(ritual.getAttunedConstellation())) {
                    return value * (1.0 + (0.35 * propertyLevel));
                }
            }
            return value;
        });
    }

    public IWeakConstellation getConstellation() {
        return cst;
    }

    @Override
    public int getMaxTier() {
        return 2;
    }

    @Override
    public boolean canSee(PlayerProgress progress) {
        return super.canSee(progress) && progress.hasConstellationDiscovered(this.cst);
    }
}
