package hellfirepvp.astralsorcery.common.enchantment.dynamic;

import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.base.Mods;
import hellfirepvp.astralsorcery.common.data.config.registry.AmuletEnchantmentRegistry;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantmentHelper;
import hellfirepvp.astralsorcery.common.event.DynamicEnchantmentEvent;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.util.object.ObjectReference;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.QuickChargeEnchantment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class DynamicEnchantmentHelper {

    private static int getNewEnchantmentLevel(int current, String enchStr, ItemStack item, @Nullable List<DynamicEnchantment> context) {
        ResourceLocation rl = ResourceLocation.tryParse(enchStr);
        if (rl == null) return current;
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(rl);
        if (enchantment != null) {
            current = getNewEnchantmentLevel(current, enchantment, item, context);
            if (enchantment instanceof QuickChargeEnchantment) {
                current = Mth.clamp(current, 0, 5);
            }
        }
        return current;
    }

    public static int getNewEnchantmentLevel(int current, Enchantment enchantment, ItemStack item, @Nullable List<DynamicEnchantment> context) {
        if (!canHaveDynamicEnchantment(item)) {
            return current;
        }
        if (enchantment == null || !AmuletEnchantmentRegistry.canBeInfluenced(enchantment)) {
            return current;
        }

        List<DynamicEnchantment> modifiers = context != null ? context : fireEnchantmentGatheringEvent(item);
        for (DynamicEnchantment mod : modifiers) {
            Enchantment target = mod.getEnchantment();
            switch (mod.getType()) {
                case ADD_TO_SPECIFIC:
                    if (enchantment.equals(target)) {
                        current += mod.getLevelAddition();
                    }
                    break;
                case ADD_TO_EXISTING_SPECIFIC:
                    if (enchantment.equals(target) && current > 0) {
                        current += mod.getLevelAddition();
                    }
                    break;
                case ADD_TO_EXISTING_ALL:
                    if (current > 0) {
                        current += mod.getLevelAddition();
                    }
                    break;
                default:
                    break;
            }
        }
        if (enchantment instanceof QuickChargeEnchantment) {
            current = Mth.clamp(current, 0, 5);
        }
        return current;
    }

    public static ListTag modifyEnchantmentTags(ListTag existingEnchantments, ItemStack stack) {
        if (!canHaveDynamicEnchantment(stack)) {
            return existingEnchantments;
        }

        List<DynamicEnchantment> context = fireEnchantmentGatheringEvent(stack);
        if (context.isEmpty()) {
            return existingEnchantments;
        }

        ListTag returnNew = new ListTag();
        Set<String> enchantments = new HashSet<>(existingEnchantments.size());
        for (int i = 0; i < existingEnchantments.size(); i++) {
            CompoundTag cmp = existingEnchantments.getCompound(i);
            String enchKey = cmp.getString("id");
            int lvl = cmp.getInt("lvl");
            int newLvl = getNewEnchantmentLevel(lvl, enchKey, stack, context);

            CompoundTag newEnchTag = new CompoundTag();
            newEnchTag.putString("id", enchKey);
            newEnchTag.putInt("lvl", newLvl);
            returnNew.add(newEnchTag);

            enchantments.add(enchKey);
        }

        for (DynamicEnchantment mod : context) {
            if (mod.getType() == DynamicEnchantmentType.ADD_TO_SPECIFIC) {
                Enchantment ench = mod.getEnchantment();
                if (ench == null || !AmuletEnchantmentRegistry.canBeInfluenced(ench)) {
                    continue;
                }

                if (!stack.canApplyAtEnchantingTable(ench)) {
                    continue;
                }
                ResourceLocation enchKey = ForgeRegistries.ENCHANTMENTS.getKey(ench);
                if (enchKey == null) continue;
                String enchName = enchKey.toString();
                if (!enchantments.contains(enchName)) {
                    CompoundTag newEnchTag = new CompoundTag();
                    newEnchTag.putString("id", enchName);
                    newEnchTag.putInt("lvl", getNewEnchantmentLevel(0, ench, stack, context));
                    returnNew.add(newEnchTag);
                }
            }
        }
        return returnNew;
    }

    public static Map<Enchantment, Integer> addNewLevels(Map<Enchantment, Integer> enchantmentLevelMap, ItemStack stack) {
        if (!canHaveDynamicEnchantment(stack)) {
            return enchantmentLevelMap;
        }

        List<DynamicEnchantment> context = fireEnchantmentGatheringEvent(stack);
        if (context.isEmpty()) {
            return enchantmentLevelMap;
        }

        Map<Enchantment, Integer> copyRet = Maps.newLinkedHashMap(enchantmentLevelMap);
        enchantmentLevelMap.clear();
        for (Map.Entry<Enchantment, Integer> enchant : copyRet.entrySet()) {
            enchantmentLevelMap.put(enchant.getKey(), getNewEnchantmentLevel(enchant.getValue(), enchant.getKey(), stack, context));
        }

        for (DynamicEnchantment mod : context) {
            if (mod.getType() == DynamicEnchantmentType.ADD_TO_SPECIFIC) {
                Enchantment ench = mod.getEnchantment();
                if (ench == null || !AmuletEnchantmentRegistry.canBeInfluenced(ench)) {
                    continue;
                }
                if (!enchantmentLevelMap.containsKey(ench)) {
                    enchantmentLevelMap.put(ench, getNewEnchantmentLevel(0, ench, stack, context));
                }
            }
        }
        return enchantmentLevelMap;
    }

    public static boolean canHaveDynamicEnchantment(ItemStack stack) {
        if (!EventFlags.CAN_HAVE_DYN_ENCHANTMENTS.isSet()) {
            ObjectReference<Boolean> mayHaveDynamicEnchantments = new ObjectReference<>(false);
            EventFlags.CAN_HAVE_DYN_ENCHANTMENTS.executeWithFlag(() -> {
                if (stack.isEmpty()) {
                    return;
                }
                Item i = stack.getItem();
                ResourceLocation iKey = ForgeRegistries.ITEMS.getKey(i);
                if (iKey == null) {
                    return;
                }
                try {
                    if (!i.isEnchantable(stack) || i instanceof BookItem) {
                        return;
                    }
                } catch (NullPointerException exc) {
                    return;
                }
                if (Mods.DRACONIC_EVOLUTION.owns(iKey)) {
                    return;
                }
                mayHaveDynamicEnchantments.set(true);
            });
            return mayHaveDynamicEnchantments.get();
        }
        return false;
    }

    private static List<DynamicEnchantment> fireEnchantmentGatheringEvent(ItemStack tool) {
        Player foundEntity = AmuletEnchantmentHelper.getPlayerHavingTool(tool);
        if (foundEntity == null) {
            return new ArrayList<>();
        }
        DynamicEnchantmentEvent.Add addEvent = new DynamicEnchantmentEvent.Add(tool, foundEntity);
        if (MinecraftForge.EVENT_BUS.post(addEvent)) {
            return new ArrayList<>();
        }
        DynamicEnchantmentEvent.Modify modifyEvent = new DynamicEnchantmentEvent.Modify(tool, addEvent.getEnchantmentsToApply(), foundEntity);
        if (MinecraftForge.EVENT_BUS.post(modifyEvent)) {
            return new ArrayList<>();
        }
        return modifyEvent.getEnchantmentsToApply();
    }

}
