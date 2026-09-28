package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.lib.ItemsAS;

public class ItemRockCrystal extends ItemCrystalBase {

    public ItemRockCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public ItemAttunedCrystalBase getTunedItemVariant() {
        return (ItemAttunedCrystalBase) ItemsAS.ATTUNED_ROCK_CRYSTAL.get();
    }

    @Override
    public ItemCrystalBase getInertDuplicateItem() {
        return (ItemCrystalBase) ItemsAS.ROCK_CRYSTAL.get();
    }
}
