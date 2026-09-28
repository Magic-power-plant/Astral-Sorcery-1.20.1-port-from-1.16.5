/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.source.provider.equipment;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.source.ModifierManager;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSourceProvider;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;

import java.util.Collection;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EquipmentSourceProvider
 * Created by HellFirePvP
 * Date: 02.04.2020 / 18:56
 */
public class EquipmentSourceProvider extends ModifierSourceProvider<EquipmentModifierSource> {

    static final String KEY_MOD_IDENTIFIER = "modifier_identifier";

    public EquipmentSourceProvider() {
        super(ModifierManager.EQUIPMENT_PROVIDER_KEY);
    }

    @Override
    protected void update(ServerPlayer Player) {
        for (EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            //Items held in offhand will not provide modifers.
            if (slot == net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
                continue;
            }

            ResourceLocation id = AstralSorcery.key("slot_" + slot.getName());

            ItemStack stack = Player.getItemBySlot(slot);
            EquipmentModifierSource slotSource = new EquipmentModifierSource(slot, stack.copy());
            if (!stack.isEmpty()) {
                Collection<PerkAttributeModifier> modifiers = slotSource.getModifiers(Player, LogicalSide.SERVER, false);
                if (!modifiers.isEmpty()) {
                    CompoundTag nbt = NBTHelper.getPersistentData(stack);
                    if (!nbt.hasUUID(KEY_MOD_IDENTIFIER)) {
                        nbt.putUUID(KEY_MOD_IDENTIFIER, UUID.randomUUID());
                    }
                    updateSource(Player, id, slotSource);
                } else {
                    updateSource(Player, id, null);
                }
            } else {
                updateSource(Player, id, null);
            }
        }
    }

    @Override
    protected void removeModifiers(ServerPlayer Player) {
        for (EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            if (slot == net.minecraft.world.entity.EquipmentSlot.OFFHAND) {
                continue;
            }

            ResourceLocation id = AstralSorcery.key("slot_" + slot.getName());
            updateSource(Player, id, null);
        }
    }

    @Override
    public void serialize(EquipmentModifierSource source, FriendlyByteBuf buf) {
        ByteBufUtils.writeEnum(buf, source.slot);
        ByteBufUtils.writeItemStack(buf, source.itemStack);
    }

    @Override
    public EquipmentModifierSource deserialize(FriendlyByteBuf buf) {
        EquipmentSlot type = ByteBufUtils.readEnum(buf, net.minecraft.world.entity.EquipmentSlot.class);
        ItemStack stack = ByteBufUtils.readItemStack(buf);
        return new EquipmentModifierSource(type, stack);
    }

}

