package hellfirepvp.astralsorcery.api.crystal;

/** Crystal item capability that can generate and cap property tiers. */
public interface CrystalAttributeGenItem extends CrystalAttributeItem {

    int getGeneratedPropertyTiers();

    int getMaxPropertyTiers();
}
