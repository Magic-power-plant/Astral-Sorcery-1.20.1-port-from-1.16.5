package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class ItemCelestialCrystal extends ItemCrystalBase {

    public ItemCelestialCrystal(Properties properties) {
        super(properties.rarity(CommonProxy.RARITY_CELESTIAL));
    }

    @Override
    protected Color getItemEntityColor(ItemStack stack) {
        return ColorsAS.CELESTIAL_CRYSTAL;
    }

    @Override
    public int getGeneratedPropertyTiers() {
        return 8;
    }

    @Override
    public int getMaxPropertyTiers() {
        return 10;
    }

    @Override
    public ItemAttunedCrystalBase getTunedItemVariant() {
        return (ItemAttunedCrystalBase) ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get();
    }

    @Override
    public ItemCrystalBase getInertDuplicateItem() {
        return (ItemCrystalBase) ItemsAS.CELESTIAL_CRYSTAL.get();
    }
}
