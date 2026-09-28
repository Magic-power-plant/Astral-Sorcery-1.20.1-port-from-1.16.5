package hellfirepvp.astralsorcery.common.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.fml.LogicalSide;

public abstract class EnchantmentPlayerTick extends Enchantment {

    protected EnchantmentPlayerTick(Rarity rarityIn, EnchantmentCategory typeIn, EquipmentSlot[] slots) {
        super(rarityIn, typeIn, slots);
    }

    public abstract void tick(Player player, LogicalSide side, int level);

}
