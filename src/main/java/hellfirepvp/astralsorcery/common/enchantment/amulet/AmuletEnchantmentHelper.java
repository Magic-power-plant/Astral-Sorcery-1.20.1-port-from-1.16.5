package hellfirepvp.astralsorcery.common.enchantment.amulet;

import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentHelper;
import hellfirepvp.astralsorcery.common.integration.IntegrationCurios;
import hellfirepvp.astralsorcery.common.item.ItemEnchantmentAmulet;
import hellfirepvp.astralsorcery.common.entity.util.EntityUtils;
import hellfirepvp.astralsorcery.common.item.util.ItemComparator;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class AmuletEnchantmentHelper {

    public static final String KEY_AS_OWNER = "AS_Amulet_Holder";

    public static void removeAmuletTagsAndCleanup(Player player, boolean keepEquipped) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (i == player.getInventory().selected && keepEquipped) {
                continue;
            }
            removeAmuletOwner(player.getInventory().items.get(i));
        }
        removeAmuletOwner(player.getInventory().items.get(player.getInventory().selected));
        if (!keepEquipped) {
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                removeAmuletOwner(player.getInventory().armor.get(i));
            }
            for (int i = 0; i < player.getInventory().offhand.size(); i++) {
                removeAmuletOwner(player.getInventory().offhand.get(i));
            }
        }
    }

    @Nonnull
    private static UUID getWornPlayerUUID(ItemStack anyTool) {
        if (DynamicEnchantmentHelper.canHaveDynamicEnchantment(anyTool) && anyTool.hasTag()) {
            return NBTHelper.getUUID(anyTool.getTag(), KEY_AS_OWNER, new UUID(0, 0));
        }
        return new UUID(0, 0);
    }

    public static void applyAmuletOwner(ItemStack tool, Player wearer) {
        if (DynamicEnchantmentHelper.canHaveDynamicEnchantment(tool)) {
            tool.getOrCreateTag().putUUID(KEY_AS_OWNER, wearer.getUUID());
        }
    }

    private static void removeAmuletOwner(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        stack.getTag().remove(KEY_AS_OWNER);
        if (stack.getTag().isEmpty()) {
            stack.setTag(null);
        }
    }

    @Nullable
    public static Player getPlayerHavingTool(ItemStack anyTool) {
        UUID plUUID = getWornPlayerUUID(anyTool);
        if (plUUID.getLeastSignificantBits() == 0 && plUUID.getMostSignificantBits() == 0) {
            return null;
        }
        Player player = DistExecutor.unsafeRunForDist(
                () -> () -> resolvePlayerClient(plUUID),
                () -> () -> EntityUtils.getPlayerServer(plUUID)
        );
        if (player == null) {
            return null;
        }

        int originalDamage = anyTool.getDamageValue();

        boolean foundTool = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            anyTool.setDamageValue(stack.getDamageValue());
            if (ItemComparator.compare(stack, anyTool, ItemComparator.Clause.Sets.ITEMSTACK_STRICT)) {
                foundTool = true;
                break;
            }
        }
        anyTool.setDamageValue(originalDamage);
        if (!foundTool) return null;

        return player;
    }

    @Nullable
    static Pair<ItemStack, Player> getWornAmulet(ItemStack anyTool) {
        Player player = getPlayerHavingTool(anyTool);
        if (player == null) return null;

        Optional<Object> curios = IntegrationCurios.getCurio(player, (stack) -> stack.getItem() instanceof ItemEnchantmentAmulet);
        if (curios.isPresent()) {
            return null;
        }

        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.getItem() instanceof ItemEnchantmentAmulet) {
                return Pair.of(stack, player);
            }
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private static Player resolvePlayerClient(UUID plUUID) {
        return EntityUtils.getPlayerClient(plUUID);
    }

    public static class Pair<A, B> {
        private final A a;
        private final B b;

        private Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public static <A, B> Pair<A, B> of(A a, B b) {
            return new Pair<>(a, b);
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }
    }

}
