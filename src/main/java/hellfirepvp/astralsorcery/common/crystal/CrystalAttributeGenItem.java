package hellfirepvp.astralsorcery.common.crystal;

@Deprecated
public interface CrystalAttributeGenItem extends CrystalAttributeItem,
        hellfirepvp.astralsorcery.api.crystal.CrystalAttributeGenItem {

    @Override
    int getGeneratedPropertyTiers();

    @Override
    int getMaxPropertyTiers();
}
