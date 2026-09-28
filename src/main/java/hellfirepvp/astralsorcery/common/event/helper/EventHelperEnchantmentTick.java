package hellfirepvp.astralsorcery.common.event.helper;

import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.enchantment.EnchantmentPlayerTick;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class EventHelperEnchantmentTick implements ITickHandler {

    public static final EventHelperEnchantmentTick INSTANCE = new EventHelperEnchantmentTick();

    private Collection<EnchantmentPlayerTick> tickableEnchantments = null;

    private EventHelperEnchantmentTick() {}

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Player player = (Player) context[0];
        LogicalSide side = (LogicalSide) context[1];

        if (tickableEnchantments == null) {
            tickableEnchantments = ForgeRegistries.ENCHANTMENTS.getValues().stream()
                    .filter(enchantment -> enchantment instanceof EnchantmentPlayerTick)
                    .map(enchantment -> (EnchantmentPlayerTick) enchantment)
                    .collect(Collectors.toList());
        }

        for (EnchantmentPlayerTick ench : this.tickableEnchantments) {
            int totalLevel = 0;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                int slotLevel = EnchantmentHelper.getTagEnchantmentLevel(ench, player.getItemBySlot(slot));
                if (slotLevel > totalLevel) {
                    totalLevel = slotLevel;
                }
            }
            if (totalLevel > 0) {
                ench.tick(player, side, totalLevel);
            }
        }
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.PLAYER);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "TickEnchantment Helper";
    }
}
