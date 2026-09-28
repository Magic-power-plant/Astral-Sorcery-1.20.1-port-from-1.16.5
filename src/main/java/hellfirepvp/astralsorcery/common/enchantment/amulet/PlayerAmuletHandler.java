package hellfirepvp.astralsorcery.common.enchantment.amulet;

import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import hellfirepvp.astralsorcery.common.event.DynamicEnchantmentEvent;
import hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;

import java.util.EnumSet;

public class PlayerAmuletHandler implements ITickHandler {

    public static final PlayerAmuletHandler INSTANCE = new PlayerAmuletHandler();

    private PlayerAmuletHandler() {}

    public static void onEnchantmentAdd(DynamicEnchantmentEvent.Add event) {
        if (!DynamicEnchantmentHelper.canHaveDynamicEnchantment(event.getEnchantedItemStack())) {
            return;
        }

        AmuletEnchantmentHelper.Pair<ItemStack, Player> linkedAmulet = AmuletEnchantmentHelper.getWornAmulet(event.getEnchantedItemStack());
        if (linkedAmulet == null ||
                linkedAmulet.getA().isEmpty() ||
                linkedAmulet.getB() == null) {
            return;
        }

        event.getEnchantmentsToApply().addAll(ItemEnchantmentAmulet.getAmuletEnchantments(linkedAmulet.getA()));
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Player player = (Player) context[0];
        applyAmuletTags(player);
        clearAmuletTags(player);
    }

    private void applyAmuletTags(Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            AmuletEnchantmentHelper.applyAmuletOwner(player.getItemBySlot(slot), player);
        }
    }

    private void clearAmuletTags(Player player) {
        AmuletEnchantmentHelper.removeAmuletTagsAndCleanup(player, true);
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
        return "PlayerAmuletHandler";
    }
}
