package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import hellfirepvp.astralsorcery.common.lib.CreativeTabsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemAttunedRockCrystal extends ItemAttunedCrystalBase implements CreativeTabItem {

    public ItemAttunedRockCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public CreativeModeTab getCreativeTab() {
        return CreativeTabsAS.TAB_AS_CRYSTALS.get();
    }

    // 1.16.5 fillItemGroup: one attuned variant per weak constellation in the crystals tab.
    @Override
    public void fillCreativeTab(CreativeModeTab.Output output) {
        for (IWeakConstellation cst : ConstellationRegistry.getWeakConstellations()) {
            ItemStack stack = new ItemStack(this);
            setAttunedConstellation(stack, cst);
            output.accept(stack);
        }
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
