package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletRandomizeHelper;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ItemEnchantmentAmulet extends Item implements ItemDynamicColor {

    private static final Random rand = new Random();

    public ItemEnchantmentAmulet(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        List<AmuletEnchantment> enchantments = getAmuletEnchantments(stack);
        for (AmuletEnchantment ench : enchantments) {
            tooltip.add(ench.getDisplay().copy().withStyle(ChatFormatting.BLUE));
        }

        if (getAmuletColor(stack).map(color -> color == 0xFFFFFFFF).orElse(false)) {
            tooltip.add(Component.translatable("astralsorcery.amulet.color.colorless")
                    .withStyle(ChatFormatting.ITALIC)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!worldIn.isClientSide() && getAmuletColor(stack).isEmpty()) {
            freezeAmuletColor(stack);
        }
        if (!worldIn.isClientSide() && getAmuletEnchantments(stack).isEmpty()) {
            AmuletRandomizeHelper.rollAmulet(stack);
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFFFF;
        }
        Optional<Integer> color = getAmuletColor(stack);
        if (color.isPresent()) {
            return color.get();
        }
        int tick = (int) (ClientScheduler.getClientTick() % 500000L);
        int c = Color.getHSBColor(tick / 500000F, 0.7F, 1F).getRGB();
        return c | 0xFF000000;
    }

    public static Optional<Integer> getAmuletColor(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return Optional.empty();
        }
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (!tag.contains("amuletColor")) {
            return Optional.empty();
        }
        return Optional.of(tag.getInt("amuletColor"));
    }

    public static void freezeAmuletColor(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return;
        }
        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (tag.contains("amuletColor")) {
            return;
        }
        if (rand.nextInt(400) == 0) {
            tag.putInt("amuletColor", 0xFFFFFFFF);
        } else {
            tag.putInt("amuletColor", Color.getHSBColor(rand.nextFloat(), 0.7F, 1.0F).getRGB() | 0xFF000000);
        }
    }

    public static List<AmuletEnchantment> getAmuletEnchantments(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return new ArrayList<>();
        }

        CompoundTag tag = NBTHelper.getPersistentData(stack);
        if (!tag.contains("amuletEnchantments")) {
            return new ArrayList<>();
        }
        ListTag enchants = tag.getList("amuletEnchantments", Tag.TAG_COMPOUND);
        List<AmuletEnchantment> enchantments = new ArrayList<>(enchants.size());
        for (int i = 0; i < enchants.size(); i++) {
            AmuletEnchantment ench = AmuletEnchantment.deserialize(enchants.getCompound(i));
            if (ench != null) {
                enchantments.add(ench);
            }
        }
        enchantments.sort(Comparator.comparing(AmuletEnchantment::getType));
        return enchantments;
    }

    public static void setAmuletEnchantments(ItemStack stack, List<AmuletEnchantment> enchantments) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemEnchantmentAmulet)) {
            return;
        }
        enchantments.sort(Comparator.comparing(AmuletEnchantment::getType));

        CompoundTag tag = NBTHelper.getPersistentData(stack);
        ListTag enchants = new ListTag();
        for (AmuletEnchantment enchant : enchantments) {
            enchants.add(enchant.serialize());
        }
        tag.put("amuletEnchantments", enchants);
    }
}
